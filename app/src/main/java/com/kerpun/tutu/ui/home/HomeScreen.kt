package com.kerpun.tutu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.AvatarStack
import com.kerpun.tutu.ui.common.BalanceCardSkeleton
import com.kerpun.tutu.ui.common.ChevronDownIcon
import com.kerpun.tutu.ui.common.SkeletonBlock
import com.kerpun.tutu.ui.common.SwipeActionsTransactionRow
import com.kerpun.tutu.ui.common.TransactionRowSkeleton
import com.kerpun.tutu.ui.common.TransactionUi
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.rememberShimmerBrush
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.theme.LocalTutuColors

@Composable
fun HomeScreen(
    onEditTransaction: (TransactionUi) -> Unit,
    onOpenSpaces: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenApprovals: () -> Unit,
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
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenSpaces,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(state.spaceColor.toComposeColor()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.spaceName.take(1).ifEmpty { "T" }.uppercase(),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.spaceName.ifEmpty { "Tutu" },
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        ChevronDownIcon(
                            color = colors.textTertiary,
                            modifier = Modifier.padding(start = 5.dp),
                        )
                    }
                }

                if (state.memberAvatars.isNotEmpty()) {
                    AvatarStack(
                        avatars = state.memberAvatars,
                        size = 27.dp,
                        borderColor = colors.bg,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenMembers,
                            ),
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
                            vaultText = state.vaultText,
                        )
                    }
                }
            }

            if (state.summaryCards.isNotEmpty()) {
                item {
                    SummaryCarousel(
                        cards = state.summaryCards,
                        onCardClick = { card -> if (card.key == "pending") onOpenApprovals() },
                        modifier = Modifier.fillMaxWidth(),
                    )
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
private fun BalanceCard(
    balanceText: String,
    incomeText: String,
    expenseText: String,
    vaultText: String,
) {
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
            .border(1.dp, colors.cardBorder, RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Text(text = balanceText, color = colors.cardText, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text(text = "Ingresos", color = colors.cardLabel, fontSize = 12.sp)
                Text(text = incomeText, color = colors.income, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
                Text(text = "Egresos", color = colors.cardLabel, fontSize = 12.sp)
                Text(text = expenseText, color = colors.expense, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
                Text(text = "Vault", color = colors.cardLabel, fontSize = 12.sp)
                Text(text = vaultText, color = colors.cardText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
