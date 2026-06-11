package com.example.data.repository

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// --- SESSION MANAGER ---
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("medlink_session", Context.MODE_PRIVATE)

    fun saveSession(userId: String, email: String, role: String) {
        prefs.edit()
            .putString("user_id", userId)
            .putString("email", email)
            .putString("role", role)
            .putBoolean("is_logged_in", true)
            .apply()
        _currentUserFlow.value = CurrentUser(userId, email, role, true)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentUserFlow.value = CurrentUser("", "", "", false)
    }

    data class CurrentUser(val id: String, val email: String, val role: String, val isLoggedIn: Boolean)

    private val _currentUserFlow = MutableStateFlow(
        CurrentUser(
            id = prefs.getString("user_id", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            role = prefs.getString("role", "") ?: "",
            isLoggedIn = prefs.getBoolean("is_logged_in", false)
        )
    )
    val currentUserFlow: StateFlow<CurrentUser> = _currentUserFlow.asStateFlow()
}

// --- AUTH REPOSITORY ---
class AuthRepository(
    private val userDao: UserDao,
    private val notificationDao: NotificationDao,
    private val sessionManager: SessionManager
) {
    val activeUser = sessionManager.currentUserFlow

    suspend fun signup(
        email: String,
        name: String,
        passwordHash: String,
        role: String,
        specialty: String? = null,
        licenseNumber: String? = null,
        registrationNumber: String? = null,
        governmentId: String? = null,
        phoneNumber: String? = null,
        location: String? = null
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                return@withContext Result.failure(Exception("Registration failed: Email already registered."))
            }

            val userId = UUID.randomUUID().toString()
            // Doctors are not approved upon signup and start as isApproved = false
            val isApproved = (role != "DOCTOR" && role != "ADMIN") || email == "admin@medlink.com"

            val user = UserEntity(
                id = userId,
                email = email,
                name = name,
                passwordHash = passwordHash,
                role = role,
                isApproved = isApproved,
                specialty = specialty,
                licenseNumber = licenseNumber,
                registrationNumber = registrationNumber,
                governmentId = governmentId,
                phoneNumber = phoneNumber,
                location = location
            )

            userDao.insertUser(user)

            // Auto log in if not a doctor (as doctors need admin approval first)
            if (isApproved) {
                sessionManager.saveSession(userId, email, role)
            }

            // Create system notification
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = "Welcome to MedLink!",
                    message = "Your account has been created successfully as a $role.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, passwordHash: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            if (email == "admin@medlink.com" && passwordHash == "admin123") {
                // Admin bypass
                var admin = userDao.getUserByEmail(email)
                if (admin == null) {
                    val uid = UUID.randomUUID().toString()
                    admin = UserEntity(uid, email, "System Administrator", "admin123", "ADMIN", true)
                    userDao.insertUser(admin)
                }
                sessionManager.saveSession(admin.id, email, "ADMIN")
                return@withContext Result.success(admin)
            }

            val user = userDao.getUserByEmail(email)
                ?: return@withContext Result.failure(Exception("Login failed: User not found."))

            if (user.passwordHash != passwordHash) {
                return@withContext Result.failure(Exception("Login failed: Incorrect password."))
            }

            if (user.role == "DOCTOR" && !user.isApproved) {
                return@withContext Result.failure(Exception("Verification pending: Your background medical license is currently being audited by MedLink compliance team. Access will be granted shortly."))
            }

            sessionManager.saveSession(user.id, user.email, user.role)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyDoctor(doctorId: String, isApproved: Boolean): Boolean = withContext(Dispatchers.IO) {
        val doctor = userDao.getUserById(doctorId)
        if (doctor != null && doctor.role == "DOCTOR") {
            val updated = doctor.copy(isApproved = isApproved)
            userDao.updateUser(updated)

            // Notify
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = doctorId,
                    title = if (isApproved) "Account Approved" else "Verification Rejected",
                    message = if (isApproved) "Congratulations, your medical practice credentials have been verified! You now have full access to MedLink Doctor Portal." 
                             else "Your verification request could not be completed. Please re-upload valid license records.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
            return@withContext true
        }
        false
    }

    suspend fun deleteAccount(userId: String) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(userId)
        sessionManager.clearSession()
    }

    suspend fun getUserDetails(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    suspend fun updateUserProfile(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun getPendingDoctorsFlow(): Flow<List<UserEntity>> = userDao.getPendingDoctorsFlow()
    fun getApprovedDoctorsFlow(): Flow<List<UserEntity>> = userDao.getApprovedDoctorsFlow()
}

// --- APPOINTMENT REPOSITORY & QUEUE ENGINE ---
class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val queueDao: QueueDao,
    private val queueItemDao: QueueItemDao,
    private val notificationDao: NotificationDao,
    private val userDao: UserDao
) {
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForPatient(patientId)

    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForDoctor(doctorId)

    suspend fun bookAppointment(
        patientId: String,
        doctorId: String,
        notes: String,
        dateTime: Long
    ): Result<AppointmentEntity> = withContext(Dispatchers.IO) {
        try {
            val patient = userDao.getUserById(patientId) ?: return@withContext Result.failure(Exception("Patient not found"))
            val doctor = userDao.getUserById(doctorId) ?: return@withContext Result.failure(Exception("Doctor not found"))

            // Compute calendar bounds for today
            val cal = Calendar.getInstance().apply { timeInMillis = dateTime }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startOfDay = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis

            // Next queue number
            val countToday = appointmentDao.getAppointmentCountForDoctorToday(doctorId, startOfDay, endOfDay)
            val queueNumber = countToday + 1

            val appointmentId = UUID.randomUUID().toString()
            val appointment = AppointmentEntity(
                id = appointmentId,
                patientId = patientId,
                patientName = patient.name,
                doctorId = doctorId,
                doctorName = doctor.name,
                dateTime = dateTime,
                status = "SCHEDULED",
                notes = notes,
                queueNumber = queueNumber
            )

            appointmentDao.insertAppointment(appointment)

            // Add item to queue_items list
            val queueItemId = UUID.randomUUID().toString()
            val queueItem = QueueItemEntity(
                id = queueItemId,
                doctorId = doctorId,
                appointmentId = appointmentId,
                patientName = patient.name,
                queueNumber = queueNumber,
                status = "WAITING",
                timestamp = System.currentTimeMillis()
            )
            queueItemDao.insertQueueItem(queueItem)

            // Maintain doctor's general queue profile
            val curQueue = queueDao.getQueueForDoctor(doctorId)
            if (curQueue == null) {
                val newQueue = QueueEntity(
                    doctorId = doctorId,
                    currentPatientId = null,
                    currentPatientName = null,
                    currentQueueNumber = 0,
                    estimatedWaitMinutes = queueNumber * 15 // 15 mins per patient
                )
                queueDao.insertOrUpdateQueue(newQueue)
            } else {
                val updatedQueue = curQueue.copy(
                    estimatedWaitMinutes = (queueNumber - curQueue.currentQueueNumber).coerceAtLeast(0) * 15
                )
                queueDao.insertOrUpdateQueue(updatedQueue)
            }

            // Notifications
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = patientId,
                    title = "Appointment Booked",
                    message = "Your consultation with Dr. ${doctor.name} is confirmed. Queue Position: #${queueNumber}.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = doctorId,
                    title = "New Appointment Scheduled",
                    message = "${patient.name} has booked an appointment today. Queue Position: #${queueNumber}.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )

            Result.success(appointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(appointmentId: String) = withContext(Dispatchers.IO) {
        val appt = appointmentDao.getAppointmentById(appointmentId)
        if (appt != null) {
            val updated = appt.copy(status = "CANCELLED")
            appointmentDao.updateAppointment(updated)
            // Remove from waiting queue items
            queueItemDao.deleteQueueItemByAppointmentId(appointmentId)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = appt.patientId,
                    title = "Appointment Cancelled",
                    message = "Your appointment with Dr. ${appt.doctorName} has been cancelled.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }
    }
}

// --- QUEUE MANAGEMENT REPOSITORY ---
class QueueRepository(
    private val queueDao: QueueDao,
    private val queueItemDao: QueueItemDao,
    private val appointmentDao: AppointmentDao,
    private val notificationDao: NotificationDao
) {
    fun getQueueForDoctorFlow(doctorId: String): Flow<QueueEntity?> =
        queueDao.getQueueForDoctorFlow(doctorId)

    fun getWaitingQueueItemsFlow(doctorId: String): Flow<List<QueueItemEntity>> =
        queueItemDao.getQueueItemsForDoctorFlow(doctorId, "WAITING")

    fun getCompletedQueueItemsFlow(doctorId: String): Flow<List<QueueItemEntity>> =
        queueItemDao.getQueueItemsForDoctorFlow(doctorId, "COMPLETED")

    suspend fun nextPatient(doctorId: String): QueueItemEntity? = withContext(Dispatchers.IO) {
        val waiting = queueItemDao.getWaitingItemsForDoctor(doctorId)
        val doctorQueue = queueDao.getQueueForDoctor(doctorId) ?: QueueEntity(doctorId, null, null, 0, 0)

        // Complete the current patient if one exists
        if (doctorQueue.currentPatientId != null) {
            val curApptId = waiting.firstOrNull { it.status == "ACTIVE" }?.appointmentId
            if (curApptId != null) {
                val appt = appointmentDao.getAppointmentById(curApptId)
                if (appt != null) {
                    appointmentDao.updateAppointment(appt.copy(status = "COMPLETED"))
                }
            }
        }

        val activeItem = waiting.firstOrNull() ?: return@withContext null

        // Check if we have an item to set to active
        val updatedItem = activeItem.copy(status = "ACTIVE")
        queueItemDao.updateQueueItem(updatedItem)

        // Update Doctor Queue Profile
        val updatedQueue = doctorQueue.copy(
            currentPatientId = activeItem.id,
            currentPatientName = activeItem.patientName,
            currentQueueNumber = activeItem.queueNumber,
            estimatedWaitMinutes = ((waiting.size - 1) * 12) // 12-min estimate
        )
        queueDao.insertOrUpdateQueue(updatedQueue)

        // Mark appointment in progress
        val appt = appointmentDao.getAppointmentById(activeItem.appointmentId)
        if (appt != null) {
            appointmentDao.updateAppointment(appt.copy(status = "IN_PROGRESS"))
            
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = appt.patientId,
                    title = "You're Up Next!",
                    message = "Dr. ${appt.doctorName} is ready to see you now. Please proceed to the consultation room.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }

        updatedItem
    }

    suspend fun completeCurrentPatient(doctorId: String) = withContext(Dispatchers.IO) {
        val doctorQueue = queueDao.getQueueForDoctor(doctorId) ?: return@withContext
        val currentItemId = doctorQueue.currentPatientId ?: return@withContext

        val items = queueItemDao.getWaitingItemsForDoctor(doctorId)
        val currentItem = items.find { it.id == currentItemId || it.status == "ACTIVE" }

        if (currentItem != null) {
            // Delete queue item as completed or update it
            queueItemDao.updateQueueItem(currentItem.copy(status = "COMPLETED"))
            val appt = appointmentDao.getAppointmentById(currentItem.appointmentId)
            if (appt != null) {
                appointmentDao.updateAppointment(appt.copy(status = "COMPLETED"))
                
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = appt.patientId,
                        title = "Consultation Finished",
                        message = "Your checkup with Dr. ${appt.doctorName} is successfully completed. Safe travels!",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
            }
        }

        // Reset Doctor Queue Header
        val nextWaitingList = queueItemDao.getWaitingItemsForDoctor(doctorId).filter { it.status == "WAITING" }
        val updatedQueue = doctorQueue.copy(
            currentPatientId = null,
            currentPatientName = null,
            estimatedWaitMinutes = nextWaitingList.size * 12
        )
        queueDao.insertOrUpdateQueue(updatedQueue)
    }
}

// --- PRESCRIPTION ENGINE REPOSITORY ---
class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao,
    private val notificationDao: NotificationDao,
    private val context: Context
) {
    fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getPrescriptionsForPatient(patientId)

    fun getPrescriptionsForDoctor(doctorId: String): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getPrescriptionsForDoctor(doctorId)

    suspend fun createPrescription(
        patientId: String,
        patientName: String,
        doctorId: String,
        doctorName: String,
        diagnoses: String,
        medicationsJson: String
    ): Result<PrescriptionEntity> = withContext(Dispatchers.IO) {
        try {
            val prescriptionId = UUID.randomUUID().toString()
            
            // Draw visual PDF on device
            val pdfFile = generateDigitalPrescriptionPDF(
                prescriptionId = prescriptionId,
                patientName = patientName,
                doctorName = doctorName,
                diagnoses = diagnoses,
                medicationsJson = medicationsJson
            )

            val prescription = PrescriptionEntity(
                id = prescriptionId,
                patientId = patientId,
                patientName = patientName,
                doctorId = doctorId,
                doctorName = doctorName,
                diagnoses = diagnoses,
                medicationsJson = medicationsJson,
                pdfPath = pdfFile?.absolutePath,
                timestamp = System.currentTimeMillis()
            )

            prescriptionDao.insertPrescription(prescription)

            // Notify
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = patientId,
                    title = "New Digital Prescription Issued",
                    message = "Dr. $doctorName has issued a digital prescription for you. PDF generated and available for offline download.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )

            Result.success(prescription)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateDigitalPrescriptionPDF(
        prescriptionId: String,
        patientName: String,
        doctorName: String,
        diagnoses: String,
        medicationsJson: String
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor("#0F172A") // Slate Dark
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#3B82F6") // Clinical Blue Accent
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.parseColor("#475569")
                textSize = 13f
                isAntiAlias = true
            }

            val boldBodyPaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val dividerPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            // Top Header Letterhead
            canvas.drawText("MEDLINK CLINICAL PLATFORM", 50f, 60f, titlePaint)
            canvas.drawText("Official Certified Medical Prescription Record", 50f, 80f, subtitlePaint)
            canvas.drawLine(50f, 100f, 545f, 100f, dividerPaint)

            // Info Grid
            var y = 130f
            canvas.drawText("Prescription ID:", 50f, y, boldBodyPaint)
            canvas.drawText(prescriptionId, 180f, y, bodyPaint)

            y += 25f
            canvas.drawText("Attending Physician:", 50f, y, boldBodyPaint)
            canvas.drawText("Dr. $doctorName", 180f, y, bodyPaint)

            y += 25f
            canvas.drawText("Certified Patient:", 50f, y, boldBodyPaint)
            canvas.drawText(patientName, 180f, y, bodyPaint)

            y += 25f
            canvas.drawText("Dated:", 50f, y, boldBodyPaint)
            canvas.drawText(SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date()), 180f, y, bodyPaint)

            y += 35f
            canvas.drawLine(50f, y, 545f, y, dividerPaint)

            // Diagnosis Box
            y += 30f
            canvas.drawText("DIAGNOSIS & CLINICAL SYMPTOMS:", 50f, y, subtitlePaint)
            y += 25f
            canvas.drawText(diagnoses, 50f, y, bodyPaint)

            y += 35f
            canvas.drawLine(50f, y, 545f, y, dividerPaint)

            // Rx List Section
            y += 35f
            canvas.drawText("Rx DIRECTIVE & MEDICATION LIST:", 50f, y, subtitlePaint)
            y += 10f

            // Parse simple medication lines (comma separated or raw json style line breaks)
            val medications = medicationsJson.split(",", "\n", "|").filter { it.trim().isNotEmpty() }
            for (med in medications) {
                y += 25f
                canvas.drawCircle(60f, y - 4f, 4f, subtitlePaint)
                canvas.drawText(med.trim(), 80f, y, bodyPaint)
            }

            y += 80f
            canvas.drawLine(50f, y, 545f, y, dividerPaint)
            y += 25f
            canvas.drawText("Signed digitally via MedLink Secure Signature Framework.", 50f, y, Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 10f
                isAntiAlias = true
            })

            pdfDocument.finishPage(page)

            // Write File to Disk
            val dir = File(context.filesDir, "prescriptions")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Prescription_$prescriptionId.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// --- COMPREHENSIVE REVIEWS REPOSITORY ---
class ReviewRepository(private val reviewDao: ReviewDao) {
    fun getReviewsForDoctorFlow(doctorId: String): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForDoctorFlow(doctorId)

    suspend fun submitReview(
        doctorId: String,
        patientId: String,
        patientName: String,
        rating: Int,
        comment: String
    ): Result<ReviewEntity> = withContext(Dispatchers.IO) {
        try {
            val review = ReviewEntity(
                id = UUID.randomUUID().toString(),
                doctorId = doctorId,
                patientId = patientId,
                patientName = patientName,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            reviewDao.insertReview(review)
            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// --- SYSTEM NOTIFICATIONS REPOSITORY ---
class NotificationRepository(private val notificationDao: NotificationDao) {
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    suspend fun markAllAsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }
}
