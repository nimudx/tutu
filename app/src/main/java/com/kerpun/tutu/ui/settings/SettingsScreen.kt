package com.kerpun.tutu.ui.settings

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun SettingsScreen(
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
            SettingsGroup(title = "Cuenta", colors = colors) {
                SettingsRow(label = "Perfil") { Text("Editar", color = colors.textTertiary, fontSize = 13.sp) }
                SettingsDivider(colors)
                SettingsRow(label = "Moneda") { Text(state.currencyLabel, color = colors.textTertiary, fontSize = 13.sp) }
            }
        }

        item {
            SettingsGroup(title = "Preferencias", colors = colors) {
                SettingsRow(label = "Gestionar categorías") {
                    Text(state.categoryCount.toString(), color = colors.textTertiary, fontSize = 13.sp)
                }
                SettingsDivider(colors)
                SettingsRow(label = "Notificaciones") {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.accent),
                    )
                }
                SettingsDivider(colors)
                SettingsRow(label = "Tema") {
                    ThemeSegmentedControl(
                        isDark = state.isDarkTheme,
                        onDarkSelected = { viewModel.setDarkTheme(true) },
                        onLightSelected = { viewModel.setDarkTheme(false) },
                        colors = colors,
                    )
                }
            }
        }

        item {
            SettingsGroup(title = "Datos", colors = colors) {
                SettingsRow(label = "Exportar datos") { Text("CSV", color = colors.textTertiary, fontSize = 13.sp) }
            }
        }

        item {
            SettingsGroup(title = "Acerca de", colors = colors) {
                SettingsRow(label = "Versión") { Text("1.0", color = colors.textTertiary, fontSize = 13.sp) }
                SettingsDivider(colors)
                SettingsRow(label = "Tutu") { Text("Hecho por Kerpun", color = colors.textTertiary, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, colors: TutuColors, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(text = title, color = colors.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, trailing: @Composable () -> Unit) {
    val colors = LocalTutuColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = colors.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun SettingsDivider(colors: TutuColors) {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
}

@Composable
private fun ThemeSegmentedControl(
    isDark: Boolean,
    onDarkSelected: () -> Unit,
    onLightSelected: () -> Unit,
    colors: TutuColors,
) {
    Row(
        modifier = Modifier
            .width(140.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bg)
            .padding(2.dp),
    ) {
        ThemeOption(
            label = "Oscuro",
            selected = isDark,
            accent = colors.accent,
            textColor = colors.textSecondary,
            onClick = onDarkSelected,
        )
        ThemeOption(
            label = "Claro",
            selected = !isDark,
            accent = colors.accent,
            textColor = colors.textSecondary,
            onClick = onLightSelected,
        )
    }
}

@Composable
private fun RowScope.ThemeOption(
    label: String,
    selected: Boolean,
    accent: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) colorOnAccent() else textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun colorOnAccent(): Color = LocalTutuColors.current.bg
