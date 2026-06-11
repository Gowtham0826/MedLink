package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedLinkViewModel
import kotlinx.coroutines.delay

enum class AuthScreenState {
    SPLASH,
    ROLE_SELECTION,
    PATIENT_AUTH,
    DOCTOR_AUTH
}

@Composable
fun AuthScreen(
    viewModel: MedLinkViewModel,
    onLoginSuccess: () -> Unit
) {
    var screenState by remember { mutableStateOf(AuthScreenState.SPLASH) }
    
    // We can pre-fill admin username/password if role selection goes through admin path
    var adminPreFillRequested by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = screenState,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "AuthFlowTransition"
    ) { state ->
        when (state) {
            AuthScreenState.SPLASH -> {
                SplashScreen(onFinished = {
                    screenState = AuthScreenState.ROLE_SELECTION
                })
            }
            AuthScreenState.ROLE_SELECTION -> {
                RoleSelectionScreen(
                    onSelectPatient = {
                        adminPreFillRequested = false
                        viewModel.clearAuthErrors()
                        screenState = AuthScreenState.PATIENT_AUTH
                    },
                    onSelectDoctor = {
                        adminPreFillRequested = false
                        viewModel.clearAuthErrors()
                        screenState = AuthScreenState.DOCTOR_AUTH
                    },
                    onSelectAdminPortal = {
                        adminPreFillRequested = true
                        viewModel.clearAuthErrors()
                        screenState = AuthScreenState.DOCTOR_AUTH
                    }
                )
            }
            AuthScreenState.PATIENT_AUTH -> {
                PatientAuthScreen(
                    viewModel = viewModel,
                    onBackToRoles = {
                        screenState = AuthScreenState.ROLE_SELECTION
                    },
                    onLoginSuccess = onLoginSuccess
                )
            }
            AuthScreenState.DOCTOR_AUTH -> {
                DoctorAuthScreen(
                    viewModel = viewModel,
                    preFillAdmin = adminPreFillRequested,
                    onBackToRoles = {
                        screenState = AuthScreenState.ROLE_SELECTION
                    },
                    onLoginSuccess = onLoginSuccess
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200) // 2.2 seconds splash display
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        com.example.ui.theme.PolishBg
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Pulsing branding Logo Container
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(32.dp))
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.medlink_logo),
                    contentDescription = "MedLink Secure Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "MEDLINK",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.PolishDarkSlate,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ecosystem Clinical Operation Suite",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = com.example.ui.theme.PolishSky,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(0.7f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = com.example.ui.theme.PolishAccentEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HIPAA Compliant Sandbox Environment",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.PolishDarkSlate
                )
            }
        }
    }
}

@Composable
fun RoleSelectionScreen(
    onSelectPatient: () -> Unit,
    onSelectDoctor: () -> Unit,
    onSelectAdminPortal: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.PolishBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Small Header branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.medlink_logo),
                    contentDescription = "MedLink Mini Logo",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MEDLINK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.PolishDarkSlate,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Select Portal Persona",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.PolishDarkSlate,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Please select your operational profile to enter secondary registries.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Portal Card 1: Patient
            Card(
                onClick = onSelectPatient,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("role_patient_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(com.example.ui.theme.PolishSkyLight, RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = "Patient Icon",
                            tint = com.example.ui.theme.PolishSky,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Patient Portal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PolishDarkSlate
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Access clinical consultation schedules, track active prescription registers, consult smart AI bot.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Portal Card 2: Doctor
            Card(
                onClick = onSelectDoctor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
                    .testTag("role_doctor_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Doctor Icon",
                            tint = com.example.ui.theme.SlateDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Doctor Portal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PolishDarkSlate
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Operate active patient queues, write electronic prescription slips, coordinate clinical coverage rosters.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(20.dp))

            // Easy admin compliance entryway
            Card(
                onClick = onSelectAdminPortal,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.PolishSkyLight.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentInd,
                        contentDescription = "Inspector Icon",
                        tint = com.example.ui.theme.PolishSky,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compliance Administrative Portal Entry",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.PolishSky
                    )
                }
            }
        }
    }
}

@Composable
fun PatientAuthScreen(
    viewModel: MedLinkViewModel,
    onBackToRoles: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    // Forms
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.PolishBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackToRoles,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to role select",
                        tint = com.example.ui.theme.PolishDarkSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mini Logo
            Image(
                painter = painterResource(id = R.drawable.medlink_logo),
                contentDescription = "MedLink Logo",
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(10.dp)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Patient Portal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.PolishDarkSlate
            )

            Text(
                text = if (isSignUpMode) "Register your secure clinical key" else "Access your clinical profile records",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TabRow(
                        selectedTabIndex = if (isSignUpMode) 1 else 0,
                        containerColor = Color(0xFFF1F5F9),
                        indicator = { TabRowDefaults.Indicator(color = Color.Transparent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = !isSignUpMode,
                            onClick = { 
                                isSignUpMode = false 
                                viewModel.clearAuthErrors()
                            },
                            text = { Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = com.example.ui.theme.PolishSky,
                            unselectedContentColor = Color(0xFF64748B),
                            modifier = Modifier.background(if (!isSignUpMode) Color.White else Color.Transparent)
                        )
                        Tab(
                            selected = isSignUpMode,
                            onClick = { 
                                isSignUpMode = true 
                                viewModel.clearAuthErrors()
                            },
                            text = { Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = com.example.ui.theme.PolishSky,
                            unselectedContentColor = Color(0xFF64748B),
                            modifier = Modifier.background(if (isSignUpMode) Color.White else Color.Transparent)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (authError != null) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            contentColor = Color(0xFF991B1B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = authError!!,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val textFieldsColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                        unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                        focusedBorderColor = com.example.ui.theme.PolishSky,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedLabelColor = com.example.ui.theme.PolishSky,
                        unfocusedLabelColor = Color(0xFF64748B)
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Legal Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("patient_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = textFieldsColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("patient_email_input")
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Primary Phone Contact") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("patient_phone_input")
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Access Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = textFieldsColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("patient_password_input")
                    )

                    Button(
                        onClick = {
                            if (!isSignUpMode) {
                                viewModel.login(email, password, onLoginSuccess)
                            } else {
                                viewModel.signupPatient(email, name, password, phone) {
                                    isSignUpMode = false // Switch to Sign In after signup
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("patient_submit_button")
                    ) {
                        Text(
                            text = if (isSignUpMode) "Register Account" else "Secure Verify Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSignUpMode) "Have registered credentials? Sign In" else "Need clinical folder entry? Sign Up",
                        color = com.example.ui.theme.PolishSky,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                viewModel.clearAuthErrors()
                                isSignUpMode = !isSignUpMode
                            }
                            .testTag("patient_toggle_mode")
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorAuthScreen(
    viewModel: MedLinkViewModel,
    preFillAdmin: Boolean,
    onBackToRoles: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSignedUpDoctorName by remember { mutableStateOf("") }

    // Forms
    var email by remember { mutableStateOf(if (preFillAdmin) "admin@medlink.com" else "") }
    var password by remember { mutableStateOf(if (preFillAdmin) "admin123" else "") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Doctor Specialty Forms
    var specialty by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var governmentId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.PolishBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackToRoles,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to role select",
                        tint = com.example.ui.theme.PolishDarkSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mini Logo
            Image(
                painter = painterResource(id = R.drawable.medlink_logo),
                contentDescription = "MedLink Logo",
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(10.dp)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Clinical Hub Portal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.PolishDarkSlate
            )

            Text(
                text = if (isSignUpMode) "Register your healthcare practice" else "Sign In to clinic operations dashboard",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TabRow(
                        selectedTabIndex = if (isSignUpMode) 1 else 0,
                        containerColor = Color(0xFFF1F5F9),
                        indicator = { TabRowDefaults.Indicator(color = Color.Transparent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = !isSignUpMode,
                            onClick = { 
                                isSignUpMode = false 
                                viewModel.clearAuthErrors()
                            },
                            text = { Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = com.example.ui.theme.PolishSky,
                            unselectedContentColor = Color(0xFF64748B),
                            modifier = Modifier.background(if (!isSignUpMode) Color.White else Color.Transparent)
                        )
                        Tab(
                            selected = isSignUpMode,
                            onClick = { 
                                isSignUpMode = true 
                                viewModel.clearAuthErrors()
                            },
                            text = { Text("Register", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = com.example.ui.theme.PolishSky,
                            unselectedContentColor = Color(0xFF64748B),
                            modifier = Modifier.background(if (isSignUpMode) Color.White else Color.Transparent)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (authError != null) {
                        Surface(
                            color = Color(0xFFFFE2E2),
                            contentColor = Color(0xFF991B1B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = authError!!,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val textFieldsColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.ui.theme.PolishDarkSlate,
                        unfocusedTextColor = com.example.ui.theme.PolishDarkSlate,
                        focusedBorderColor = com.example.ui.theme.PolishSky,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedLabelColor = com.example.ui.theme.PolishSky,
                        unfocusedLabelColor = Color(0xFF64748B)
                    )

                    // Core Credentials
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Doctor Full Name (e.g. Martha)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("doctor_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Clinical Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = textFieldsColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("doctor_email_input")
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Verified Contact Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("doctor_phone_input")
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Operational Secret Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = textFieldsColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (isSignUpMode) 16.dp else 20.dp)
                            .testTag("doctor_password_input")
                    )

                    // Doctor Specific Clinical Registry Verification Fields
                    if (isSignUpMode) {
                        Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Text(
                            text = "Accreditation Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.PolishSky,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = { Text("Medical Specialty Area (e.g. Cardiology)") },
                            leadingIcon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("doctor_specialty_input")
                        )

                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("Medical License String ID") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("doctor_license_input")
                        )

                        OutlinedTextField(
                            value = registrationNumber,
                            onValueChange = { registrationNumber = it },
                            label = { Text("Authorized Registration String") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("doctor_reg_input")
                        )

                        OutlinedTextField(
                            value = governmentId,
                            onValueChange = { governmentId = it },
                            label = { Text("Government Fingerprint ID Code") },
                            leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("doctor_gov_id_input")
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Live Practice Location / Coordinates") },
                            placeholder = { Text("Click GPS pin to fetch live location") },
                            leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, tint = com.example.ui.theme.PolishSky) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        location = "Saveetha Dental Medical Hub, Chennai, TN (13.0285° N, 80.2439° E)"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PinDrop,
                                        contentDescription = "Fetch Live GPS",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            },
                            singleLine = true,
                            colors = textFieldsColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("doctor_location_input")
                        )

                        Text(
                            text = "* Note: Doctor portals remain blocked from clinic list lookups until validated by administrative compliance reviews.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(bottom = 20.dp, start = 4.dp, end = 4.dp)
                        )
                    } else {
                        // Quick Pre-fill administrative Inspector helper when in login mode
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Admin Review Access:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(com.example.ui.theme.PolishBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .clickable {
                                        email = "admin@medlink.com"
                                        password = "admin123"
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.PolishSky,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Tap to autofill official Inspector credentials",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.PolishSky
                                    )
                                    Text(
                                        text = "admin@medlink.com / admin123",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!isSignUpMode) {
                                viewModel.login(email, password, onLoginSuccess)
                            } else {
                                lastSignedUpDoctorName = name
                                viewModel.signupDoctor(
                                    email = email,
                                    name = name,
                                    pass = password,
                                    specialty = specialty,
                                    license = licenseNumber,
                                    registration = registrationNumber,
                                    governmentId = governmentId,
                                    phone = phone,
                                    location = location
                                ) {
                                    showSuccessDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("doctor_submit_button")
                    ) {
                        Text(
                            text = if (isSignUpMode) "Register Account" else "Verify & Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSignUpMode) "Accredited specialist? Sign In" else "New doctor? Join MedLink",
                        color = com.example.ui.theme.PolishSky,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                viewModel.clearAuthErrors()
                                isSignUpMode = !isSignUpMode
                            }
                            .testTag("doctor_toggle_mode")
                    )
                }
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    isSignUpMode = false
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = com.example.ui.theme.PolishAccentEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Doctor Registered!",
                            color = com.example.ui.theme.PolishDarkSlate,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Congratulations, Dr. $lastSignedUpDoctorName!",
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.PolishDarkSlate,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your clinical registration request has been successfully saved to our secure, HIPAA-compliant registry.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: First-time doctor accounts enter a 'Pending' status. You can sign in using Administrator credentials (admin@medlink.com / admin123) to instantly approve your account from the Compliance Panel, or log in once approved.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = com.example.ui.theme.PolishSky
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            isSignUpMode = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PolishSky),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed to Sign In", color = Color.White)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}
