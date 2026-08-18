package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSettings
import com.example.data.model.ThemeMode
import com.example.ui.components.ConfirmationDialog
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var schoolName by remember(settings) { mutableStateOf(settings.schoolName) }
    var academicYear by remember(settings) { mutableStateOf(settings.academicYear) }
    var defaultClass by remember(settings) { mutableStateOf(settings.defaultClass) }
    var defaultSection by remember(settings) { mutableStateOf(settings.defaultSection) }
    var allowFutureDates by remember(settings) { mutableStateOf(settings.allowFutureDates) }
    var themeMode by remember(settings) { mutableStateOf(settings.themeMode) }

    // Dialogs
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Institute Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Institute Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School / Institute Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("Academic Session") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = defaultClass,
                        onValueChange = { defaultClass = it },
                        label = { Text("Default Class") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = defaultSection,
                        onValueChange = { defaultSection = it },
                        label = { Text("Default Section") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val updated = settings.copy(
                            schoolName = schoolName.trim().ifEmpty { "Smart Academy" },
                            academicYear = academicYear.trim().ifEmpty { "2026-2027" },
                            defaultClass = defaultClass.trim().ifEmpty { "Class 10" },
                            defaultSection = defaultSection.trim().ifEmpty { "A" },
                            allowFutureDates = allowFutureDates,
                            themeMode = themeMode
                        )
                        viewModel.updateSettings(updated)
                        Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Changes")
                }
            }
        }

        // Attendance Rules & Theme Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("App Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                // Theme Mode Selector
                Column {
                    Text("Appearance Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = themeMode == ThemeMode.SYSTEM,
                            onClick = {
                                themeMode = ThemeMode.SYSTEM
                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.SYSTEM))
                            },
                            label = { Text("System") }
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.LIGHT,
                            onClick = {
                                themeMode = ThemeMode.LIGHT
                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.LIGHT))
                            },
                            label = { Text("Light") }
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.DARK,
                            onClick = {
                                themeMode = ThemeMode.DARK
                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.DARK))
                            },
                            label = { Text("Dark") }
                        )
                    }
                }

                // Allow Future Dates Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Future Dates Attendance", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text("Permits marking attendance for upcoming calendar dates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = allowFutureDates,
                        onCheckedChange = {
                            allowFutureDates = it
                            viewModel.updateSettings(settings.copy(allowFutureDates = it))
                        }
                    )
                }
            }
        }

        // Data Backup & Restore Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Data Backup & Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                exportedJsonText = viewModel.exportDataAsJson()
                                showExportDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON")
                    }

                    OutlinedButton(
                        onClick = {
                            importJsonText = ""
                            showImportDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import JSON")
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sample Data")
                    }

                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All")
                    }
                }
            }
        }

        // About & Legal Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About Smart Attendance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Version 1.0.0 • Offline Ready", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Privacy Policy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("100% on-device private local storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Data Backup (JSON)") },
            text = {
                Column {
                    Text("Copy this backup text to keep your student & attendance data safe:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("SmartAttendanceBackup", exportedJsonText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) {
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data Backup (JSON)") },
            text = {
                Column {
                    Text("Paste your exported Smart Attendance JSON below to restore students and logs:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Paste JSON here...") },
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isBlank()) {
                            Toast.makeText(context, "Please paste valid JSON", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.importDataFromJson(importJsonText) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) showImportDialog = false
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetDialog) {
        ConfirmationDialog(
            title = "Reset Sample Data",
            message = "This will replace the current database with sample demo students and attendance logs.",
            confirmText = "Reset Data",
            onConfirm = {
                viewModel.resetSampleData {
                    Toast.makeText(context, "Sample data restored", Toast.LENGTH_SHORT).show()
                    showResetDialog = false
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Clear All Confirmation
    if (showClearDialog) {
        ConfirmationDialog(
            title = "Clear All Records",
            message = "Are you sure you want to delete all students and attendance history? This action cannot be undone.",
            confirmText = "Clear Everything",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllData {
                    Toast.makeText(context, "All records cleared", Toast.LENGTH_SHORT).show()
                    showClearDialog = false
                }
            },
            onDismiss = { showClearDialog = false }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Smart Attendance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Smart Attendance is a modern, high-performance attendance management system built for schools, colleges, and institutes.", style = MaterialTheme.typography.bodyMedium)
                    Text("Features:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("• Student Management with full profile details\n• Mark Today and Back-Date Past Attendance\n• Daily, Monthly, Student-wise, Class-wise Reports\n• JSON Backup & Restore\n• Offline first persistence", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Smart Attendance values your privacy. All your student profiles, phone numbers, addresses, and attendance records are stored locally and securely on your device using a local SQLite database.", style = MaterialTheme.typography.bodyMedium)
                    Text("No personal data is sent to external servers without your explicit JSON export.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}
