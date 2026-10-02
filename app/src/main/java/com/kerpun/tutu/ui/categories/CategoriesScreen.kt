package com.kerpun.tutu.ui.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.ui.common.ChevronRightIcon
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.spaces.SpaceColorPalette
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

private val typeTabs = listOf(
    TransactionType.EXPENSE to "Egresos",
    TransactionType.INCOME to "Ingresos",
    TransactionType.VAULT to "Vault",
)

private val typeOptions = listOf(
    TransactionType.EXPENSE to "Egreso",
    TransactionType.INCOME to "Ingreso",
    TransactionType.VAULT to "Vault",
)

@Composable
fun CategoriesScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val form by viewModel.formState.collectAsState()
    val colors = LocalTutuColors.current

    Box(modifier = modifier.fillMaxSize().background(colors.bg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(text = "Categorías", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.padding(top = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(state.spaceColor.toComposeColor()),
                            )
                            Text(
                                text = "${state.spaceName} · ${state.totalCount} en total",
                                color = colors.textTertiary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surface2)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClose,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "✕", color = colors.textSecondary, fontSize = 14.sp)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    typeTabs.forEach { (type, label) ->
                        UnderlineTab(
                            label = label,
                            selected = state.selectedType == type,
                            colors = colors,
                            onClick = { viewModel.selectType(type) },
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            }

            if (state.visibleRows.isEmpty()) {
                item {
                    Text(
                        text = "Todavía no hay categorías de este tipo.",
                        color = colors.textTertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            } else {
                items(state.visibleRows, key = { it.id }) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.startEditing(row) },
                            )
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(row.color.toComposeColor()),
                        )
                        Text(
                            text = row.name,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(start = 12.dp),
                        )
                        Text(
                            text = when (row.usageCount) {
                                0 -> "Sin uso"
                                1 -> "1 movimiento"
                                else -> "${row.usageCount} movimientos"
                            },
                            color = colors.textTertiary,
                            fontSize = 12.5.sp,
                        )
                        ChevronRightIcon(
                            color = colors.textFaint,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.tabBarBg)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 26.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.ink)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::startCreating,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Nueva categoría", color = colors.inkForeground, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        CategoryEditSheet(
            form = form,
            visible = form.isOpen,
            onDismiss = viewModel::closeForm,
            onNameChange = viewModel::setName,
            onTypeChange = viewModel::setType,
            onColorChange = viewModel::setColor,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            colors = colors,
        )
    }
}

@Composable
private fun UnderlineTab(label: String, selected: Boolean, colors: TutuColors, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (selected) colors.textPrimary else colors.textTertiary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(2.dp)
                .width(if (selected) 24.dp else 0.dp)
                .background(colors.accent),
        )
    }
}

@Composable
fun CategoryEditSheet(
    form: CategoryEditFormState,
    visible: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onColorChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
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
                .background(colors.bg)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (form.isEditing) "Editar categoría" else "Nueva categoría",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
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

            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 26.dp)) {
            TextField(
                value = form.name,
                onValueChange = onNameChange,
                placeholder = { Text("Nombre", color = colors.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                typeOptions.forEach { (type, label) ->
                    UnderlineTab(
                        label = label,
                        selected = form.type == type,
                        colors = colors,
                        onClick = { onTypeChange(type) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpaceColorPalette.forEach { hex ->
                    val selected = hex == form.color
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, if (selected) colors.textPrimary else Color.Transparent, CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(hex.toComposeColor())
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onColorChange(hex) },
                            ),
                    )
                }
            }

            form.errorMessage?.let { message ->
                Text(text = message, color = colors.expense, fontSize = 13.sp, modifier = Modifier.padding(top = 14.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .alpha(if (form.canSubmit) 1f else 0.5f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.ink)
                    .clickable(
                        enabled = form.canSubmit,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSave,
                    )
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (form.isEditing) "Guardar cambios" else "Crear categoría",
                    color = colors.inkForeground,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (form.isEditing) {
                Text(
                    text = "Eliminar categoría",
                    color = colors.expense,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDelete,
                        ),
                )
            }
            }
        }
        }
    }
}
