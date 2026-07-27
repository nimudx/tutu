package com.kerpun.tutu.ui.movements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.SwipeActionsTransactionRow
import com.kerpun.tutu.ui.common.TransactionUi
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun MovementsScreen(
    onEditTransaction: (TransactionUi) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovementsViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current
    var openTransactionId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "Movimientos",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.statusBarsPadding().padding(top = 24.dp, bottom = 18.dp),
            )
        }

        item {
            FilterSegmentedControl(
                filter = state.filter,
                onFilterSelected = viewModel::setFilter,
                colors = colors,
            )
        }

        if (state.transactions.isEmpty()) {
            item {
                Text(
                    text = "No hay movimientos para este filtro",
                    color = colors.textTertiary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            items(state.transactions, key = { it.id }) { transaction ->
                SwipeActionsTransactionRow(
                    transaction = transaction,
                    isRevealed = openTransactionId == transaction.id,
                    onRevealedChange = { revealed -> openTransactionId = if (revealed) transaction.id else null },
                    onEdit = {
                        openTransactionId = null
                        onEditTransaction(transaction)
                    },
                    onDelete = {
                        openTransactionId = null
                        viewModel.deleteTransaction(transaction)
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterSegmentedControl(
    filter: MovementsFilter,
    onFilterSelected: (MovementsFilter) -> Unit,
    colors: TutuColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(2.dp),
    ) {
        SegmentedOption(
            label = "Todos",
            selected = filter == MovementsFilter.ALL,
            selectedBg = colors.accent,
            selectedText = colors.bg,
            unselectedText = colors.textSecondary,
            onClick = { onFilterSelected(MovementsFilter.ALL) },
        )
        SegmentedOption(
            label = "Ingreso",
            selected = filter == MovementsFilter.INCOME,
            selectedBg = colors.income,
            selectedText = colors.bg,
            unselectedText = colors.textSecondary,
            onClick = { onFilterSelected(MovementsFilter.INCOME) },
        )
        SegmentedOption(
            label = "Egreso",
            selected = filter == MovementsFilter.EXPENSE,
            selectedBg = colors.expenseStrong,
            selectedText = Color.White,
            unselectedText = colors.textSecondary,
            onClick = { onFilterSelected(MovementsFilter.EXPENSE) },
        )
    }
}

@Composable
private fun RowScope.SegmentedOption(
    label: String,
    selected: Boolean,
    selectedBg: Color,
    selectedText: Color,
    unselectedText: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) selectedBg else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) selectedText else unselectedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}
