package com.example.todeveu.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.todeveu.ui.HistoryScreen
import com.example.todeveu.ui.EnrollmentScreen
import com.example.todeveu.ui.MainScreen
import com.example.todeveu.ui.SettingsScreen
import com.example.todeveu.ui.MainViewModel

sealed class Screen {
    data object Main : Screen()
    data object Enrollment : Screen()
    data object Settings : Screen()
    data object History : Screen()
}

@Composable
fun NoShoutNav(
    viewModel: MainViewModel,
    onOpenBatterySettings: () -> Unit,
) {
    var current by remember { mutableStateOf<Screen>(Screen.Main) }
    when (current) {
        is Screen.Main -> MainScreen(
            viewModel = viewModel,
            onEnrollment = { current = Screen.Enrollment },
            onSettings = { current = Screen.Settings },
            onHistory = { current = Screen.History },
        )
        is Screen.Enrollment -> EnrollmentScreen(
            viewModel = viewModel,
            onBack = { current = Screen.Main },
        )
        is Screen.Settings -> SettingsScreen(
            viewModel = viewModel,
            onOpenBatterySettings = onOpenBatterySettings,
            onBack = { current = Screen.Main },
        )
        is Screen.History -> HistoryScreen(
            viewModel = viewModel,
            onBack = { current = Screen.Main },
        )
    }
}
