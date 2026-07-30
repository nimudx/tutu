package com.kerpun.tutu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.R
import com.kerpun.tutu.ui.common.BalanceCardSkeleton
import com.kerpun.tutu.ui.common.SkeletonBlock
import com.kerpun.tutu.ui.common.SwipeActionsTransactionRow
import com.kerpun.tutu.ui.common.TransactionRowSkeleton
import com.kerpun.tutu.ui.common.TransactionUi
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.rememberShimmerBrush
import com.kerpun.tutu.ui.theme.LocalTutuColors

@Composable
fun HomeScreen(
    onEditTransaction: (TransactionUi) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current
    var openTransactionId by remember { mutableStateOf<Long?>(null) }
    val shimmer = rememberShimmerBrush()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tutu_isotype),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(26.dp),
                )
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Tutu",
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        if (state.isLoading) {
            item { BalanceCardSkeleton(brush = shimmer) }
            item {
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(0.7f).height(34.dp),
                    brush = shimmer,
                    shape = RoundedCornerShape(16.dp),
                )
            }
            item {
                Text(
                    text = "Movimientos recientes",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(4) { TransactionRowSkeleton(brush = shimmer) }
        } else {
            item {
                Column {
                    Text(text = "Saldo disponible", color = colors.textSecondary, fontSize = 13.sp)
                    Box(modifier = Modifier.padding(top = 6.dp)) {
                        BalanceCard(
                            balanceText = state.balanceText,
                            incomeText = state.incomeText,
                            expenseText = state.expenseText,
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(text = state.insightText, color = colors.textSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            item {
                Text(
                    text = "Movimientos recientes",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    Text(text = "Aún no tienes movimientos", color = colors.textTertiary, fontSize = 13.sp)
                }
            } else {
                items(state.recentTransactions, key = { it.id }) { transaction ->
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
}

@Composable
private fun BalanceCard(balanceText: String, incomeText: String, expenseText: String) {
    val colors = LocalTutuColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.accent.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Text(text = balanceText, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text(text = "Ingresos", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                Text(text = incomeText, color = colors.income, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
                Text(text = "Egresos", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                Text(text = expenseText, color = colors.expense, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
