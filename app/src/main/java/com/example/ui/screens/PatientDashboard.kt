package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.R
import com.example.ui.theme.ClinicalBlue
import com.example.ui.theme.ClinicalCyan
import com.example.ui.theme.SlateDark
import com.example.data.database.*
import com.example.ui.viewmodel.MedLinkViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    viewModel: MedLinkViewModel,
    onLogout: () -> Unit
) {
    val userDetails by viewModel.userDetails.collectAsState()
    val doctors by viewModel.doctorsList.collectAsState()
    val appointments by viewModel.patientAppointments.collectAsState()
    val prescriptions by viewModel.patientPrescriptions.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()

    var activeTab by remember { mutableStateOf("dashboard") } // dashboard, doctors, prescriptions, ai_chat, notifications
    var showProfileDialog by remember { mutableStateOf(false) }

    // Trigger loads
    LaunchedEffect(userDetails) {
        userDetails?.let {
            viewModel.loadPatientAppointments(it.id)
            viewModel.loadPatientPrescriptions(it.id)
            viewModel.loadUserNotifications(it.id)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { activeTab = "dashboard" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PolishSky,
                        selectedTextColor = com.example.ui.theme.PolishSky,
                        indicatorColor = com.example.ui.theme.PolishSkyLight,
                        unselectedIconColor = Color(0xFF64748B).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF64748B).copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "doctors",
                    onClick = { activeTab = "doctors" },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Doctors") },
                    label = { Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PolishSky,
                        selectedTextColor = com.example.ui.theme.PolishSky,
                        indicatorColor = com.example.ui.theme.PolishSkyLight,
                        unselectedIconColor = Color(0xFF64748B).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF64748B).copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "prescriptions",
                    onClick = { activeTab = "prescriptions" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Rx") },
                    label = { Text("Prescriptions", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PolishSky,
                        selectedTextColor = com.example.ui.theme.PolishSky,
                        indicatorColor = com.example.ui.theme.PolishSkyLight,
                        unselectedIconColor = Color(0xFF64748B).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF64748B).copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "ai_chat",
                    onClick = { activeTab = "ai_chat" },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
                    label = { Text("AI Partner", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PolishSky,
                        selectedTextColor = com.example.ui.theme.PolishSky,
                        indicatorColor = com.example.ui.theme.PolishSkyLight,
                        unselectedIconColor = Color(0xFF64748B).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF64748B).copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "notifications",
                    onClick = { activeTab = "notifications" },
                    icon = {
                        BadgedBox(
                            badge = {
                                val unread = notifications.filter { !it.isRead }.size
                                if (unread > 0) {
                                    Badge(containerColor = Color.Red) {
                                        Text(unread.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    },
                    label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.PolishSky,
                        selectedTextColor = com.example.ui.theme.PolishSky,
                        indicatorColor = com.example.ui.theme.PolishSkyLight,
                        unselectedIconColor = Color(0xFF64748B).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF64748B).copy(alpha = 0.6f)
                    )
                )
            }
        },
        containerColor = com.example.ui.theme.PolishBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Patient Header Card
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GOOD MORNING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userDetails?.name ?: "Valued Patient",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.PolishDarkSlate,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeTab = "notifications" },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Quick Alerts",
                                tint = Color(0xFF64748B)
                            )
                        }

                        IconButton(
                            onClick = {
                                activeTab = "settings"
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(com.example.ui.theme.PolishSky, RoundedCornerShape(16.dp))
                                .testTag("patient_top_profile_button")
                        ) {
                            if (!userDetails?.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userDetails?.avatarUrl,
                                    contentDescription = "My Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initials = (userDetails?.name?.take(2) ?: "SJ").uppercase()
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Screen content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Tab Router
                when (activeTab) {
                    "dashboard" -> PatientDashboardHome(viewModel, appointments, prescriptions) { activeTab = it }
                    "doctors" -> DoctorsScreen(viewModel, doctors)
                    "prescriptions" -> PatientPrescriptionsView(viewModel, prescriptions)
                    "ai_chat" -> AIConsultationView(viewModel)
                    "notifications" -> PatientNotificationsLog(viewModel, notifications)
                    "settings" -> PatientSettingsView(viewModel, userDetails, onLogout)
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = com.example.ui.theme.PolishSky,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = com.example.ui.theme.PolishDarkSlate,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PatientDashboardHome(
    viewModel: MedLinkViewModel,
    appointments: List<AppointmentEntity>,
    prescriptions: List<PrescriptionEntity>,
    onTabChange: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // High Priority Live Queue representation
        val activeAppointment = appointments.firstOrNull { it.status == "IN_PROGRESS" || it.status == "SCHEDULED" }
        
        if (activeAppointment != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.PolishSky),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "LIVE QUEUE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Est. Wait: 12 min",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Queue position badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val position = String.format("%02d", activeAppointment.queueNumber)
                                    Text(
                                        text = position,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "POSITION",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeAppointment.doctorName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = activeAppointment.notes.takeIf { it.isNotBlank() } ?: "Cardiology Specialist",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                            
                            Button(
                                onClick = { onTabChange("doctors") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Check-in",
                                    color = com.example.ui.theme.PolishSky,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Live Queue Placeholder when no upcoming slots
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.PolishSky),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Queue Status is Standby",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "No active check-ins currently.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { onTabChange("doctors") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Book consultation", color = com.example.ui.theme.PolishSky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Actions Bento Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { onTabChange("ai_chat") },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("✨")
                                }
                            }
                            Column {
                                Text("AI MedLink", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 13.sp)
                                Text("Verify symptoms or check up instantly", color = Color(0xFF64748B), fontSize = 10.sp, lineHeight = 12.sp)
                            }
                        }
                    }

                    Card(
                        onClick = { onTabChange("prescriptions") },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFECFDF5),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("💊")
                                }
                            }
                            Column {
                                Text("Prescriptions", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 13.sp)
                                Text("Digital prescription cache and logs", color = Color(0xFF64748B), fontSize = 10.sp, lineHeight = 12.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { onTabChange("doctors") },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3E8FF),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📋")
                                }
                            }
                            Column {
                                Text("Doctors", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 13.sp)
                                Text("Browse and book certified staff", color = Color(0xFF64748B), fontSize = 10.sp, lineHeight = 12.sp)
                            }
                        }
                    }

                    Card(
                        onClick = { onTabChange("notifications") },
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.PolishDarkSlate),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🛡️")
                                }
                            }
                            Column {
                                Text("Clinical Logs", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Security logs and regulatory alerts", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, lineHeight = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Healthcare Itinerary Log",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.PolishDarkSlate,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EventNote, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No consultations scheduled yet.", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(appointments) { appt ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = com.example.ui.theme.PolishSky, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(appt.doctorName, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ailment Notes: ${appt.notes}", fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "Queue No: #${appt.queueNumber}",
                                fontSize = 11.sp,
                                color = com.example.ui.theme.PolishSky,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                            Text(
                                formatter.format(Date(appt.dateTime)),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (appt.status) {
                                    "SCHEDULED" -> Color(0xFFEFF6FF)
                                    "IN_PROGRESS" -> Color(0xFFECFDF5)
                                    "COMPLETED" -> Color(0xFFF0FDF4)
                                    else -> Color(0xFFFEF2F2)
                                },
                                contentColor = when (appt.status) {
                                    "SCHEDULED" -> Color(0xFF1E40AF)
                                    "IN_PROGRESS" -> Color(0xFF065F46)
                                    "COMPLETED" -> Color(0xFF166534)
                                    else -> Color(0xFF991B1B)
                                }
                            ) {
                                Text(
                                    appt.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DoctorsScreen(
    viewModel: MedLinkViewModel,
    doctors: List<UserEntity>
) {
    var searchSpecialty by remember { mutableStateOf("") }
    var showBookModal by remember { mutableStateOf<UserEntity?>(null) }
    var showReviewModal by remember { mutableStateOf<UserEntity?>(null) }

    val filteredDoctors = doctors.filter {
        searchSpecialty.isBlank() || (it.specialty?.contains(searchSpecialty, ignoreCase = true) == true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchSpecialty,
            onValueChange = { searchSpecialty = it },
            label = { Text("Filter Specialty / Group") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                focusedBorderColor = com.example.ui.theme.PolishSky,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        if (filteredDoctors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No verified operators matching that specialty.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredDoctors) { doc ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = com.example.ui.theme.PolishSkyLight,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = com.example.ui.theme.PolishSky,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(doc.name, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 16.sp)
                                    Text("Group: ${doc.specialty ?: "General Practice"}", color = com.example.ui.theme.PolishSky, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showBookModal = doc },
                                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Schedule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showReviewModal = doc },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(Icons.Default.StarRate, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF59E0B))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Feedback", fontSize = 12.sp, color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Book Consultation Dialogue
        if (showBookModal != null) {
            val d = showBookModal!!
            var appointmentNotes by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showBookModal = null },
                title = { Text("Consultation Scheduling", color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Selected Doctor: ${d.name}", color = Color(0xFF64748B), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = appointmentNotes,
                            onValueChange = { appointmentNotes = it },
                            label = { Text("Symptoms / Operational Notes") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                                unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                                focusedBorderColor = com.example.ui.theme.PolishSky,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.bookAppointment(d.id, appointmentNotes, System.currentTimeMillis()) {
                                showBookModal = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Appointment", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookModal = null }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                },
                containerColor = Color.White
            )
        }

        // Custom Review Doctor Dialogue
        if (showReviewModal != null) {
            val d = showReviewModal!!
            var comment by remember { mutableStateOf("") }
            var rating by remember { mutableStateOf(5) }
            AlertDialog(
                onDismissRequest = { showReviewModal = null },
                title = { Text("Grade Operating Doctor", color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Doctor: ${d.name}", color = Color(0xFF64748B), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { rate ->
                                Icon(
                                    imageVector = if (rate <= rating) Icons.Default.Star else Icons.Outlined.StarRate,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { rating = rate }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Experience review comments") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                                unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                                focusedBorderColor = com.example.ui.theme.PolishSky,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.submitDoctorReview(d.id, rating, comment)
                            showReviewModal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Review", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReviewModal = null }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun PatientPrescriptionsView(
    viewModel: MedLinkViewModel,
    prescriptions: List<PrescriptionEntity>
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Medical Prescriptions (Rx) Cache",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = com.example.ui.theme.PolishDarkSlate,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (prescriptions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No prescriptions have been issued to you yet.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(prescriptions) { rx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Issued by: Dr. ${rx.doctorName}", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Diagnostics: ${rx.diagnoses}", fontSize = 13.sp, color = com.example.ui.theme.PolishDarkSlate)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Directives: ${rx.medicationsJson}", fontSize = 12.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(14.dp))

                            if (rx.pdfPath != null) {
                                Button(
                                    onClick = { viewModel.openPrescriptionPDF(context, rx.pdfPath) },
                                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download Certified Prescription PDF", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIConsultationView(viewModel: MedLinkViewModel) {
    val chatHistory by viewModel.aiChatHistory.collectAsState()
    val loading by viewModel.aiConsultationLoading.collectAsState()
    var userPrompt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = com.example.ui.theme.PolishSky)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Secure AI Clinical Assistant Proxy", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 13.sp)
                    Text("Guarded under safety controls. Advise, never diagnoses.", color = Color(0xFF64748B), fontSize = 11.sp)
                }
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatHistory) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .background(
                                color = if (isUser) com.example.ui.theme.PolishSky else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 16.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.parts.firstOrNull()?.text ?: "",
                            fontSize = 13.sp,
                            color = if (isUser) Color.White else com.example.ui.theme.PolishDarkSlate
                        )
                    }
                }
            }

            if (loading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(color = com.example.ui.theme.PolishSky, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                label = { Text("Describe inquiry or symptoms...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                    unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                    focusedBorderColor = com.example.ui.theme.PolishSky,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        viewModel.initiateAIConsultationMessage(userPrompt)
                        userPrompt = ""
                    }
                },
                modifier = Modifier
                    .background(com.example.ui.theme.PolishSky, CircleShape)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Query", tint = Color.White)
            }
        }
    }
}

@Composable
fun PatientNotificationsLog(
    viewModel: MedLinkViewModel,
    notifications: List<NotificationEntity>
) {
    LaunchedEffect(Unit) {
        viewModel.markNotificationsAsRead()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Push Operational Logs & Alerts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = com.example.ui.theme.PolishDarkSlate,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Operational alert log is completely clear.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { alert ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = if (alert.isRead) Color(0xFF94A3B8) else com.example.ui.theme.PolishSky
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(alert.title, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 13.sp)
                                Text(alert.message, color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientSettingsView(
    viewModel: MedLinkViewModel,
    userDetails: com.example.data.database.UserEntity?,
    onLogout: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAvatarPickerDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showInsuranceDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // Form states
    var editName by remember(userDetails) { mutableStateOf(userDetails?.name ?: "") }
    var editPhone by remember(userDetails) { mutableStateOf(userDetails?.phoneNumber ?: "") }
    var editInsurance by remember(userDetails) { mutableStateOf(userDetails?.insuranceInfo ?: "") }
    var editEmergencyContact by remember(userDetails) { mutableStateOf(userDetails?.emergencyContact ?: "") }

    // Media chooser launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (userDetails != null) {
                viewModel.updateUserProfile(userDetails.copy(avatarUrl = it.toString()))
            }
        }
    }

    val quickAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&h=256&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=256&h=256&q=80",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=256&h=256&q=80",
        "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=256&h=256&q=80"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. PREMIUM HEADER AVATAR CARD ---
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                // Verified Badge Top Right
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Verified Patient",
                    tint = Color(0xFF10B981),
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Profile Circle Avatar with Camera Button Overlap
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, com.example.ui.theme.PolishSky, CircleShape)
                            .clickable { showAvatarPickerDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userDetails?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userDetails?.avatarUrl,
                                contentDescription = "Patient Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = (userDetails?.name?.take(2) ?: "PT").uppercase()
                            Text(
                                text = initials,
                                color = com.example.ui.theme.PolishSky,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        // Camera icon badge at bottom right inside circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .background(com.example.ui.theme.PolishSky, CircleShape)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Edit photo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Text Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = userDetails?.name ?: "Verified Patient",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.PolishDarkSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SECURE PATIENT NODE ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = "Security Info",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "E2E Encrypted Medical Ledger",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. PERSONAL SETTINGS SUBTITLE ---
        Text(
            text = "PATIENT REGISTRY SETTINGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        // --- 3. OPTIONS CARDS ---
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Edit Profile Details (Name, Phone)
            Surface(
                onClick = { showEditProfileDialog = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE0F2FE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.PolishSky)
                        }
                        Column {
                            Text("Edit Registry Profile", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 14.sp)
                            Text("Update clinical record login name & primary contact phone", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }

            // Option 2: Insurance Registry Info (Insurance details)
            Surface(
                onClick = { showInsuranceDialog = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFD97706))
                        }
                        Column {
                            Text("Insurance Policy Records", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 14.sp)
                            Text("Manage policy providers, group claims & validation cards", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }

            // Option 3: Emergency Broadcast Network
            Surface(
                onClick = { showEmergencyDialog = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFEE2E2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                        }
                        Column {
                            Text("Emergency Care Contacts", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 14.sp)
                            Text("Configure peer contacts & automatic notification nodes", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }
        }

        // --- 4. SIGN OUT SECTION ---
        Text(
            text = "ACCOUNT SECURE ACTIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, top = 10.dp)
        )

        Surface(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFEF2F2),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFEE2E2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                    Column {
                        Text("Secure Registry Logout", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 14.sp)
                        Text("Log out from local patient node files on this device safely", fontSize = 11.sp, color = Color(0xFF991B1B))
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFFCA5A5))
            }
        }

        TextButton(
            onClick = { showDeleteConfirmDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Delete Patient Hub Registry Node Record", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // --- MEDLINK BRANDING FOOTER ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MEDLINK v1.0.4 PREMIUM BUILD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.PolishSky,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Secured Clinical Records Node Database",
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }

    // --- DIALOG 1: CHOOSE PROFILE PICTURE ---
    if (showAvatarPickerDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarPickerDialog = false },
            title = { Text("Set Up Profile Photo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select a beautiful quick virtual avatar index card, browse device files, or delete photo.", fontSize = 13.sp, color = Color(0xFF64748B))

                    Button(
                        onClick = {
                            showAvatarPickerDialog = false
                            pickerLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse Device Gallery Cards", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Instant Virtual Avatars:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickAvatars.forEachIndexed { idx, url ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (userDetails?.avatarUrl == url) Color(0xFF10B981) else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        if (userDetails != null) {
                                            viewModel.updateUserProfile(userDetails.copy(avatarUrl = url))
                                        }
                                        showAvatarPickerDialog = false
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Avatar Options",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (userDetails != null) {
                            viewModel.updateUserProfile(userDetails.copy(avatarUrl = null))
                        }
                        showAvatarPickerDialog = false
                    }
                ) {
                    Text("Clear Avatar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarPickerDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG 2: EDIT PROFILE ---
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Registrar Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Registrar Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_patient_name_input")
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Registrar Primary Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_patient_phone_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userDetails != null) {
                            val updated = userDetails.copy(
                                name = editName,
                                phoneNumber = editPhone
                            )
                            viewModel.updateUserProfile(updated)
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG 3: INSURANCE POLICY REGISTRY ---
    if (showInsuranceDialog) {
        AlertDialog(
            onDismissRequest = { showInsuranceDialog = false },
            title = { Text("Insurance Policy Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Specify your modern health insurance carrier, policy validation code, or groups for medical dispensation clearance.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = editInsurance,
                        onValueChange = { editInsurance = it },
                        label = { Text("Insurance Carrier / Policy Code") },
                        placeholder = { Text("e.g. Star Health #ST-99824-A") },
                        leadingIcon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_patient_insurance_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userDetails != null) {
                            val updated = userDetails.copy(
                                insuranceInfo = editInsurance
                            )
                            viewModel.updateUserProfile(updated)
                        }
                        showInsuranceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky)
                ) {
                    Text("Save Policy", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsuranceDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG 4: EMERGENCY BROADCAST CONTACTS ---
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Emergency Contacts", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Specify contact details or emergency peer nodes which MedLink specialists can call on your behalf.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = editEmergencyContact,
                        onValueChange = { editEmergencyContact = it },
                        label = { Text("Emergency Kin Name & Phone") },
                        placeholder = { Text("e.g., Jane Doe (Spouse) +91 99876 54321") },
                        leadingIcon = { Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_patient_emergency_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userDetails != null) {
                            val updated = userDetails.copy(
                                emergencyContact = editEmergencyContact
                            )
                            viewModel.updateUserProfile(updated)
                        }
                        showEmergencyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky)
                ) {
                    Text("Save Contact", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Permanently Delete Patient?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete your patient profile and medical prescriptions ledger from our records.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAccount()
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}
