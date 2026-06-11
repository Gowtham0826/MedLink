package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.network.GeminiAssistantManager
import com.example.data.network.GeminiContent
import com.example.data.network.GeminiPart
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MedLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionManager = SessionManager(application)
    
    // Repositories
    val authRepository = AuthRepository(db.userDao(), db.notificationDao(), sessionManager)
    val appointmentRepository = AppointmentRepository(
        db.appointmentDao(),
        db.queueDao(),
        db.queueItemDao(),
        db.notificationDao(),
        db.userDao()
    )
    val queueRepository = QueueRepository(
        db.queueDao(),
        db.queueItemDao(),
        db.appointmentDao(),
        db.notificationDao()
    )
    val prescriptionRepository = PrescriptionRepository(db.prescriptionDao(), db.notificationDao(), application)
    val reviewRepository = ReviewRepository(db.reviewDao())
    val notificationRepository = NotificationRepository(db.notificationDao())

    // AI Assitant Manager
    private val geminiAssistant = GeminiAssistantManager()

    // ----------------------------------------------------
    // AUTHENTICATION & PROFILE DATA STATE
    // ----------------------------------------------------
    val currentUser = authRepository.activeUser
    
    private val _userDetails = MutableStateFlow<UserEntity?>(null)
    val userDetails: StateFlow<UserEntity?> = _userDetails.asStateFlow()

    private val _doctorsList = MutableStateFlow<List<UserEntity>>(emptyList())
    val doctorsList: StateFlow<List<UserEntity>> = _doctorsList.asStateFlow()

    private val _pendingDoctors = MutableStateFlow<List<UserEntity>>(emptyList())
    val pendingDoctors: StateFlow<List<UserEntity>> = _pendingDoctors.asStateFlow()

    init {
        // Collect current user shifts to fetch specific profile details
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user.isLoggedIn && user.id.isNotEmpty()) {
                    _userDetails.value = authRepository.getUserDetails(user.id)
                    loadUserNotifications(user.id)
                } else {
                    _userDetails.value = null
                }
            }
        }

        // Load Doctors List dynamically
        viewModelScope.launch {
            authRepository.getApprovedDoctorsFlow().collect {
                _doctorsList.value = it
            }
        }

        // Load Pending Approval Requests dynamically for Admins
        viewModelScope.launch {
            authRepository.getPendingDoctorsFlow().collect {
                _pendingDoctors.value = it
            }
        }
    }

    fun signupPatient(email: String, name: String, pass: String, phone: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val res = authRepository.signup(
                email = email,
                name = name,
                passwordHash = pass,
                role = "PATIENT",
                phoneNumber = phone
            )
            if (res.isSuccess) {
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message
            }
        }
    }

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun signupDoctor(
        email: String,
        name: String,
        pass: String,
        specialty: String,
        license: String,
        registration: String,
        governmentId: String,
        phone: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authError.value = null
            val res = authRepository.signup(
                email = email,
                name = name,
                passwordHash = pass,
                role = "DOCTOR",
                specialty = specialty,
                licenseNumber = license,
                registrationNumber = registration,
                governmentId = governmentId,
                phoneNumber = phone,
                location = location
            )
            if (res.isSuccess) {
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val res = authRepository.login(email, pass)
            if (res.isSuccess) {
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message
            }
        }
    }

    fun clearAuthErrors() {
        _authError.value = null
    }

    fun logout() {
        authRepository.logout()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user.isLoggedIn) {
                authRepository.deleteAccount(user.id)
            }
        }
    }

    fun updateUserProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            authRepository.updateUserProfile(updatedUser)
            _userDetails.value = updatedUser
        }
    }

    // ----------------------------------------------------
    // APPOINTMENT BOOKING STATE
    // ----------------------------------------------------
    private val _patientAppointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val patientAppointments: StateFlow<List<AppointmentEntity>> = _patientAppointments.asStateFlow()

    private val _doctorAppointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val doctorAppointments: StateFlow<List<AppointmentEntity>> = _doctorAppointments.asStateFlow()

    fun loadPatientAppointments(patientId: String) {
        viewModelScope.launch {
            appointmentRepository.getAppointmentsForPatient(patientId).collect {
                _patientAppointments.value = it
            }
        }
    }

    fun loadDoctorAppointments(doctorId: String) {
        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(doctorId).collect {
                _doctorAppointments.value = it
            }
        }
    }

    fun bookAppointment(doctorId: String, notes: String, timeInMillis: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            val patientId = currentUser.value.id
            if (patientId.isNotEmpty()) {
                appointmentRepository.bookAppointment(patientId, doctorId, notes, timeInMillis)
                onComplete()
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.cancelAppointment(appointmentId)
        }
    }

    // ----------------------------------------------------
    // REALTIME QUEUE SYSTEM STATE
    // ----------------------------------------------------
    private val _activeQueue = MutableStateFlow<QueueEntity?>(null)
    val activeQueue: StateFlow<QueueEntity?> = _activeQueue.asStateFlow()

    private val _waitingQueueItems = MutableStateFlow<List<QueueItemEntity>>(emptyList())
    val waitingQueueItems: StateFlow<List<QueueItemEntity>> = _waitingQueueItems.asStateFlow()

    private val _completedQueueItems = MutableStateFlow<List<QueueItemEntity>>(emptyList())
    val completedQueueItems: StateFlow<List<QueueItemEntity>> = _completedQueueItems.asStateFlow()

    fun listenToQueueForDoctor(doctorId: String) {
        viewModelScope.launch {
            queueRepository.getQueueForDoctorFlow(doctorId).collect {
                _activeQueue.value = it
            }
        }
        viewModelScope.launch {
            queueRepository.getWaitingQueueItemsFlow(doctorId).collect {
                _waitingQueueItems.value = it
            }
        }
        viewModelScope.launch {
            queueRepository.getCompletedQueueItemsFlow(doctorId).collect {
                _completedQueueItems.value = it
            }
        }
    }

    fun callNextPatient(doctorId: String) {
        viewModelScope.launch {
            queueRepository.nextPatient(doctorId)
        }
    }

    fun completeCurrentPatient(doctorId: String) {
        viewModelScope.launch {
            queueRepository.completeCurrentPatient(doctorId)
        }
    }

    // ----------------------------------------------------
    // PRESCRIPTION ENGINE
    // ----------------------------------------------------
    private val _patientPrescriptions = MutableStateFlow<List<PrescriptionEntity>>(emptyList())
    val patientPrescriptions: StateFlow<List<PrescriptionEntity>> = _patientPrescriptions.asStateFlow()

    private val _doctorPrescriptions = MutableStateFlow<List<PrescriptionEntity>>(emptyList())
    val doctorPrescriptions: StateFlow<List<PrescriptionEntity>> = _doctorPrescriptions.asStateFlow()

    fun loadPatientPrescriptions(patientId: String) {
        viewModelScope.launch {
            prescriptionRepository.getPrescriptionsForPatient(patientId).collect {
                _patientPrescriptions.value = it
            }
        }
    }

    fun loadDoctorPrescriptions(doctorId: String) {
        viewModelScope.launch {
            prescriptionRepository.getPrescriptionsForDoctor(doctorId).collect {
                _doctorPrescriptions.value = it
            }
        }
    }

    fun issuePrescription(
        patientId: String,
        patientName: String,
        symptoms: String,
        medications: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val doctor = userDetails.value
            if (doctor != null && doctor.role == "DOCTOR") {
                prescriptionRepository.createPrescription(
                    patientId = patientId,
                    patientName = patientName,
                    doctorId = doctor.id,
                    doctorName = doctor.name,
                    diagnoses = symptoms,
                    medicationsJson = medications
                )
                onSuccess()
            }
        }
    }

    fun openPrescriptionPDF(context: Context, pdfPath: String) {
        try {
            val file = File(pdfPath)
            if (file.exists()) {
                val authority = "${context.packageName}.fileprovider"
                val uri: Uri = FileProvider.getUriForFile(context, authority, file)
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(intent, "Open Prescription Record"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ----------------------------------------------------
    // AI CLINICAL ASSISTANT (GEMINI)
    // ----------------------------------------------------
    private val _aiChatHistory = MutableStateFlow<List<GeminiContent>>(emptyList())
    val aiChatHistory: StateFlow<List<GeminiContent>> = _aiChatHistory.asStateFlow()

    private val _aiConsultationLoading = MutableStateFlow(false)
    val aiConsultationLoading: StateFlow<Boolean> = _aiConsultationLoading.asStateFlow()

    fun initiateAIConsultationMessage(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _aiConsultationLoading.value = true
            
            // Append User Question locally
            val userMsg = GeminiContent(role = "user", parts = listOf(GeminiPart(text = message)))
            val currentList = _aiChatHistory.value
            _aiChatHistory.value = currentList + userMsg

            // Call real Gemini API
            val responseText = geminiAssistant.chat(currentList, message)

            // Append response locally
            val modelMsg = GeminiContent(role = "model", parts = listOf(GeminiPart(text = responseText)))
            _aiChatHistory.value = _aiChatHistory.value + modelMsg
            _aiConsultationLoading.value = false
        }
    }

    fun resetAICorrespondenceHistory() {
        _aiChatHistory.value = emptyList()
    }

    // ----------------------------------------------------
    // REVIEWS STATE
    // ----------------------------------------------------
    private val _doctorReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val doctorReviews: StateFlow<List<ReviewEntity>> = _doctorReviews.asStateFlow()

    fun loadReviewsForDoctor(doctorId: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsForDoctorFlow(doctorId).collect {
                _doctorReviews.value = it
            }
        }
    }

    fun submitDoctorReview(doctorId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val patient = userDetails.value
            if (patient != null) {
                reviewRepository.submitReview(
                    doctorId = doctorId,
                    patientId = patient.id,
                    patientName = patient.name,
                    rating = rating,
                    comment = comment
                )
            }
        }
    }

    // ----------------------------------------------------
    // SYSTEM NOTIFICATIONS
    // ----------------------------------------------------
    private val _userNotifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val userNotifications: StateFlow<List<NotificationEntity>> = _userNotifications.asStateFlow()

    fun loadUserNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.getNotificationsForUser(userId).collect {
                _userNotifications.value = it
            }
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            val uid = currentUser.value.id
            if (uid.isNotEmpty()) {
                notificationRepository.markAllAsRead(uid)
            }
        }
    }

    // ----------------------------------------------------
    // ADMIN ACTIONS (VERIFICATIONS)
    // ----------------------------------------------------
    fun reviewDoctorLicense(doctorId: String, isApproved: Boolean) {
        viewModelScope.launch {
            authRepository.verifyDoctor(doctorId, isApproved)
        }
    }

    // ----------------------------------------------------
    // LEAVE & COVERAGE MANAGER FOR DOCTORS
    // ----------------------------------------------------
    private val _doctorLeaveRequests = MutableStateFlow<List<LeaveRequestEntity>>(emptyList())
    val doctorLeaveRequests: StateFlow<List<LeaveRequestEntity>> = _doctorLeaveRequests.asStateFlow()

    fun loadDoctorLeaveRequests(doctorId: String) {
        viewModelScope.launch {
            db.leaveRequestDao().getLeaveRequestsForDoctor(doctorId).collect {
                _doctorLeaveRequests.value = it
            }
        }
    }

    fun submitLeaveRequest(doctorId: String, start: Long, end: Long, reason: String) {
        viewModelScope.launch {
            val req = LeaveRequestEntity(
                id = UUID.randomUUID().toString(),
                doctorId = doctorId,
                startDate = start,
                endDate = end,
                reason = reason,
                status = "PENDING"
            )
            db.leaveRequestDao().insertLeaveRequest(req)
        }
    }

    private val _coverageRequests = MutableStateFlow<List<CoverageRequestEntity>>(emptyList())
    val coverageRequests: StateFlow<List<CoverageRequestEntity>> = _coverageRequests.asStateFlow()

    fun loadCoverageRequestsForDoctor(doctorId: String) {
        viewModelScope.launch {
            db.coverageRequestDao().getCoverageRequestsForDoctor(doctorId, doctorId).collect {
                _coverageRequests.value = it
            }
        }
    }

    fun submitCoverageRequest(doctorId: String, coveringDoctorId: String, date: Long) {
        viewModelScope.launch {
            val req = CoverageRequestEntity(
                id = UUID.randomUUID().toString(),
                doctorId = doctorId,
                coveringDoctorId = coveringDoctorId,
                date = date,
                status = "PENDING"
            )
            db.coverageRequestDao().insertCoverageRequest(req)
        }
    }
}
