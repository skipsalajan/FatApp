package com.skip.FatApp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties


@Composable
fun EditPersonalDetailsDialog(
    currentName: String,
    currentHeightCm: Double?,
    onDismiss: () -> Unit,
    onSave: (String, Double?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var heightText by remember {
        mutableStateOf(currentHeightCm?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personal details") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { text ->
                        if (text.isEmpty() || text.all { it.isDigit() }) {
                            heightText = text
                        }
                    },
                    label = { Text("Height") },
                    placeholder = { Text("e.g. 175") },
                    suffix = { Text("cm") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val heightCm = heightText
                        .takeIf { it.isNotBlank() }
                        ?.toDoubleOrNull()

                    onSave(name.trim(), heightCm)
                }
            ) {
                Text("Save details")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}