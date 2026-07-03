package com.example.bluesnap

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluesnap.data.ThemeMode
import com.example.bluesnap.notification.GenerationNotifier
import com.example.bluesnap.ui.navigation.AppNavigation
import com.example.bluesnap.ui.theme.BlueSnapTheme
import com.example.bluesnap.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private var viewModelRef: MainViewModel? = null
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        GenerationNotifier(this).ensureChannel()
        requestNotificationPermissionIfNeeded()
        setContent {
            val viewModel: MainViewModel = viewModel()
            viewModelRef = viewModel
            val state by viewModel.state.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (state.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            BlueSnapTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModelRef?.setAppInForeground(true)
    }

    override fun onStop() {
        viewModelRef?.setAppInForeground(false)
        super.onStop()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
