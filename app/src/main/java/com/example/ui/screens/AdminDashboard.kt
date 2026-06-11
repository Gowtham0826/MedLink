package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserEntity
import com.example.ui.theme.ClinicalCyan
import com.example.ui.theme.SlateDark
import com.example.ui.viewmodel.MedLinkViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: MedLinkViewModel,
    onLogout: () -> Unit
) {
    val pendingDoctors by viewModel.pendingDoctors.collectAsState()
    val approvedDoctors by viewModel.doctorsList.collectAsState()

    var showApprovedList by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = com.example.ui.theme.PolishBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Surface
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
                            text = "COMPLIANCE CONSOLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Compliance Console",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.PolishDarkSlate,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "System Administrator Oversight Dashboard",
                            fontSize = 12.sp,
                            color = com.example.ui.theme.PolishSky,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(16.dp))
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Fast tab selectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showApprovedList = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showApprovedList) com.example.ui.theme.PolishSky else Color.White,
                            contentColor = if (!showApprovedList) Color.White else com.example.ui.theme.PolishDarkSlate
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .then(
                                if (showApprovedList) Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Requests (${pendingDoctors.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { showApprovedList = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showApprovedList) com.example.ui.theme.PolishSky else Color.White,
                            contentColor = if (showApprovedList) Color.White else com.example.ui.theme.PolishDarkSlate
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .then(
                                if (!showApprovedList) Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Operators (${approvedDoctors.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (!showApprovedList) {
                    // Pending Review Queue
                    Text(
                        text = "Awaiting License Verification Audits",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.PolishDarkSlate,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (pendingDoctors.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No pending licensing requests require oversight checks.", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(pendingDoctors) { doctor ->
                                VerificationRequestCard(doctor) { isApproved ->
                                    viewModel.reviewDoctorLicense(doctor.id, isApproved)
                                }
                            }
                        }
                    }
                } else {
                    // Verified Directory Overlord View
                    Text(
                        text = "Officially Sanctioned & Licensed Clinicians",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.PolishDarkSlate,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (approvedDoctors.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No clinical operators registered on network directory.", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(approvedDoctors) { doctor ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(doctor.name, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 15.sp)
                                            Text("Specialty Group: ${doctor.specialty}", color = com.example.ui.theme.PolishSky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Reg Num: ${doctor.registrationNumber}", color = Color(0xFF64748B), fontSize = 11.sp)
                                        }
                                    }
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
fun VerificationRequestCard(
    doctor: UserEntity,
    onAuditDecision: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // General Info Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Pending, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(doctor.name, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PolishDarkSlate, fontSize = 15.sp)
                    Text("Requested Role: Doctor of ${doctor.specialty ?: "General Practice"}", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Document audit list
            Text("License Number: ${doctor.licenseNumber}", fontSize = 12.sp, color = com.example.ui.theme.PolishDarkSlate, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Medical Registry ID: ${doctor.registrationNumber}", fontSize = 12.sp, color = com.example.ui.theme.PolishDarkSlate)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Government Identity Hash: ${doctor.governmentId}", fontSize = 12.sp, color = com.example.ui.theme.PolishDarkSlate)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Contact Telephone: ${doctor.phoneNumber}", fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(16.dp))

            // Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onAuditDecision(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text("Approve", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onAuditDecision(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
