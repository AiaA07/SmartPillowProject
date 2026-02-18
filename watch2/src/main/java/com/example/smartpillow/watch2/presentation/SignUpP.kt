package com.example.smartpillow.watch2.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*

@Composable
fun SignUpScreen(
    onSignUpClick: (String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.title2,
                    color = MaterialTheme.colors.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            item {
                SignUpTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            item {
                SignUpTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            item {
                SignUpTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactChip(
                        onClick = onBackClick,
                        label = { Text("Back", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = ChipDefaults.secondaryChipColors()
                    )
                    CompactChip(
                        onClick = { onSignUpClick(username, password, confirmPassword) },
                        label = { Text("Sign Up", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }
        }
    }
}

@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(40.dp)
            .background(
                MaterialTheme.colors.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        textStyle = TextStyle(
            color = MaterialTheme.colors.onSurface,
            fontSize = 14.sp
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}
