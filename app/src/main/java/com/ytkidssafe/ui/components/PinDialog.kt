package com.ytkidssafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytkidssafe.ui.theme.Error
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.Surface
import com.ytkidssafe.ui.theme.TextPrimary

@Composable
fun PinDialog(
    title: String = "Enter PIN",
    isSetup: Boolean = false,
    error: String? = null,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isSetup && isConfirming) "Confirm PIN" else title,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    val currentPin = if (isConfirming) confirmPin else pin
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < currentPin.length) Primary
                                    else Surface
                                )
                                .border(1.dp, Primary, CircleShape)
                        )
                        if (index < 3) Spacer(modifier = Modifier.width(16.dp))
                    }
                }

                // Error message
                if (error != null) {
                    Text(
                        text = error,
                        color = Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Number pad
                NumberPad(
                    onNumberClick = { number ->
                        if (isConfirming) {
                            if (confirmPin.length < 4) {
                                confirmPin += number
                                if (confirmPin.length == 4) {
                                    if (pin == confirmPin) {
                                        onPinEntered(pin)
                                    } else {
                                        confirmPin = ""
                                    }
                                }
                            }
                        } else {
                            if (pin.length < 4) {
                                pin += number
                                if (pin.length == 4) {
                                    if (isSetup) {
                                        isConfirming = true
                                    } else {
                                        onPinEntered(pin)
                                    }
                                }
                            }
                        }
                    },
                    onBackspace = {
                        if (isConfirming) {
                            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                        } else {
                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                        }
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        ).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(modifier = Modifier.size(64.dp))
                        "⌫" -> {
                            IconButton(
                                onClick = onBackspace,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = TextPrimary
                                )
                            }
                        }
                        else -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Surface)
                                    .border(1.dp, Primary.copy(alpha = 0.3f), CircleShape)
                                    .clickable { onNumberClick(key) }
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
