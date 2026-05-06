package com.example.bluesnap.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.bluesnap.data.Screen
import com.example.bluesnap.ui.screens.*
import com.example.bluesnap.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    AnimatedContent(
        targetState = state.currentScreen,
        transitionSpec = {
            fadeIn() + slideInHorizontally { it / 3 } togetherWith
            fadeOut() + slideOutHorizontally { -it / 3 }
        },
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            Screen.HOME -> HomeScreen(
                onSendMessage = { viewModel.sendMessage(it) },
                onTemplateClick = { viewModel.startFromTemplate(it) },
                onNavigateToChat = { viewModel.navigateTo(Screen.CHAT) }
            )

            Screen.CHAT -> ChatScreen(
                messages = state.messages,
                isGenerating = state.isGenerating,
                streamingContent = state.streamingContent,
                onSendMessage = { viewModel.sendMessage(it) },
                onViewPlan = { viewModel.generatePlan() },
                onBack = { viewModel.navigateTo(Screen.HOME) }
            )

            Screen.PLAN -> PlanScreen(
                plan = state.currentPlan,
                isGenerating = state.isGenerating,
                onToggleFeature = { viewModel.toggleFeature(it) },
                onSelectLayout = { viewModel.selectLayout(it) },
                onConfirm = { viewModel.confirmPlan() },
                onBack = { viewModel.navigateTo(Screen.CHAT) }
            )

            Screen.PREVIEW -> PreviewScreen(
                app = state.generatedApp,
                isGenerating = state.isGenerating,
                onFeedback = { viewModel.iterateWithFeedback(it) },
                onBack = { viewModel.navigateTo(Screen.HOME) }
            )
        }
    }
}
