package com.kerpun.tutu.ui.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.AvatarStack
import com.kerpun.tutu.ui.common.ChevronDownIcon
import com.kerpun.tutu.ui.common.SkeletonBlock
import com.kerpun.tutu.ui.common.TransactionInteractionsState
import com.kerpun.tutu.ui.common.TransactionRow
import com.kerpun.tutu.ui.common.TransactionRowSkeleton
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.rememberShimmerBrush
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.theme.LocalTutuColors

@Composable
fun HomeScreen(
    interactions: TransactionInteractionsState,
    onOpenSpaces: () -> Unit,
    onOpenMembers: () -> Unit,
    onGoMovements: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current
    val shimmer = rememberShimmerBrush()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                item { SkeletonBlock(modifier = Modifier.fillMaxWidth(0.35f).height(11.dp), brush = shimmer) }
                item { SkeletonBlock(modifier = Modifier.fillMaxWidth(0.7f).height(44.dp), brush = shimmer, shape = RoundedCornerShape(10.dp)) }
                item {
                    Text(
                        text = "Movimientos",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(4) { TransactionRowSkeleton(brush = shimmer) }
            } else {
                item {
                    Column {
                        Text(text = "Disponible", color = colors.textTertiary, fontSize = 12.sp)
                        Text(
                            text = state.balanceText,
                            color = colors.textPrimary,
                            fontSize = 44.sp,
                            lineHeight = 50.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = state.trendText,
                            color = state.trendColor ?: colors.textTertiary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = state.monthLabel,
                            color = colors.textTertiary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.surface2),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            if (state.incomeShare > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(state.incomeShare)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.income),
                                )
                            }
                            if (state.expenseShare > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(state.expenseShare)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.expense),
                                )
                            }
                        }
                        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                            Column {
                                Text(text = "Ingresos", color = colors.textTertiary, fontSize = 12.sp)
                                Text(
                                    text = state.incomeText,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Column {
                                Text(text = "Egresos", color = colors.textTertiary, fontSize = 12.sp)
                                Text(
                                    text = state.expenseText,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }

                if (state.hasVault) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Vault", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "Guardado aparte",
                                        color = colors.textTertiary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 1.dp),
                                    )
                                }
                                Text(
                                    text = state.vaultText,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(text = "Movimientos", color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Ver todos",
                                color = colors.accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onGoMovements,
                                ),
                            )
                        }
                    }
                }

                if (state.recentGroups.isEmpty()) {
                    item {
                        Text(text = "Aún no tienes movimientos", color = colors.textTertiary, fontSize = 13.sp)
                    }
                } else {
                    items(state.recentGroups, key = { it.label }) { group ->
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
}
