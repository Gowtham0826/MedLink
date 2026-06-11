package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.ClinicalCyan
import com.example.ui.theme.SlateDark
import com.example.ui.viewmodel.MedLinkViewModel

@Composable
fun DoctorDashboardScreen(
    viewModel: MedLinkViewModel,
    onLogout: () -> Unit
) {
    val userDetails by viewModel.userDetails.collectAsState()
    val queueStatus by viewModel.activeQueue.collectAsState()
    val waitingList by viewModel.waitingQueueItems.collectAsState()
    val completedList by viewModel.completedQueueItems.collectAsState()
    val appointments by viewModel.doctorAppointments.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val leaveRequests by viewModel.doctorLeaveRequests.collectAsState()
    val coverageRequests by viewModel.coverageRequests.collectAsState()
    val approvedDoctorsList by viewModel.doctorsList.collectAsState()
    val prescriptions by viewModel.doctorPrescriptions.collectAsState()

    var activeTab by remember { mutableStateOf("dashboard") } // dashboard, queue, alerts, settings
    
    var showCoverageModal by remember { mutableStateOf(false) }
    var showLeaveModal by remember { mutableStateOf(false) }
    var showRxModal by remember { mutableStateOf(false) }

    // Synchronize Leave/Coverage resources on load
    LaunchedEffect(userDetails) {
        userDetails?.let {
            viewModel.listenToQueueForDoctor(it.id)
            viewModel.loadDoctorAppointments(it.id)
            viewModel.loadUserNotifications(it.id)
            viewModel.loadDoctorLeaveRequests(it.id)
            viewModel.loadCoverageRequestsForDoctor(it.id)
            viewModel.loadDoctorPrescriptions(it.id)
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
                    label = { Text("DASHBOARD", fontSize = 10.sp, fontWeight = if (activeTab == "dashboard") FontWeight.Black else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0284C7),
                        selectedTextColor = Color(0xFF0284C7),
                        indicatorColor = Color(0xFFE0F2FE),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "queue",
                    onClick = { activeTab = "queue" },
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = "Queue") },
                    label = { Text("QUEUE", fontSize = 10.sp, fontWeight = if (activeTab == "queue") FontWeight.Black else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0284C7),
                        selectedTextColor = Color(0xFF0284C7),
                        indicatorColor = Color(0xFFE0F2FE),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "alerts",
                    onClick = { activeTab = "alerts" },
                    icon = { 
                        Icon(
                            if (notifications.any { !it.isRead }) Icons.Default.NotificationsActive else Icons.Default.Notifications, 
                            contentDescription = "Alerts"
                        ) 
                    },
                    label = { Text("ALERTS", fontSize = 10.sp, fontWeight = if (activeTab == "alerts") FontWeight.Black else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0284C7),
                        selectedTextColor = Color(0xFF0284C7),
                        indicatorColor = Color(0xFFE0F2FE),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { activeTab = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("SETTINGS", fontSize = 10.sp, fontWeight = if (activeTab == "settings") FontWeight.Black else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0284C7),
                        selectedTextColor = Color(0xFF0284C7),
                        indicatorColor = Color(0xFFE0F2FE),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
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
            when (activeTab) {
                "dashboard" -> DoctorDashboardHomeView(
                    viewModel = viewModel,
                    userDetails = userDetails,
                    waitingList = waitingList,
                    appointments = appointments,
                    onRequestCoverageClick = { showCoverageModal = true },
                    onCrossNetworkShiftClick = { showLeaveModal = true },
                    onRxPadClick = { showRxModal = true },
                    onNavigateToQueue = { activeTab = "queue" },
                    onProfilePicClick = { activeTab = "settings" }
                )
                "queue" -> Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    DoctorQueueManagerView(viewModel, userDetails?.id ?: "", queueStatus, waitingList, completedList)
                }
                "alerts" -> Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    DoctorAlertsView(viewModel, notifications, userDetails?.id ?: "")
                }
                "settings" -> Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    DoctorSettingsView(viewModel, userDetails, onLogout)
                }
            }
        }
    }

    // Modal Request Coverage Dialog
    if (showCoverageModal) {
        RequestCoverageDialog(
            onDismiss = { showCoverageModal = false },
            viewModel = viewModel,
            doctorId = userDetails?.id ?: "",
            approvedDoctors = approvedDoctorsList,
            coverageRequests = coverageRequests
        )
    }

    // Modal Absence / Leave Shift Dialog
    if (showLeaveModal) {
        CrossNetworkShiftDialog(
            onDismiss = { showLeaveModal = false },
            viewModel = viewModel,
            doctorId = userDetails?.id ?: "",
            leaveRequests = leaveRequests
        )
    }

    // Modal Rx Dispensation Pad Dialog
    if (showRxModal) {
        PhysicianRxPadDialog(
            onDismiss = { showRxModal = false },
            viewModel = viewModel,
            doctorId = userDetails?.id ?: "",
            appointments = appointments,
            prescriptions = prescriptions
        )
    }
}

@Composable
fun DoctorDashboardHomeView(
    viewModel: MedLinkViewModel,
    userDetails: com.example.data.database.UserEntity?,
    waitingList: List<com.example.data.database.QueueItemEntity>,
    appointments: List<com.example.data.database.AppointmentEntity>,
    onRequestCoverageClick: () -> Unit,
    onCrossNetworkShiftClick: () -> Unit,
    onRxPadClick: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onProfilePicClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        // 1. Corporate Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.medlink_logo),
                    contentDescription = "MedLink Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MedLink",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0284C7),
                    letterSpacing = (-0.5).sp
                )
            }

            // Circular corporate avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFE2E8F0), CircleShape)
                    .background(Color(0xFFE2E8F0))
                    .clickable { onProfilePicClick() }
                    .testTag("doctor_top_profile_button"),
                contentAlignment = Alignment.Center
            ) {
                if (!userDetails?.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = userDetails?.avatarUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initials = (userDetails?.name?.take(2) ?: "MD").uppercase()
                    Text(
                        text = initials,
                        color = com.example.ui.theme.PolishDarkSlate,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 2. Dual state pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pill 1: PRACTITIONER NODE LIVE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFECFDF5), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PRACTITIONER NODE LIVE",
                    color = Color(0xFF047857),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Pill 2: MD WORKSPACE CONSOLE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MD WORKSPACE CONSOLE",
                    color = Color(0xFF1D4ED8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // 3. Welcome title
        Text(
            text = "Welcome, Dr. ${userDetails?.name ?: "Srilekha"}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = com.example.ui.theme.PolishDarkSlate,
            lineHeight = 36.sp,
            letterSpacing = (-0.5).sp
        )

        // 4. Facility subtext with blue circle dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6))
            )
            Spacer(modifier = Modifier.width(8.dp))
            val facilityName = if (userDetails?.specialty.isNullOrBlank()) "SAVEETHA DENTAL" else userDetails?.specialty?.uppercase()
            Text(
                text = "CLINIC FACILITY: $facilityName",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
        }

        // 5. Medical License registration black card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = (userDetails?.name?.take(2) ?: "MD").uppercase()
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "MEDICAL LICENSE REGISTRATION",
                        color = Color(0xFF0EA5E9),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = (userDetails?.licenseNumber ?: "REG-TMQYJGZL4EQ07FZEBFJ7XPAS9C82").uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }

        // 6. Two statistic cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Card: ACTIVE IN QUEUE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .clickable { onNavigateToQueue() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE IN QUEUE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${waitingList.size}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.PolishDarkSlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Realtime Waiting",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Right Card: TOTAL SCHEDULE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .clickable { onNavigateToQueue() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL SCHEDULE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${appointments.size}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.PolishDarkSlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Booked Consults",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 7. Large Card Section: FACILITY CONTINUITY & NETWORK
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "FACILITY CONTINUITY & NETWORK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )

                // Item 1: REQUEST COVERAGE
                NetworkActionRow(
                    title = "REQUEST COVERAGE",
                    subtitle = "DELEGATE ACTIVE PATIENT STREAMS",
                    icon = Icons.Default.Business,
                    iconBgColor = Color(0xFFF59E0B),
                    onClick = onRequestCoverageClick
                )

                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 14.dp))

                // Item 2: CROSS-NETWORK SHIFT
                NetworkActionRow(
                    title = "CROSS-NETWORK SHIFT",
                    subtitle = "MANAGE STANDBY CLINIC COVERAGES",
                    icon = Icons.Default.Schedule,
                    iconBgColor = Color(0xFF0EA5E9),
                    onClick = onCrossNetworkShiftClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3: PHYSICIAN RX PAD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E5A97)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRxPadClick() }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 10.dp, y = 10.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "PHYSICIAN RX PAD",
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Dispense HIPAA prescriptions",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.PolishDarkSlate,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DoctorAlertsView(
    viewModel: MedLinkViewModel,
    notifications: List<com.example.data.database.NotificationEntity>,
    doctorId: String
) {
    LaunchedEffect(doctorId) {
        viewModel.loadUserNotifications(doctorId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Clinical Alerts & Dispatch Center",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.PolishDarkSlate
            )
            if (notifications.any { !it.isRead }) {
                TextButton(onClick = { viewModel.markNotificationsAsRead() }) {
                    Text("Mark All Read", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No pending compliance or queue alerts.", color = Color(0xFF64748B), fontSize = 14.sp)
                    Text("You will see compliance reports or leaves approvals here.", color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notification ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (notification.isRead) Color.White else Color(0xFFF1F5F9)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (notification.isRead) 1.dp else 2.dp,
                                color = if (notification.isRead) Color(0xFFE2E8F0) else Color(0xFF0284C7).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (notification.isRead) Color(0xFFF1F5F9) else Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (notification.isRead) Color(0xFF64748B) else Color(0xFF0284C7)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notification.title,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.PolishDarkSlate,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notification.message,
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val dateStr = java.text.DateFormat.getDateTimeInstance(
                                    java.text.DateFormat.SHORT, java.text.DateFormat.SHORT
                                ).format(java.util.Date(notification.timestamp))
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
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
fun DoctorSettingsView(
    viewModel: MedLinkViewModel,
    userDetails: com.example.data.database.UserEntity?,
    onLogout: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAvatarPickerDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCredentialsDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Edit Profile Forms
    var editName by remember(userDetails) { mutableStateOf(userDetails?.name ?: "") }
    var editPhone by remember(userDetails) { mutableStateOf(userDetails?.phoneNumber ?: "") }
    var editLocation by remember(userDetails) { mutableStateOf(userDetails?.location ?: "") }

    // Edit Credentials Forms
    var editSpecialty by remember(userDetails) { mutableStateOf(userDetails?.specialty ?: "") }
    var editLicense by remember(userDetails) { mutableStateOf(userDetails?.licenseNumber ?: "") }
    var editRegNo by remember(userDetails) { mutableStateOf(userDetails?.registrationNumber ?: "") }
    var editGovId by remember(userDetails) { mutableStateOf(userDetails?.governmentId ?: "") }

    // Photo picker launcher
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
        "https://images.unsplash.com/photo-1622253692010-333f2da6031d?auto=format&fit=crop&w=256&h=256&q=80",
        "https://images.unsplash.com/photo-1594824813573-246434de83fb?auto=format&fit=crop&w=256&h=256&q=80",
        "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&w=256&h=256&q=80",
        "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&w=256&h=256&q=80"
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
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Practice",
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
                                contentDescription = "Specialist Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = (userDetails?.name?.take(2) ?: "MD").uppercase()
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
                            text = "Dr. ${userDetails?.name ?: "Verified Specialist"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.PolishDarkSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "DOCTOR PORTAL ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Room,
                                contentDescription = "Practice Address",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (!userDetails?.location.isNullOrEmpty()) userDetails!!.location!! else "Location coordinates not pinned",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // --- 2. PERSONAL SETTINGS SUBTITLE ---
        Text(
            text = "PERSONAL REGISTRY SETTINGS",
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
            // Option 1: Edit Profile details (Name, Phone, Live practice Location)
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
                            Text("Update clinical login name, contact phone & custom locations", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }

            // Option 2: Medical Credentials details (Specialty, License, Reg, Gov ID)
            Surface(
                onClick = { showCredentialsDialog = true },
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
                            Text("Verification Credentials", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 14.sp)
                            Text("Certified specialties, state medical licenses & compliance IDs", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }

            // Option 3: Notifications setups & options
            Surface(
                onClick = { showNotificationDialog = true },
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
                                .background(Color(0xFFF3E8FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF7C3AED))
                        }
                        Column {
                            Text("Registry Alerts & Pushes", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 14.sp)
                            Text("Configure urgent ER delegate calls & clinical queue buzzers", fontSize = 11.sp, color = Color(0xFF64748B))
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
                        Text("Secure Practice Sign Out", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 14.sp)
                        Text("Log out from local practitioner registration records safely", fontSize = 11.sp, color = Color(0xFF991B1B))
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFFCA5A5))
            }
        }

        TextButton(
            onClick = { showDeleteConfirmDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Delete Certified Practitioner Node", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                text = "Secure Blockchain Cryptographic Clinician Records",
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }

    // --- DIALOG 1: CHOOSE PROFILE PICTURE ---
    if (showAvatarPickerDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarPickerDialog = false },
            title = { Text("Clinical Profile Picture Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select a quick professional clinical avatar illustration, upload from your photo gallery, or reset details.", fontSize = 13.sp, color = Color(0xFF64748B))

                    // Browse Gallery launcher
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
                        Text("Upload Photo from Gallery Mode", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Instant Clinical Avatars:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))

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
                    Text("Reset Picture", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
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
                        label = { Text("Registered Practice Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_doctor_name_input")
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Contact Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_doctor_phone_input")
                    )

                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Live Practice Coordinates / Hub") },
                        placeholder = { Text("Click pin to locate Live Practice GPS") },
                        leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    editLocation = "Saveetha Specialty Heights, Bangalore Hwy, Chennai, (13.0285° N, 80.2440° E)"
                                }
                            ) {
                                Icon(Icons.Default.PinDrop, contentDescription = "Simulate GPS", tint = Color(0xFF10B981))
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.PolishSky,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_doctor_location_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userDetails != null) {
                            val updated = userDetails.copy(
                                name = editName,
                                phoneNumber = editPhone,
                                location = editLocation
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

    // --- DIALOG 3: CERTIFIED CREDENTIALS ---
    if (showCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showCredentialsDialog = false },
            title = { Text("Verification Credentials", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editSpecialty,
                        onValueChange = { editSpecialty = it },
                        label = { Text("Certified Medical Specialty") },
                        leadingIcon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editLicense,
                        onValueChange = { editLicense = it },
                        label = { Text("State Licensing License ID") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editRegNo,
                        onValueChange = { editRegNo = it },
                        label = { Text("Registry Registration Code") },
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editGovId,
                        onValueChange = { editGovId = it },
                        label = { Text("Government Fingerprint ID Code") },
                        leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userDetails != null) {
                            val updated = userDetails.copy(
                                specialty = editSpecialty,
                                licenseNumber = editLicense,
                                registrationNumber = editRegNo,
                                governmentId = editGovId
                            )
                            viewModel.updateUserProfile(updated)
                        }
                        showCredentialsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky)
                ) {
                    Text("Save Credentials", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCredentialsDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG 4: NOTIFICATIONS LIST ---
    if (showNotificationDialog) {
        var alertBeeps by remember { mutableStateOf(true) }
        var urgentCoverageAlerts by remember { mutableStateOf(true) }
        var drugDispensationConfirmations by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Pushes & Broadcast Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Active Patient Beep Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Buzz when new patients enter the clinical lobby", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = alertBeeps,
                            onCheckedChange = { alertBeeps = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PolishSky, checkedTrackColor = com.example.ui.theme.PolishSkyLight)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Urgent ER Delegations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Broadcast other clinician's leave relief notifications", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = urgentCoverageAlerts,
                            onCheckedChange = { urgentCoverageAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PolishSky, checkedTrackColor = com.example.ui.theme.PolishSkyLight)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Secured Rx Broadcasts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Push warnings from validated pharmacy audits on signatures", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = drugDispensationConfirmations,
                            onCheckedChange = { drugDispensationConfirmations = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PolishSky, checkedTrackColor = com.example.ui.theme.PolishSkyLight)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = Color.White
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Permanently Delete Node?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove your licensed doctor file from our localized registry. This action cannot be undone.", fontSize = 13.sp) },
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

@Composable
fun RequestCoverageDialog(
    onDismiss: () -> Unit,
    viewModel: MedLinkViewModel,
    doctorId: String,
    approvedDoctors: List<com.example.data.database.UserEntity>,
    coverageRequests: List<com.example.data.database.CoverageRequestEntity>
) {
    var selectedDoctorEntity by remember { mutableStateOf<com.example.data.database.UserEntity?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedDayOffset by remember { mutableStateOf(1) } // 1: Tomorrow, 2: In 2 Days, etc.
    var requestSuccessMessage by remember { mutableStateOf<String?>(null) }

    val otherDoctors = approvedDoctors.filter { it.id != doctorId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Network Coverage Board", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lodge an operational delegate request to transfer patient streams during clock-out periods.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Divider(color = Color(0xFFF1F5F9))

                Text("1. Select Certified Covering Specialist", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDropdown = !expandedDropdown }
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedDoctorEntity?.name?.let { "Dr. $it" } ?: "Select Specialist...",
                            color = if (selectedDoctorEntity == null) Color(0xFF94A3B8) else com.example.ui.theme.PolishDarkSlate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        if (otherDoctors.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No other certified clinicians found", color = Color.Gray, fontSize = 12.sp) },
                                onClick = { expandedDropdown = false }
                            )
                        } else {
                            otherDoctors.forEach { doc ->
                                DropdownMenuItem(
                                    text = { Text("Dr. ${doc.name} (${doc.specialty ?: "General Practice"})", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedDoctorEntity = doc
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text("2. Target Coverage Date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1 to "Tomorrow",
                        2 to "In 2 Days",
                        3 to "In 3 Days"
                    ).forEach { (offset, text) ->
                        val isSelected = selectedDayOffset == offset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFFEF3C7) else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFF59E0B) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDayOffset = offset }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                color = if (isSelected) Color(0xFFD97706) else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        selectedDoctorEntity?.let { doc ->
                            val targetDate = System.currentTimeMillis() + selectedDayOffset * 86400000
                            viewModel.submitCoverageRequest(
                                doctorId = doctorId,
                                coveringDoctorId = doc.id,
                                date = targetDate
                            )
                            requestSuccessMessage = "Coverage Delegation safely lodged to registry ledger successfully."
                        } ?: run {
                            requestSuccessMessage = "Error: Please choose a valid specialist doctor first."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lodge Coverage Transfer", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (requestSuccessMessage != null) {
                    Text(
                        text = requestSuccessMessage!!,
                        fontSize = 11.sp,
                        color = if (requestSuccessMessage!!.startsWith("Error")) Color.Red else Color(0xFF0F5132),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(color = Color(0xFFF1F5F9))

                Text("Coverage Delegation History", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                
                if (coverageRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active coverage delegations created.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        coverageRequests.forEach { req ->
                            val docName = otherDoctors.find { it.id == req.coveringDoctorId }?.name ?: "Specialist Doctor"
                            val dateStr = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(java.util.Date(req.date))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Covering: Dr. $docName", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate)
                                        Text("Date: $dateStr", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (req.status == "PENDING") Color(0xFFFEF3C7) else Color(0xFFD1E7DD),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = req.status,
                                            fontSize = 9.sp,
                                            color = if (req.status == "PENDING") Color(0xFFD97706) else Color(0xFF0F5132),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Board", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun CrossNetworkShiftDialog(
    onDismiss: () -> Unit,
    viewModel: MedLinkViewModel,
    doctorId: String,
    leaveRequests: List<com.example.data.database.LeaveRequestEntity>
) {
    var leaveReason by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(1) } // 1: 1 Day, 2: 2 Days, 3: 3 Days
    var leaveMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clinical Shift Leave Center", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Request clinical time-off. Leave notifications enter pending audits status until admin validation.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Divider(color = Color(0xFFF1F5F9))

                Text("Enter Leave Purpose Reason", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                OutlinedTextField(
                    value = leaveReason,
                    onValueChange = { leaveReason = it },
                    placeholder = { Text("e.g. Clinical Conference, Emergency Dental Surgery Delegate") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0EA5E9),
                        focusedLabelColor = Color(0xFF0EA5E9)
                    )
                )

                Text("Leave Duration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1 to "1 Day",
                        2 to "2 Days",
                        3 to "3 Days"
                    ).forEach { (valNo, name) ->
                        val isSel = selectedDuration == valNo
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0xFFE0F2FE) else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSel) Color(0xFF0EA5E9) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDuration = valNo }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (isSel) Color(0xFF0284C7) else Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (leaveReason.isNotBlank()) {
                            val start = System.currentTimeMillis() + 86400000
                            val end = start + (selectedDuration * 86400000L)
                            viewModel.submitLeaveRequest(doctorId, start, end, leaveReason)
                            leaveMessage = "Leave Request issued. Audit state recorded safely."
                            leaveReason = ""
                        } else {
                            leaveMessage = "Error: Please describe the clear purpose of time-off."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish Leave Notification", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (leaveMessage != null) {
                    Text(
                        text = leaveMessage!!,
                        fontSize = 11.sp,
                        color = if (leaveMessage!!.startsWith("Error")) Color.Red else Color(0xFF0F5132),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(color = Color(0xFFF1F5F9))

                Text("Audited Calendar Absences", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))

                if (leaveRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recorded leave sessions.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        leaveRequests.forEach { req ->
                            val sDate = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(java.util.Date(req.startDate))
                            val eDate = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(java.util.Date(req.endDate))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(req.reason, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Duration: $sDate - $eDate", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (req.status) {
                                                    "APPROVED" -> Color(0xFFECFDF5)
                                                    "REJECTED" -> Color(0xFFFEF2F2)
                                                    else -> Color(0xFFFFFBEB)
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = req.status,
                                            fontSize = 9.sp,
                                            color = when (req.status) {
                                                "APPROVED" -> Color(0xFF047857)
                                                "REJECTED" -> Color(0xFFB91C1C)
                                                else -> Color(0xFFD97706)
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Shift Board", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun PhysicianRxPadDialog(
    onDismiss: () -> Unit,
    viewModel: MedLinkViewModel,
    doctorId: String,
    appointments: List<com.example.data.database.AppointmentEntity>,
    prescriptions: List<com.example.data.database.PrescriptionEntity>
) {
    val activePatients = appointments.map { it.patientId to it.patientName }.distinct()
    
    var selectedPatientId by remember { mutableStateOf("") }
    var selectedPatientName by remember { mutableStateOf("") }
    var expandedPatientDropdown by remember { mutableStateOf(false) }

    var diagnoses by remember { mutableStateOf("") }
    var medicationsList by remember { mutableStateOf("") }
    var prescStatusMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF0E5A97), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("HIPAA Physician Rx Pad", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Publish secure digital clinical prescriptions and compile official records. Verified transactions write legally binding logs.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Divider(color = Color(0xFFF1F5F9))

                Text("1. Assign Patient Recipient", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPatientDropdown = !expandedPatientDropdown }
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (selectedPatientName.isEmpty()) "Assign Patient..." else "Patient: $selectedPatientName",
                            color = if (selectedPatientName.isEmpty()) Color(0xFF94A3B8) else com.example.ui.theme.PolishDarkSlate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
                    }

                    DropdownMenu(
                        expanded = expandedPatientDropdown,
                        onDismissRequest = { expandedPatientDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        if (activePatients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No consultations scheduled. Cannot assign.", color = Color.Gray, fontSize = 12.sp) },
                                onClick = { expandedPatientDropdown = false }
                            )
                        } else {
                            activePatients.forEach { patient ->
                                DropdownMenuItem(
                                    text = { Text(patient.second, color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedPatientId = patient.first
                                        selectedPatientName = patient.second
                                        expandedPatientDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text("2. Diagnoses & Symptom Findings", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                OutlinedTextField(
                    value = diagnoses,
                    onValueChange = { diagnoses = it },
                    placeholder = { Text("e.g. Mild periodontitis, wisdom tooth extraction post-op irritation") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0E5A97),
                        focusedLabelColor = Color(0xFF0E5A97)
                    )
                )

                Text("3. Rx Medication Instructions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                OutlinedTextField(
                    value = medicationsList,
                    onValueChange = { medicationsList = it },
                    placeholder = { Text("e.g. Amoxicillin 500mg (3x daily, 7 days), Ibuprofen 400mg (as needed)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0E5A97),
                        focusedLabelColor = Color(0xFF0E5A97)
                    ),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (selectedPatientId.isNotEmpty() && diagnoses.isNotBlank() && medicationsList.isNotBlank()) {
                            viewModel.issuePrescription(
                                patientId = selectedPatientId,
                                patientName = selectedPatientName,
                                symptoms = diagnoses,
                                medications = medicationsList
                            ) {
                                prescStatusMessage = "Prescription successfully compiled. Official clinical document issued."
                                diagnoses = ""
                                medicationsList = ""
                                selectedPatientId = ""
                                selectedPatientName = ""
                            }
                        } else {
                            prescStatusMessage = "Error: All fields are required to draft Rx Ledger."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E5A97)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Issue Secure Prescription", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (prescStatusMessage != null) {
                    Text(
                        text = prescStatusMessage!!,
                        fontSize = 11.sp,
                        color = if (prescStatusMessage!!.startsWith("Error")) Color.Red else Color(0xFF15803D),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(color = Color(0xFFF1F5F9))

                Text("Issued Direct Prescription Logs", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))

                if (prescriptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No digital prescriptions recorded.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prescriptions.forEach { rx ->
                            val rxDate = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(java.util.Date(rx.timestamp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Patient: ${rx.patientName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate)
                                        Text("Findings: ${rx.diagnoses}", fontSize = 10.sp, color = Color(0xFF475569))
                                        Text("Compiled Date: $rxDate", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                    }
                                    
                                    if (!rx.pdfPath.isNullOrBlank()) {
                                        IconButton(
                                            onClick = { viewModel.openPrescriptionPDF(context, rx.pdfPath) },
                                            modifier = Modifier
                                                .background(Color(0xFFE2E8F0), CircleShape)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = "View PDF",
                                                tint = Color(0xFF0F172A),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Rx Console", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
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
                color = com.example.ui.theme.SlateDark,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DoctorQueueManagerView(
    viewModel: MedLinkViewModel,
    doctorId: String,
    queueStatus: com.example.data.database.QueueEntity?,
    waitingList: List<com.example.data.database.QueueItemEntity>,
    completedList: List<com.example.data.database.QueueItemEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Realtime queue status center
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Clinical Roster Status - Today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.PolishSky,
                        modifier = Modifier.padding(bottom = 12.dp),
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Active Patient", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = queueStatus?.currentPatientName ?: "Unoccupied",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PolishDarkSlate
                            )
                            if (queueStatus?.currentQueueNumber != null && queueStatus.currentQueueNumber > 0) {
                                Text(
                                    "Consultation Ticket: #${queueStatus.currentQueueNumber}",
                                    fontSize = 12.sp,
                                    color = com.example.ui.theme.PolishSky,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Queue Backlog", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${waitingList.size} awaiting",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PolishDarkSlate
                            )
                            Text(
                                "Est. Delay: ${queueStatus?.estimatedWaitMinutes ?: 0} mins",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Controller ops
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.callNextPatient(doctorId) },
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consult Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.completeCurrentPatient(doctorId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish Case", fontSize = 12.sp, color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Awaiting Consultations Today (" + waitingList.size + ")",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.PolishDarkSlate
            )
        }

        if (waitingList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No patients scheduled in queue today.", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(waitingList) { item ->
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
                        Column {
                            Text(item.patientName, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 15.sp)
                            Text("Ticket Position: #${item.queueNumber}", fontSize = 12.sp, color = com.example.ui.theme.PolishSky, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (item.status == "ACTIVE") Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                            contentColor = if (item.status == "ACTIVE") Color(0xFF065F46) else Color(0xFF64748B)
                        ) {
                            Text(
                                item.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Concluded Sessions (" + completedList.size + ")",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.PolishDarkSlate
            )
        }

        if (completedList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("History log file is unoccupied today.", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(completedList) { item ->
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.patientName, color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Processed Ticket: #${item.queueNumber}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF10B981))
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorPrescribeView(
    viewModel: MedLinkViewModel,
    appointments: List<com.example.data.database.AppointmentEntity>
) {
    val activePatients = appointments.map { it.patientId to it.patientName }.distinct()
    
    var selectedPatientId by remember { mutableStateOf("") }
    var selectedPatientName by remember { mutableStateOf("") }
    var expandedPatientDropdown by remember { mutableStateOf(false) }

    var diagnoses by remember { mutableStateOf("") }
    var medicationsList by remember { mutableStateOf("") }
    var prescStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Certified Digital Prescription Publisher",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = com.example.ui.theme.PolishDarkSlate,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Patient Selector Dropdown Box
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { expandedPatientDropdown = !expandedPatientDropdown }
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (selectedPatientName.isEmpty()) "Assign Patient..." else "Patient: $selectedPatientName",
                    color = if (selectedPatientName.isEmpty()) Color(0xFF64748B) else com.example.ui.theme.PolishDarkSlate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = com.example.ui.theme.PolishSky)
            }

            DropdownMenu(
                expanded = expandedPatientDropdown,
                onDismissRequest = { expandedPatientDropdown = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            ) {
                if (activePatients.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No consultations scheduled. Cannot prescribe.", color = Color(0xFF64748B)) },
                        onClick = { expandedPatientDropdown = false }
                    )
                } else {
                    activePatients.forEach { patient ->
                        DropdownMenuItem(
                            text = { Text(patient.second, color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedPatientId = patient.first
                                selectedPatientName = patient.second
                                expandedPatientDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = diagnoses,
            onValueChange = { diagnoses = it },
            label = { Text("Diagnostics & Symptoms Records") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                focusedBorderColor = com.example.ui.theme.PolishSky,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = medicationsList,
            onValueChange = { medicationsList = it },
            label = { Text("Rx Medication Instructions (comma/line separated)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                focusedBorderColor = com.example.ui.theme.PolishSky,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedPatientId.isNotEmpty() && diagnoses.isNotBlank() && medicationsList.isNotBlank()) {
                    viewModel.issuePrescription(
                        patientId = selectedPatientId,
                        patientName = selectedPatientName,
                        symptoms = diagnoses,
                        medications = medicationsList
                    ) {
                        prescStatusMessage = "Prescription issued successfully. Digital PDF written and stored securely in Clinical Storage database."
                        diagnoses = ""
                        medicationsList = ""
                        selectedPatientId = ""
                        selectedPatientName = ""
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.AssignmentReturned, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Issue Digital Prescription & Generate PDF", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (prescStatusMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFFD1E7DD),
                contentColor = Color(0xFF0F5132),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = prescStatusMessage!!,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DoctorAbsencesView(viewModel: MedLinkViewModel, doctorId: String) {
    var reason by remember { mutableStateOf("") }
    var successNotice by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Out of Office (Leave & Absences)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = com.example.ui.theme.PolishDarkSlate,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason for absence (e.g. Clinical Conference)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                focusedBorderColor = com.example.ui.theme.PolishSky,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (reason.isNotBlank()) {
                    viewModel.submitLeaveRequest(
                        doctorId = doctorId,
                        start = System.currentTimeMillis() + 86400000, // starting tomorrow
                        end = System.currentTimeMillis() + 86400000 * 3, // 3 day duration
                        reason = reason
                    )
                    successNotice = true
                    reason = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Publish Leave Notification", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (successNotice) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFFD1E7DD),
                contentColor = Color(0xFF0F5132),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Leave recorded in operational schedule logs. Status is marked as PENDING until administrator calendar coordination audits.",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
