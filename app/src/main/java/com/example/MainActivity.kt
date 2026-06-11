package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DoctorDashboardScreen
import com.example.ui.screens.PatientDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MedLinkViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MedLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val sessionState by viewModel.currentUser.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val ignored = innerPadding // System EdgeToEdge paddings are handled in each screen
                    
                    if (!sessionState.isLoggedIn) {
                        AuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {}
                        )
                    } else {
                        when (sessionState.role) {
                            "PATIENT" -> PatientDashboardScreen(
                                viewModel = viewModel,
                                onLogout = {}
                            )
                            "DOCTOR" -> DoctorDashboardScreen(
                                viewModel = viewModel,
                                onLogout = {}
                            )
                            "ADMIN" -> AdminDashboardScreen(
                                viewModel = viewModel,
                                onLogout = {}
                            )
                            else -> AuthScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
