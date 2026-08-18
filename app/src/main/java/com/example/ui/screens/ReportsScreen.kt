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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.StudentEntity
import com.example.data.model.AttendanceStatus
import com.example.ui.components.StatusBadge
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
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Monthly", "Daily", "Student-Wise", "Class-Wise")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> MonthlyReportView(viewModel)
            1 -> DailyReportView(viewModel)
            2 -> StudentWiseReportView(viewModel)
            3 -> ClassWiseReportView(viewModel)
        }
    }
}

@Composable
fun MonthlyReportView(viewModel: AttendanceViewModel) {
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val availableClasses by viewModel.availableClasses.collectAsStateWithLifecycle()
    val availableSections by viewModel.availableSections.collectAsStateWithLifecycle()

    var selectedClass by remember { mutableStateOf("Class 10") }
    var selectedSection by remember { mutableStateOf("A") }
    var selectedYearMonth by remember { mutableStateOf("2026-08") } // YYYY-MM

    val monthlyRecords by viewModel.getMonthlyReport(selectedYearMonth, selectedClass, selectedSection)
        .collectAsStateWithLifecycle(emptyList())

    val classStudents = remember(allStudents, selectedClass, selectedSection) {
        allStudents.filter {
            it.studentClass.equals(selectedClass, ignoreCase = true) &&
                    it.section.equals(selectedSection, ignoreCase = true)
        }.sortedBy { it.rollNumber.toIntOrNull() ?: 999 }
    }

    val totalWorkingDays = remember(monthlyRecords) {
        monthlyRecords.map { it.date }.distinct().size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month, Class & Section Filter Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Month: August 2026 ($selectedYearMonth)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$totalWorkingDays Working Days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Class & Section chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableClasses.forEach { cls ->
                        FilterChip(
                            selected = selectedClass == cls,
                            onClick = { selectedClass = cls },
                            label = { Text(cls, fontSize = 12.sp) }
                        )
                    }
                    availableSections.forEach { sec ->
                        FilterChip(
                            selected = selectedSection == sec,
                            onClick = { selectedSection = sec },
                            label = { Text("Sec $sec", fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Students Monthly Attendance Table
        if (classStudents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No students in $selectedClass - Sec $selectedSection", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classStudents, key = { it.id }) { student ->
                    val studentRecords = monthlyRecords.filter { it.studentId == student.id }
                    val pDays = studentRecords.count { it.status.equals("PRESENT", ignoreCase = true) }
                    val aDays = studentRecords.count { it.status.equals("ABSENT", ignoreCase = true) }
                    val lDays = studentRecords.count { it.status.equals("LEAVE", ignoreCase = true) }
                    val percent = if (totalWorkingDays > 0) (pDays.toFloat() / totalWorkingDays) * 100f else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StudentAvatar(name = student.name, colorHex = student.avatarColorHex, size = 36.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Text(
                                    text = "${String.format("%.1f", percent)}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (percent >= 75f) StatusPresent else if (percent >= 50f) StatusLeave else StatusAbsent
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (percent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (percent >= 75f) StatusPresent else if (percent >= 50f) StatusLeave else StatusAbsent,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Present: $pDays", style = MaterialTheme.typography.labelSmall, color = StatusPresent, fontWeight = FontWeight.Bold)
                                Text("Absent: $aDays", style = MaterialTheme.typography.labelSmall, color = StatusAbsent, fontWeight = FontWeight.Bold)
                                Text("Leave: $lDays", style = MaterialTheme.typography.labelSmall, color = StatusLeave, fontWeight = FontWeight.Bold)
                                Text("Total Days: $totalWorkingDays", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyReportView(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()

    var dailyDate by remember { mutableStateOf(viewModel.todayDateString) }

    val dailyRecords by viewModel.getDailyReportForDate(dailyDate).collectAsStateWithLifecycle(emptyList())

    val totalEnrolled = allStudents.size
    val presentCount = dailyRecords.count { it.status.equals("PRESENT", ignoreCase = true) }
    val absentCount = dailyRecords.count { it.status.equals("ABSENT", ignoreCase = true) }
    val leaveCount = dailyRecords.count { it.status.equals("LEAVE", ignoreCase = true) }
    val totalMarked = dailyRecords.size
    val overallPercentage = if (totalMarked > 0) (presentCount.toFloat() / totalMarked) * 100f else 0f

    // Class-wise breakdown
    val classesBreakdown = remember(dailyRecords, allStudents) {
        val classGroups = allStudents.groupBy { "${it.studentClass}-${it.section}" }
        classGroups.map { (key, studentsInGroup) ->
            val recordsInGroup = dailyRecords.filter { "${it.studentClass}-${it.section}" == key }
            val p = recordsInGroup.count { it.status.equals("PRESENT", ignoreCase = true) }
            val a = recordsInGroup.count { it.status.equals("ABSENT", ignoreCase = true) }
            val l = recordsInGroup.count { it.status.equals("LEAVE", ignoreCase = true) }
            val tot = recordsInGroup.size
            val pct = if (tot > 0) (p.toFloat() / tot) * 100f else 0f
            ClassDailySummary(
                classSection = key,
                totalStudents = studentsInGroup.size,
                present = p,
                absent = a,
                leave = l,
                percentage = pct,
                isMarked = tot > 0
            )
        }.sortedBy { it.classSection }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date Selector Pill
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showAndroidDatePicker(
                        context = context,
                        currentDateStr = dailyDate,
                        allowFutureDates = settings.allowFutureDates
                    ) { picked -> dailyDate = picked }
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Attendance Report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dailyDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Overview Stat Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Overall Daily Rate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${String.format("%.1f", overallPercentage)}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniDailyStat("Present", presentCount.toString(), StatusPresent)
                    MiniDailyStat("Absent", absentCount.toString(), StatusAbsent)
                    MiniDailyStat("Leave", leaveCount.toString(), StatusLeave)
                    MiniDailyStat("Marked", totalMarked.toString(), MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Text("Class-Wise Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        // Class-wise Cards
        classesBreakdown.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.classSection, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        if (item.isMarked) {
                            Text(
                                "${String.format("%.1f", item.percentage)}%",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.errorContainer) {
                                Text("Not Marked", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }

                    if (item.isMarked) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Present: ${item.present}", fontSize = 12.sp, color = StatusPresent)
                            Text("Absent: ${item.absent}", fontSize = 12.sp, color = StatusAbsent)
                            Text("Leave: ${item.leave}", fontSize = 12.sp, color = StatusLeave)
                            Text("Enrolled: ${item.totalStudents}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

data class ClassDailySummary(
    val classSection: String,
    val totalStudents: Int,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val percentage: Float,
    val isMarked: Boolean
)

@Composable
fun MiniDailyStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentWiseReportView(viewModel: AttendanceViewModel) {
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    var selectedStudentId by remember { mutableStateOf<Long?>(allStudents.firstOrNull()?.id) }

    val selectedStudent = remember(allStudents, selectedStudentId) {
        allStudents.find { it.id == selectedStudentId } ?: allStudents.firstOrNull()
    }

    val attendanceLogs by (if (selectedStudent != null) viewModel.getStudentSummary(selectedStudent.id) else viewModel.getDailyReportForDate(""))
        .collectAsStateWithLifecycle(emptyList())

    val totalDays = attendanceLogs.size
    val presentDays = attendanceLogs.count { it.status.equals("PRESENT", ignoreCase = true) }
    val absentDays = attendanceLogs.count { it.status.equals("ABSENT", ignoreCase = true) }
    val leaveDays = attendanceLogs.count { it.status.equals("LEAVE", ignoreCase = true) }
    val percentage = if (totalDays > 0) (presentDays.toFloat() / totalDays) * 100f else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Student Selector
        if (allStudents.isNotEmpty()) {
            Text("Select Student", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allStudents.forEach { st ->
                    FilterChip(
                        selected = selectedStudent?.id == st.id,
                        onClick = { selectedStudentId = st.id },
                        label = { Text("${st.rollNumber}. ${st.name} (${st.studentClass})", fontSize = 12.sp) }
                    )
                }
            }
        }

        if (selectedStudent != null) {
            // Student Profile & Stats Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentAvatar(name = selectedStudent.name, colorHex = selectedStudent.avatarColorHex, size = 48.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedStudent.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Roll: ${selectedStudent.rollNumber} • ${selectedStudent.studentClass} - Sec ${selectedStudent.section}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniDailyStat("Present", "$presentDays", StatusPresent)
                        MiniDailyStat("Absent", "$absentDays", StatusAbsent)
                        MiniDailyStat("Leave", "$leaveDays", StatusLeave)
                        MiniDailyStat("Total Logs", "$totalDays", MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Text("Attendance History Records", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            // Attendance Logs List
            if (attendanceLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance logs recorded for this student", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(attendanceLogs, key = { it.id }) { log ->
                        val statusEnum = try {
                            AttendanceStatus.valueOf(log.status.uppercase(Locale.ROOT))
                        } catch (e: Exception) {
                            AttendanceStatus.PRESENT
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.date, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${log.studentClass} - Sec ${log.section}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                StatusBadge(status = statusEnum)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassWiseReportView(viewModel: AttendanceViewModel) {
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val todayRecords by viewModel.todayAttendanceRecords.collectAsStateWithLifecycle()

    val classesOverview = remember(allStudents, todayRecords) {
        val grouped = allStudents.groupBy { it.studentClass }
        grouped.map { (className, list) ->
            val classRecords = todayRecords.filter { it.studentClass == className }
            val p = classRecords.count { it.status.equals("PRESENT", ignoreCase = true) }
            val tot = classRecords.size
            val pct = if (tot > 0) (p.toFloat() / tot) * 100f else 0f
            ClassOverviewItem(
                className = className,
                totalStudents = list.size,
                sectionsCount = list.map { it.section }.distinct().size,
                todayAttendancePct = pct,
                isMarkedToday = tot > 0
            )
        }.sortedBy { it.className }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Class-Wise Summary & Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(classesOverview) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.className, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${item.totalStudents} students • ${item.sectionsCount} sections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (item.isMarkedToday) {
                                Text(
                                    "${String.format("%.1f", item.todayAttendancePct)}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    "No Logs Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (item.isMarkedToday) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (item.todayAttendancePct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = StatusPresent,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ClassOverviewItem(
    val className: String,
    val totalStudents: Int,
    val sectionsCount: Int,
    val todayAttendancePct: Float,
    val isMarkedToday: Boolean
)
