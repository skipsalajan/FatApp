package com.skip.FatApp.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.skip.FatApp.data.steps.StepCounterManager

private val DarkText = Color(0xFF19312F)
private val PrimaryTeal = Color(0xFF19766C)
private val AccentTeal = Color(0xFFDCEEE9)
private val BorderColor = Color(0xFFDCE7E4)
private val MutedText = Color(0xFF6B7D7A)

@Composable
fun SettingsDialog(
    entries: List<com.skip.FatApp.data.WeightEntry>,
    stepTrackingEnabled: Boolean,
    onSetStepTrackingEnabled: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = remember { BackupManager(context) }
    val profileManager = remember { UserProfileManager(context) }

    val stepCounterManager = remember {
        StepCounterManager(context)
    }

    var profile by remember { mutableStateOf(profileManager.loadProfile()) }
    var showPersonalDetails by remember { mutableStateOf(false) }

    val importMessage = remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = backupManager.importBackupFile(uri)
                println("IMPORT RESULT: ${result.first} – ${result.second}")
                importMessage.value = result.second
            }
        }
    }

    if (showPersonalDetails) {
        EditPersonalDetailsDialog(
            currentName = profile.name,
            currentHeightCm = profile.heightCm,
            onDismiss = {
                showPersonalDetails = false
            },
            onSave = { name, heightCm ->
                profileManager.saveProfile(name, heightCm)
                profile = profileManager.loadProfile()
                showPersonalDetails = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                BorderColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                SettingsHeader()

                Spacer(modifier = Modifier.height(20.dp))

                PersonalDetailsSection(
                    name = profile.name,
                    heightCm = profile.heightCm,
                    onEdit = {
                        showPersonalDetails = true
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                val message = when {
                    stepTrackingEnabled -> "Step tracking is active"
                    else -> "Allow Activity Recognition to track your steps."
                }

                StepTrackingSection(
                    enabled = stepTrackingEnabled,
                    message = message,
                    onToggle = { requestedEnabled ->
                        onSetStepTrackingEnabled(requestedEnabled)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = BorderColor)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Backup & restore",
                    color = DarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsContent(
                    onExport = {
                        println("EXPORT CLICKED")

                        scope.launch {
                            println("EXPORT LAUNCHED, entries count = ${entries.size}")

                            val uri = backupManager.exportAllWeights(entries)

                            println("EXPORT URI = $uri")

                            if (uri != null) {
                                backupManager.shareFile(uri)
                            }
                        }
                    },
                    onImport = {
                        importLauncher.launch("application/json")
                    }
                )

                val currentImportMessage = importMessage.value

                if (currentImportMessage != null) {
                    Text(
                        text = currentImportMessage,
                        color = MutedText,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Close",
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AccentTeal),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(20.dp)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Settings",
        color = DarkText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Personal details, backup & restore",
        color = MutedText,
        fontSize = 13.sp
    )
}

@Composable
private fun PersonalDetailsSection(
    name: String,
    heightCm: Double?,
    onEdit: () -> Unit
) {
    Text(
        text = "Personal details",
        color = DarkText,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = when {
            name.isNotBlank() && heightCm != null ->
                "$name • ${heightCm.toInt()} cm"

            name.isNotBlank() ->
                "$name • No height set"

            heightCm != null ->
                "${heightCm.toInt()} cm"

            else ->
                "Add your name and height"
        },
        color = MutedText,
        fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(13.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Edit personal details",
            color = PrimaryTeal,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsContent(
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Button(
        onClick = onExport,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryTeal,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Backup,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Export backup",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onImport,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryTeal
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Import backup",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepTrackingSection(
    enabled: Boolean,
    message: String?,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Step tracking",
                    color = DarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Track steps using this phone's step counter.",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryTeal,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = BorderColor,
                    uncheckedBorderColor = BorderColor
                )
            )
        }

        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = if (enabled) PrimaryTeal else MutedText,
                fontSize = 12.sp
            )
        }
    }
}