package com.kerpun.tutu.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerpun.tutu.data.model.TransactionStatus
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.ui.theme.LocalTutuColors

@Composable
fun TransactionDetailSheet(
    transaction: TransactionUi,
    visible: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTutuColors.current
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .background(colors.bg)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Detalle", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surface2)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "✕", color = colors.textSecondary, fontSize = 14.sp)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 26.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(colors.surface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = transaction.initial, color = colors.textSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                text = transaction.label,
                                color = colors.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = transaction.dateLabel,
                                color = colors.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }

                    Text(
                        text = transaction.amountText,
                        color = transaction.amountColor.toComposeColor(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp),
                    )

                    Column(modifier = Modifier.padding(top = 18.dp)) {
                        DetailFact(label = "Tipo", value = transaction.type.displayName(), labelColor = colors.textTertiary, valueColor = colors.textPrimary, border = colors.border)
                        DetailFact(label = "Categoría", value = transaction.categoryName, labelColor = colors.textTertiary, valueColor = colors.textPrimary, border = colors.border)
                        transaction.authorLabel?.let { author ->
                            DetailFact(label = "Anotado por", value = author, labelColor = colors.textTertiary, valueColor = colors.textPrimary, border = colors.border)
                        }
                        DetailFact(label = "Estado", value = transaction.status.displayName(), labelColor = colors.textTertiary, valueColor = colors.textPrimary, border = colors.border)
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.ink)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onEdit,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "Editar", color = colors.inkForeground, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDelete,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            TrashIcon(color = colors.expenseStrong)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailFact(label: String, value: String, labelColor: Color, valueColor: Color, border: Color) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(border))
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp)) {
            Text(text = label, color = labelColor, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(text = value, color = valueColor, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun TransactionType.displayName(): String = when (this) {
    TransactionType.INCOME -> "Ingreso"
    TransactionType.EXPENSE -> "Egreso"
    TransactionType.VAULT -> "Vault"
}

private fun TransactionStatus.displayName(): String = when (this) {
    TransactionStatus.APPROVED -> "Aprobado"
    TransactionStatus.PENDING -> "Pendiente de aprobación"
    TransactionStatus.REJECTED -> "Rechazado"
}
