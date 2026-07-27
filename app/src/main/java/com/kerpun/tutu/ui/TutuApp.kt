package com.kerpun.tutu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.addtransaction.AddTransactionScreen
import com.kerpun.tutu.ui.addtransaction.AddTransactionViewModel
import com.kerpun.tutu.ui.common.ToastBanner
import com.kerpun.tutu.ui.common.TutuBottomBar
import com.kerpun.tutu.ui.common.TutuTab
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.home.HomeScreen
import com.kerpun.tutu.ui.home.HomeViewModel
import com.kerpun.tutu.ui.movements.MovementsScreen
import com.kerpun.tutu.ui.movements.MovementsViewModel
import com.kerpun.tutu.ui.settings.SettingsScreen
import com.kerpun.tutu.ui.settings.SettingsViewModel
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuTheme
import kotlinx.coroutines.delay

private const val SAVE_TOAST_DURATION_MS = 2_200L
private const val UNDO_TOAST_DURATION_MS = 4_000L

@Composable
fun TutuApp() {
    val homeViewModel: HomeViewModel = viewModel(factory = TutuViewModelFactory)
    val movementsViewModel: MovementsViewModel = viewModel(factory = TutuViewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = TutuViewModelFactory)
    val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = TutuViewModelFactory)

    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    TutuTheme(darkTheme = settingsState.isDarkTheme) {
        var selectedTab by remember { mutableStateOf(TutuTab.HOME) }
        var showAddSheet by remember { mutableStateOf(false) }
        var toastMessage by remember { mutableStateOf<String?>(null) }
        var toastOnUndo by remember { mutableStateOf<(() -> Unit)?>(null) }

        LaunchedEffect(addTransactionViewModel) {
            addTransactionViewModel.savedEvents.collect { message ->
                showAddSheet = false
                selectedTab = TutuTab.HOME
                toastOnUndo = null
                toastMessage = message
            }
        }

        LaunchedEffect(homeViewModel) {
            homeViewModel.toastEvents.collect { event ->
                toastOnUndo = event.onUndo
                toastMessage = event.message
            }
        }

        LaunchedEffect(movementsViewModel) {
            movementsViewModel.toastEvents.collect { event ->
                toastOnUndo = event.onUndo
                toastMessage = event.message
            }
        }

        LaunchedEffect(toastMessage) {
            if (toastMessage != null) {
                delay(if (toastOnUndo != null) UNDO_TOAST_DURATION_MS else SAVE_TOAST_DURATION_MS)
                toastMessage = null
                toastOnUndo = null
            }
        }

        val colors = LocalTutuColors.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg),
        ) {
            when (selectedTab) {
                TutuTab.HOME -> HomeScreen(
                    viewModel = homeViewModel,
                    onEditTransaction = { transaction ->
                        addTransactionViewModel.startEditing(
                            id = transaction.id,
                            type = transaction.type,
                            amount = transaction.amount,
                            categoryId = transaction.categoryId,
                            description = transaction.description,
                        )
                        showAddSheet = true
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                TutuTab.MOVEMENTS -> MovementsScreen(
                    viewModel = movementsViewModel,
                    onEditTransaction = { transaction ->
                        addTransactionViewModel.startEditing(
                            id = transaction.id,
                            type = transaction.type,
                            amount = transaction.amount,
                            categoryId = transaction.categoryId,
                            description = transaction.description,
                        )
                        showAddSheet = true
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                TutuTab.SETTINGS -> SettingsScreen(viewModel = settingsViewModel, modifier = Modifier.fillMaxSize())
            }

            toastMessage?.let { message ->
                ToastBanner(
                    message = message,
                    onUndo = toastOnUndo?.let { undo ->
                        {
                            undo()
                            toastMessage = null
                            toastOnUndo = null
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 110.dp),
                )
            }

            TutuBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddClick = {
                    addTransactionViewModel.startCreating()
                    showAddSheet = true
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            AnimatedVisibility(
                visible = showAddSheet,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize(),
            ) {
                AddTransactionScreen(
                    viewModel = addTransactionViewModel,
                    onClose = {
                        addTransactionViewModel.startCreating()
                        showAddSheet = false
                    },
                )
            }
        }
    }
}
