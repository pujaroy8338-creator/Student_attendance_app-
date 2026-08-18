package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.AttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.components.showAndroidDatePicker
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: AttendanceViewModel,
    onEditAttendance: (date: String, studentClass: String, section: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val availableClasses by viewModel.availableClasses.collectAsStateWithLifecycle()
    val availableSections by viewModel.availableSections.collectAsStateWithLifecycle()

    var historyDate by remember { mutableStateOf(viewModel.todayDateString) }
    var historyClass by remember { mutableStateOf("Class 10") }
    var historySection by remember { mutableStateOf("A") }

    // Format display date
    val displayDateFormatted = remember(historyDate) {
        try {
            val d = viewModel.dateFormat.parse(historyDate)
            if (d != null) viewModel.displayDateFormat.format(d) else historyDate
        } catch (e: Exception) {
            historyDate
        }
    }

    val attendanceRecords by viewModel.getDailyReportForDate(historyDate).collectAsStateWithLifecycle(emptyList())

    val filteredRecords = remember(attendanceRecords, historyClass, historySection) {
        attendanceRecords.filter {
            it.studentClass.equals(historyClass, ignoreCase = true) &&
                    it.section.equals(historySection, ignoreCase = true)
        }
    }

    val studentsMap = remember(allStudents) {
        allStudents.associateBy { it.id }
    }

    val presentCount = filteredRecords.count { it.status.equals("PRESENT", ignoreCase = true) }
    val absentCount = filteredRecords.count { it.status.equals("ABSENT", ignoreCase = true) }
    val leaveCount = filteredRecords.count { it.status.equals("LEAVE", ignoreCase = true) }
    val totalCount = filteredRecords.size
    val percentage = if (totalCount > 0) (presentCount.toFloat() / totalCount) * 100f else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Date Navigation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_date_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    try {
                        val parsed = viewModel.dateFormat.parse(historyDate)
                        if (parsed != null) cal.time = parsed
                    } catch (e: Exception) {}
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    historyDate = viewModel.dateFormat.format(cal.time)
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Day")
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            showAndroidDatePicker(
                                context = context,
                                currentDateStr = historyDate,
                                allowFutureDates = settings.allowFutureDates
                            ) { picked ->
                                historyDate = picked
                            }
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = displayDateFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📅 Tap to select date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        try {
                            val parsed = viewModel.dateFormat.parse(historyDate)
                            if (parsed != null) cal.time = parsed
                        } catch (e: Exception) {}
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val nextDateStr = viewModel.dateFormat.format(cal.time)
                        if (settings.allowFutureDates || nextDateStr <= viewModel.todayDateString) {
                            historyDate = nextDateStr
                        }
                    },
                    enabled = settings.allowFutureDates || historyDate < viewModel.todayDateString
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Class & Section Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            availableClasses.forEach { cls ->
                FilterChip(
                    selected = historyClass == cls,
                    onClick = { historyClass = cls },
                    label = { Text(cls) }
                )
            }
            availableSections.forEach { sec ->
                FilterChip(
                    selected = historySection == sec,
                    onClick = { historySection = sec },
                    label = { Text("Sec $sec") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary Card for this History Date
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$historyClass - Sec $historySection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (totalCount > 0) {
                        Text(
                            text = "${String.format("%.1f", percentage)}% Attendance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniHistoryStat("Present", presentCount.toString(), StatusPresent)
                    MiniHistoryStat("Absent", absentCount.toString(), StatusAbsent)
                    MiniHistoryStat("Leave", leaveCount.toString(), StatusLeave)
                    MiniHistoryStat("Total", totalCount.toString(), MaterialTheme.colorScheme.onSurface)
                }

                if (totalCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onEditAttendance(historyDate, historyClass, historySection)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_this_attendance_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit This Date's Attendance")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Attendance Records List
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No attendance recorded for this date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onEditAttendance(historyDate, historyClass, historySection)
                        },
                        modifier = Modifier.testTag("mark_now_button")
                    ) {
                        Text("Mark Attendance Now")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    val student = studentsMap[record.studentId]
                    val statusEnum = try {
                        AttendanceStatus.valueOf(record.status.uppercase(Locale.ROOT))
                    } catch (e: Exception) {
                        AttendanceStatus.PRESENT
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudentAvatar(
                                name = student?.name ?: "Student",
                                colorHex = student?.avatarColorHex ?: "#3F51B5",
                                size = 42.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student?.name ?: "Unknown Student",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Roll: ${student?.rollNumber ?: "-"}  •  Adm: ${student?.admissionNumber?.ifEmpty { "-" } ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            StatusBadge(status = statusEnum)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniHistoryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
