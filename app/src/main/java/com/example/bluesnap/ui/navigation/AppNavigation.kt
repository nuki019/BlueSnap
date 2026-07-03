package com.example.bluesnap.ui.navigation

import androidx.compose.animation.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.Screen
import com.example.bluesnap.ui.screens.*
import com.example.bluesnap.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    val showBottomBar = state.currentScreen == Screen.HOME || state.currentScreen == Screen.HISTORY

    BackHandler(enabled = state.currentScreen != Screen.HOME) {
        viewModel.handleBack()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomBar(
                    currentScreen = state.currentScreen,
                    onHome = { viewModel.navigateTo(Screen.HOME) },
                    onHistory = { viewModel.navigateTo(Screen.HISTORY) }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.currentScreen,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it / 3 } togetherWith
                    fadeOut() + slideOutHorizontally { -it / 3 }
            },
            label = "screenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                Screen.HOME -> HomeScreen(
                    onSendMessage = { viewModel.sendMessage(it) },
                    onTemplateClick = { viewModel.startFromTemplate(it) },
                    onNavigateToChat = { viewModel.navigateTo(Screen.CHAT) }
                )

                Screen.HISTORY -> HistoryScreen(
                    apps = state.savedApps,
                    onOpen = { viewModel.openApp(it) },
                    onShare = { viewModel.shareApp(it) },
                    onDelete = { viewModel.deleteApp(it) },
                    onCreate = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.CHAT -> ChatScreen(
                    messages = state.messages,
                    isGenerating = state.isGenerating,
                    streamingContent = state.streamingContent,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onViewPlan = { viewModel.generatePlan() },
                    onBack = { viewModel.handleBack() }
                )

                Screen.PLAN -> PlanScreen(
                    plan = state.currentPlan,
                    isGenerating = state.isGenerating,
                    onToggleFeature = { viewModel.toggleFeature(it) },
                    onSelectLayout = { viewModel.selectLayout(it) },
                    onConfirm = { viewModel.confirmPlan() },
                    onBack = { viewModel.handleBack() }
                )

                Screen.PREVIEW -> PreviewScreen(
                    app = state.generatedApp,
                    isGenerating = state.isGenerating,
                    onFeedback = { viewModel.iterateWithFeedback(it) },
                    onSharePage = { viewModel.navigateTo(Screen.SHARE) },
                    onBack = { viewModel.handleBack() }
                )

                Screen.SHARE -> ShareScreen(
                    app = state.generatedApp,
                    onBack = { viewModel.handleBack() },
                    onReturnHome = { viewModel.navigateTo(Screen.HOME) }
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomBar(
    currentScreen: Screen,
    onHome: () -> Unit,
    onHistory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 42.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BottomNavTextButton(
                    selected = currentScreen == Screen.HOME,
                    icon = { Icon(Icons.Filled.AddCircleOutline, contentDescription = null) },
                    label = "??",
                    onClick = onHome
                )
                Spacer(modifier = Modifier.width(52.dp))
                BottomNavTextButton(
                    selected = currentScreen == Screen.HISTORY,
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = "??",
                    onClick = onHistory
                )
            }
        }
        FloatingActionButton(
            onClick = onHome,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .size(54.dp),
            shape = CircleShape,
            containerColor = Color(0xFF0B7CFF),
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "????")
        }
    }
}

@Composable
private fun BottomNavTextButton(
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CompositionLocalProviderTextColor(selected = selected) {
                icon()
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CompositionLocalProviderTextColor(
    selected: Boolean,
    content: @Composable () -> Unit
) {
    val color = if (selected) Color(0xFF0B7CFF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f)
    androidx.compose.material3.ProvideTextStyle(
        value = MaterialTheme.typography.labelSmall.copy(color = color)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides color,
            content = content
        )
    }
}
