package com.kerpun.tutu.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    onOpenSpaces: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenApprovals: () -> Unit,
    onOpenCategories: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 140.dp),
    ) {
        item {
            Text(
                text = "Ajustes",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.statusBarsPadding().padding(top = 24.dp, bottom = 18.dp),
            )
        }

        item {
            SettingsGroup(title = "Espacio", colors = colors) {
                SettingsRow(label = state.spaceName.ifEmpty { "Sin espacio" }, onClick = onOpenSpaces) {
                    Text("Cambiar", color = colors.textTertiary, fontSize = 13.sp)
                }
                SettingsRow(label = "Miembros", onClick = onOpenMembers) {
                    Text("Ver", color = colors.textTertiary, fontSize = 13.sp)
                }
                SettingsRow(label = "Aprobaciones", onClick = onOpenApprovals) {
                    Text(
                        state.approvalsHint,
                        color = if (state.approvalsNeedsAttention) colors.pending else colors.textTertiary,
                        fontSize = 13.sp,
                    )
                }
                SettingsRow(label = "Categorías", onClick = onOpenCategories) {
                    Text(state.categoryCount.toString(), color = colors.textTertiary, fontSize = 13.sp)
                }
                SettingsRow(label = "Moneda") { Text(state.currencyLabel, color = colors.textTertiary, fontSize = 13.sp) }
            }
        }

        item {
            SettingsGroup(title = "Cuenta", colors = colors) {
                SettingsRow(label = "Perfil") { Text("Editar", color = colors.textTertiary, fontSize = 13.sp) }
                SettingsRow(label = "Notificaciones") {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.accent),
                    )
                }
                SettingsRow(label = "Tema oscuro") {
                    Switch(
                        checked = state.isDarkTheme,
                        onCheckedChange = viewModel::setDarkTheme,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.accent),
                    )
                }
                SettingsRow(label = "Cerrar sesión", onClick = onSignOut, destructive = true) {}
            }
        }

        item {
            SettingsGroup(title = "Datos", colors = colors) {
                SettingsRow(label = "Exportar datos") { Text("CSV", color = colors.textTertiary, fontSize = 13.sp) }
            }
        }

        item {
            SettingsGroup(title = "Acerca de", colors = colors) {
                SettingsRow(label = "Versión") { Text("1.1", color = colors.textTertiary, fontSize = 13.sp) }
                SettingsRow(label = "Tutu") { Text("Hecho por Kerpun", color = colors.textTertiary, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, colors: TutuColors, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 28.dp)) {
        Text(
            text = title.uppercase(),
            color = colors.textFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        content()
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: (() -> Unit)? = null,
    destructive: Boolean = false,
    trailing: @Composable () -> Unit,
) {
    val colors = LocalTutuColors.current
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { base ->
                    if (onClick != null) {
                        base.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick,
                        )
                    } else {
                        base
                    }
                }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = if (destructive) colors.expense else colors.textPrimary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
    }
}
