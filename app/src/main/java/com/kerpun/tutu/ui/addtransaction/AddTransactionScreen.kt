package com.kerpun.tutu.ui.addtransaction

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.ui.categories.CategoryEditSheet
import com.kerpun.tutu.ui.categories.CategoriesViewModel
import com.kerpun.tutu.ui.common.CheckmarkIcon
import com.kerpun.tutu.ui.common.ChevronRightIcon
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
    categoriesViewModel: CategoriesViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val categoryForm by categoriesViewModel.formState.collectAsState()
    val colors = LocalTutuColors.current
    var categoryPickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(categoriesViewModel) {
        categoriesViewModel.categoryCreatedEvents.collect { created ->
            viewModel.selectCategory(created.id)
        }
    }

    val selectedCategory = state.categories.find { it.id == state.selectedCategoryId }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 30.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (state.isEditing) "Editar transacción" else "Nueva transacción",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                CloseButton(onClick = onClose, colors = colors)
            }

            TypeToggle(
                type = state.type,
                onTypeSelected = viewModel::setType,
                colors = colors,
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = state.contextText, color = colors.textSecondary, fontSize = 13.sp)
                Text(
                    text = "S/ ${state.amountInput.ifEmpty { "0" }}",
                    color = colors.textPrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Column {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { categoryPickerOpen = true },
                        )
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Categoría", color = colors.textTertiary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background((selectedCategory?.color ?: "#8A8F98").toComposeColor()),
                    )
                    Text(
                        text = selectedCategory?.name ?: "Otros",
                        color = colors.textPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                    ChevronRightIcon(color = colors.textFaint, modifier = Modifier.padding(start = 10.dp))
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                ) {
                    BasicTextField(
                        value = state.description,
                        onValueChange = viewModel::setDescription,
                        singleLine = true,
                        textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.5.sp),
                        cursorBrush = SolidColor(colors.accent),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.padding(vertical = 14.dp)) {
                                if (state.description.isEmpty()) {
                                    Text(text = "Añadir nota", color = colors.textTertiary, fontSize = 14.5.sp)
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Keypad(onKeyPress = viewModel::pressKey, colors = colors, modifier = Modifier.padding(top = 12.dp))

            state.approvalHint?.let { hint ->
                Text(
                    text = hint,
                    color = colors.textTertiary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .alpha(if (state.canSave) 1f else 0.5f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.ink)
                    .clickable(
                        enabled = state.canSave,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::save,
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (state.isEditing) "Guardar cambios" else "Guardar",
                    color = colors.inkForeground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        CategoryPickerSheet(
            visible = categoryPickerOpen,
            categories = state.categories,
            selectedCategoryId = state.selectedCategoryId,
            onSelect = { id ->
                viewModel.selectCategory(id)
                categoryPickerOpen = false
            },
            onNewCategory = {
                categoriesViewModel.startCreating(type = state.type)
                categoryPickerOpen = false
            },
            onDismiss = { categoryPickerOpen = false },
            colors = colors,
        )

        CategoryEditSheet(
            form = categoryForm,
            visible = categoryForm.isOpen,
            onDismiss = categoriesViewModel::closeForm,
            onNameChange = categoriesViewModel::setName,
            onTypeChange = categoriesViewModel::setType,
            onColorChange = categoriesViewModel::setColor,
            onSave = categoriesViewModel::save,
            onDelete = categoriesViewModel::delete,
            colors = colors,
        )
    }
}

@Composable
private fun CategoryPickerSheet(
    visible: Boolean,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Long) -> Unit,
    onNewCategory: () -> Unit,
    onDismiss: () -> Unit,
    colors: TutuColors,
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                    Text(text = "Categoría", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                        .padding(start = 20.dp, end = 20.dp, bottom = 26.dp),
                ) {
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onSelect(category.id) },
                                )
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(category.color.toComposeColor()),
                            )
                            Text(
                                text = category.name,
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(start = 12.dp),
                            )
                            if (category.id == selectedCategoryId) {
                                CheckmarkIcon(color = colors.accent)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNewCategory,
                            )
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Nueva categoría",
                            color = colors.accent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TypeOption(
            label = "Ingreso",
            selected = type == TransactionType.INCOME,
            underlineColor = colors.income,
            textColor = colors.textSecondary,
            selectedTextColor = colors.textPrimary,
            onClick = { onTypeSelected(TransactionType.INCOME) },
        )
        Box(modifier = Modifier.width(26.dp))
        TypeOption(
            label = "Egreso",
            selected = type == TransactionType.EXPENSE,
            underlineColor = colors.expenseStrong,
            textColor = colors.textSecondary,
            selectedTextColor = colors.textPrimary,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
        )
        Box(modifier = Modifier.width(26.dp))
        TypeOption(
            label = "Vault",
            selected = type == TransactionType.VAULT,
            underlineColor = colors.accent,
            textColor = colors.textSecondary,
            selectedTextColor = colors.textPrimary,
            onClick = { onTypeSelected(TransactionType.VAULT) },
        )
    }
}

@Composable
private fun TypeOption(
    label: String,
    selected: Boolean,
    underlineColor: Color,
    textColor: Color,
    selectedTextColor: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
    ) {
        Text(
            text = label,
            color = if (selected) selectedTextColor else textColor,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .height(2.dp)
                .width(if (selected) 24.dp else 0.dp)
                .background(underlineColor),
        )
    }
}

@Composable
private fun Keypad(onKeyPress: (String) -> Unit, colors: TutuColors, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        keypadKeys.chunked(3).forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
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
