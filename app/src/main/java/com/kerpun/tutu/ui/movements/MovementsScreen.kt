package com.kerpun.tutu.ui.movements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.TransactionInteractionsState
import com.kerpun.tutu.ui.common.TransactionRow
import com.kerpun.tutu.ui.common.TransactionRowSkeleton
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.rememberShimmerBrush
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun MovementsScreen(
    interactions: TransactionInteractionsState,
    modifier: Modifier = Modifier,
    viewModel: MovementsViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current
    val shimmer = rememberShimmerBrush()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "Movimientos",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.7).sp,
                modifier = Modifier.statusBarsPadding().padding(top = 22.dp, bottom = 20.dp),
            )
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    FilterTab(label = "Todos", selected = state.filter == MovementsFilter.ALL, colors = colors) {
                        viewModel.setFilter(MovementsFilter.ALL)
                    }
                    FilterTab(label = "Ingresos", selected = state.filter == MovementsFilter.INCOME, colors = colors) {
                        viewModel.setFilter(MovementsFilter.INCOME)
                    }
                    FilterTab(label = "Egresos", selected = state.filter == MovementsFilter.EXPENSE, colors = colors) {
                        viewModel.setFilter(MovementsFilter.EXPENSE)
                    }
                    FilterTab(label = "Vault", selected = state.filter == MovementsFilter.VAULT, colors = colors) {
                        viewModel.setFilter(MovementsFilter.VAULT)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(1.dp).background(colors.border))
            }
        }

        if (state.isLoading) {
            items(6) { TransactionRowSkeleton(brush = shimmer) }
        } else if (state.groups.isEmpty()) {
            item {
                Text(
                    text = "No hay movimientos para este filtro",
                    color = colors.textTertiary,
                    fontSize = 13.sp,
                )
            }
        } else {
            items(state.groups, key = { it.label }) { group ->
                Column {
                    Text(
                        text = group.label.uppercase(),
                        color = colors.textFaint,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    group.transactions.forEach { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onClick = { interactions.openDetail(transaction) },
                            onLongClick = { interactions.openMenu(transaction) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTab(label: String, selected: Boolean, colors: TutuColors, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
    ) {
        Text(
            text = label,
            color = if (selected) colors.textPrimary else colors.textTertiary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(2.dp)
                .width(if (selected) 22.dp else 0.dp)
                .background(colors.accent),
        )
    }
}
