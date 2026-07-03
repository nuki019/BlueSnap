package com.example.bluesnap.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.Screen
import com.example.bluesnap.ui.screens.ChatScreen
import com.example.bluesnap.ui.screens.HistoryScreen
import com.example.bluesnap.ui.screens.HomeScreen
import com.example.bluesnap.ui.screens.PlanScreen
import com.example.bluesnap.ui.screens.PreviewScreen
import com.example.bluesnap.ui.screens.SettingsScreen
import com.example.bluesnap.ui.screens.ShareScreen
import com.example.bluesnap.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    val showBottomBar = state.currentScreen in setOf(Screen.HOME, Screen.HISTORY, Screen.SETTINGS)

    BackHandler(enabled = state.currentScreen != Screen.HOME) {
        viewModel.handleBack()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomBar(
                    currentScreen = state.currentScreen,
                    onHistory = { viewModel.navigateTo(Screen.HISTORY) },
                    onCreate = { viewModel.navigateTo(Screen.HOME) },
                    onSettings = { viewModel.navigateTo(Screen.SETTINGS) }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

                Screen.SETTINGS -> SettingsScreen(
                    themeMode = state.themeMode,
                    systemPrompt = state.systemPrompt,
                    activeProviderLabel = state.activeProviderLabel,
                    apiKeyModeLabel = state.apiKeyModeLabel,
                    onThemeModeChange = { viewModel.updateThemeMode(it) },
                    onSystemPromptChange = { viewModel.updateSystemPrompt(it) },
                    onBackHome = { viewModel.navigateTo(Screen.HOME) }
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
                    generationStage = state.generationStage,
                    onToggleFeature = { viewModel.toggleFeature(it) },
                    onSelectLayout = { viewModel.selectLayout(it) },
                    onConfirm = { viewModel.confirmPlan() },
                    onBack = { viewModel.handleBack() }
                )

                Screen.PREVIEW -> PreviewScreen(
                    app = state.generatedApp,
                    isGenerating = state.isGenerating,
                    generationStage = state.generationStage,
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
    onHistory: () -> Unit,
    onCreate: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 10.dp),
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BottomNavTextButton(
                    selected = currentScreen == Screen.HISTORY,
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = "历史记录",
                    onClick = onHistory
                )
                Spacer(modifier = Modifier.width(58.dp))
                BottomNavTextButton(
                    selected = currentScreen == Screen.SETTINGS,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = "系统设置",
                    onClick = onSettings
                )
            }
        }

        FloatingActionButton(
            onClick = onCreate,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "创造作品")
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
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f)
    }
    ProvideTextStyle(value = MaterialTheme.typography.labelSmall.copy(color = color)) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides color,
            content = content
        )
    }
}
