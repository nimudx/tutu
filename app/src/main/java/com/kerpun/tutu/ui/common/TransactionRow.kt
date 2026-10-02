package com.kerpun.tutu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerpun.tutu.data.model.TransactionStatus
import com.kerpun.tutu.ui.theme.LocalTutuColors

/** Tap opens the detail sheet, long-press (or right-click on desktop) opens the quick action menu. */
@Composable
fun TransactionRow(
    transaction: TransactionUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTutuColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = transaction.initial,
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = transaction.label,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subLabel = transaction.subLabelText()
            if (subLabel != null) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val dotColor = when (transaction.status) {
                        TransactionStatus.PENDING -> colors.pending
                        TransactionStatus.REJECTED -> colors.expense
                        TransactionStatus.APPROVED -> null
                    }
                    if (dotColor != null) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotColor))
                        Box(modifier = Modifier.width(5.dp))
                    }
                    Text(text = subLabel, color = colors.textTertiary, fontSize = 12.sp)
                }
            }
        }
        Text(
            text = transaction.amountText,
            color = transaction.amountColor.toComposeColor(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
