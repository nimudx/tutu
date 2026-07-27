package com.kerpun.tutu.ui.addtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

private val keypadKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "⌫")

@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTransactionViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 30.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Nueva transacción", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            CloseButton(onClick = onClose, colors = colors)
        }

        TypeToggle(
            type = state.type,
            onTypeSelected = viewModel::setType,
            colors = colors,
        )

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "S/ ${state.amountInput.ifEmpty { "0" }}",
                color = colors.textPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            state.categories.forEach { category ->
                val selected = category.id == state.selectedCategoryId
                val categoryColor = category.color.toComposeColor()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) categoryColor else colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.selectCategory(category.id) },
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = category.icon?.let { "$it ${category.name}" } ?: category.name,
                        color = if (selected) colors.bg else colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        TextField(
            value = state.description,
            onValueChange = viewModel::setDescription,
            placeholder = { Text("Añadir descripción (opcional)", color = colors.textTertiary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp),
        )

        Keypad(onKeyPress = viewModel::pressKey, colors = colors)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent)
                .alpha(if (state.canSave) 1f else 0.5f)
                .clickable(
                    enabled = state.canSave,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = viewModel::save,
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Guardar", color = Color(0xFF04122E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, colors: TutuColors) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surface2)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "✕", color = colors.textSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun TypeToggle(type: TransactionType, onTypeSelected: (TransactionType) -> Unit, colors: TutuColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(2.dp),
    ) {
        TypeOption(
            label = "Ingreso",
            selected = type == TransactionType.INCOME,
            selectedColor = colors.income,
            textColor = colors.textSecondary,
            onClick = { onTypeSelected(TransactionType.INCOME) },
        )
        TypeOption(
            label = "Egreso",
            selected = type == TransactionType.EXPENSE,
            selectedColor = colors.expenseStrong,
            textColor = colors.textSecondary,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
        )
    }
}

@Composable
private fun RowScope.TypeOption(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF111214) else textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Keypad(onKeyPress: (String) -> Unit, colors: TutuColors) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        keypadKeys.chunked(3).forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onKeyPress(key) },
                            )
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = key, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
