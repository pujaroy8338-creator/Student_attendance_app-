package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.local.StudentEntity
import com.example.data.model.AttendanceStatus
import com.example.ui.components.AttendanceSegmentedControl
import com.example.ui.components.StudentAvatar
import com.example.ui.components.showAndroidDatePicker
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedClass by viewModel.selectedClass.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val attendanceMap by viewModel.currentAttendanceMap.collectAsStateWithLifecycle()
    val isSavedInDb by viewModel.isSavedInDb.collectAsStateWithLifecycle()

    val availableClasses by viewModel.availableClasses.collectAsStateWithLifecycle()
    val availableSections by viewModel.availableSections.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()

    val currentClassStudents = remember(allStudents, selectedClass, selectedSection) {
        allStudents.filter {
            it.studentClass.equals(selectedClass, ignoreCase = true) &&
                    it.section.equals(selectedSection, ignoreCase = true) &&
                    !it.status.equals("Inactive", ignoreCase = true)
        }.sortedWith(compareBy<StudentEntity> { it.rollNumber.toIntOrNull() ?: 9999 }.thenBy { it.rollNumber })
    }

    // Format display date
    val displayDateFormatted = remember(selectedDate) {
        try {
            val d = viewModel.dateFormat.parse(selectedDate)
            if (d != null) viewModel.displayDateFormat.format(d) else selectedDate
        } catch (e: Exception) {
            selectedDate
        }
    }

    val isToday = selectedDate == viewModel.todayDateString

    // Summary counts in memory
    val presentCount = attendanceMap.values.count { it == AttendanceStatus.PRESENT }
    val absentCount = attendanceMap.values.count { it == AttendanceStatus.ABSENT }
    val leaveCount = attendanceMap.values.count { it == AttendanceStatus.LEAVE }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$presentCount Present  •  $absentCount Absent",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSavedInDb) "Saved in database" else "Not yet saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSavedInDb) StatusPresent else MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveCurrentAttendance(
                                onSuccess = { count ->
                                    Toast.makeText(context, "Saved attendance for $count students on $selectedDate", Toast.LENGTH_SHORT).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("save_attendance_button")
                    ) {
                        Icon(
                            if (isSavedInDb) Icons.Default.CheckCircle else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSavedInDb) "Update Attendance" else "Save Attendance")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Prominent Date Selector Card with Back Date Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isToday) "Today's Attendance" else "Back-Date Attendance",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayDateFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Status badge for this date
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSavedInDb) StatusPresent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isSavedInDb) "✓ Saved" else "● Pending",
                                color = if (isSavedInDb) StatusPresent else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Quick-Pill Row + Back Date Picker Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Today Pill
                        FilterChip(
                            selected = isToday,
                            onClick = { viewModel.selectDate(viewModel.todayDateString) },
                            label = { Text("Today") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )

                        // Yesterday Pill
                        val yesterdayStr = remember {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            viewModel.dateFormat.format(cal.time)
                        }
                        FilterChip(
                            selected = selectedDate == yesterdayStr,
                            onClick = { viewModel.selectDate(yesterdayStr) },
                            label = { Text("Yesterday") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )

                        // 📅 Back Date Attendance Picker Button
                        OutlinedButton(
                            onClick = {
                                showAndroidDatePicker(
                                    context = context,
                                    currentDateStr = selectedDate,
                                    allowFutureDates = settings.allowFutureDates
                                ) { pickedDate ->
                                    viewModel.selectDate(pickedDate)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_back_date_button")
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📅 Pick Date", fontSize = 13.sp, maxLines = 1)
                        }
                    }
                }
            }

            // Class & Section Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Class:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                availableClasses.forEach { cls ->
                    FilterChip(
                        selected = selectedClass == cls,
                        onClick = { viewModel.selectClass(cls) },
                        label = { Text(cls) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Section:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                availableSections.forEach { sec ->
                    FilterChip(
                        selected = selectedSection == sec,
                        onClick = { viewModel.selectSection(sec) },
                        label = { Text("Sec $sec") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // Quick Mark Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.markAll(AttendanceStatus.PRESENT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_present_button")
                ) {
                    Text("All Present", fontSize = 12.sp, color = StatusPresent)
                }
                OutlinedButton(
                    onClick = { viewModel.markAll(AttendanceStatus.ABSENT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_absent_button")
                ) {
                    Text("All Absent", fontSize = 12.sp, color = StatusAbsent)
                }
                OutlinedButton(
                    onClick = { viewModel.markAll(AttendanceStatus.LEAVE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_leave_button")
                ) {
                    Text("All Leave", fontSize = 12.sp, color = StatusLeave)
                }
            }

            // Students Attendance List
            if (currentClassStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students in $selectedClass - Sec $selectedSection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add students to this class in the Students tab",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Students (${currentClassStudents.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(currentClassStudents, key = { it.id }) { student ->
                        val currentStatus = attendanceMap[student.id] ?: AttendanceStatus.PRESENT

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("attendance_row_${student.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StudentAvatar(
                                    name = student.name,
                                    colorHex = student.avatarColorHex,
                                    size = 40.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Roll: ${student.rollNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                AttendanceSegmentedControl(
                                    currentStatus = currentStatus,
                                    onStatusChange = { newStatus ->
                                        viewModel.setStudentStatus(student.id, newStatus)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
