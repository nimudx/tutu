package com.kerpun.tutu.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.R
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        BrandIcon()

        Text(
            text = "Tutu",
            color = colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = "Las cuentas de la casa, en un solo lugar.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 30.dp),
        )

        TutuTextField(
            value = state.email,
            onValueChange = viewModel::setEmail,
            placeholder = "Correo",
            colors = colors,
        )
        Spacer(modifier = Modifier.height(10.dp))
        TutuTextField(
            value = state.password,
            onValueChange = viewModel::setPassword,
            placeholder = "Contraseña",
            colors = colors,
            isPassword = true,
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = colors.expense,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
        state.infoMessage?.let { message ->
            Text(
                text = message,
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .alpha(if (state.canSubmit) 1f else 0.5f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent)
                .clickable(
                    enabled = state.canSubmit,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = viewModel::submit,
                )
                .padding(vertical = 16.dp),
        ) {
            Text(
                text = if (state.mode == LoginMode.SIGN_IN) "Entrar" else "Crear cuenta",
                color = Color(0xFF04122E),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (state.mode == LoginMode.SIGN_IN) "¿No tienes cuenta?" else "¿Ya tienes cuenta?",
                color = colors.textTertiary,
                fontSize = 13.sp,
            )
            Text(
                text = if (state.mode == LoginMode.SIGN_IN) "Crear cuenta" else "Inicia sesión",
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::toggleMode,
                    ),
            )
        }
    }
}

@Composable
private fun BrandIcon() {
    Icon(
        painter = painterResource(id = R.drawable.ic_tutu_isotype),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(40.dp),
    )
}

@Composable
private fun TutuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colors: TutuColors,
    isPassword: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = colors.textTertiary) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp),
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
}
