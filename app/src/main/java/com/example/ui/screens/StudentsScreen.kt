package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.StudentEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.SortOption
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val availableClasses by viewModel.availableClasses.collectAsStateWithLifecycle()
    val availableSections by viewModel.availableSections.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterClass by viewModel.filterClass.collectAsStateWithLifecycle()
    val filterSection by viewModel.filterSection.collectAsStateWithLifecycle()
    val currentSort by viewModel.sortOption.collectAsStateWithLifecycle()

    // Dialog States
    var showStudentFormDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToViewProfile by remember { mutableStateOf<StudentEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    studentToEdit = null
                    showStudentFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Search & Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by name, roll no...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("student_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("student_sort_button")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Roll Number (Asc)") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.ROLL_NUMBER_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Roll Number (Desc)") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.ROLL_NUMBER_DESC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name (A to Z)") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.NAME_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name (Z to A)") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.NAME_DESC
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Class Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterClass == null,
                    onClick = { viewModel.filterClass.value = null },
                    label = { Text("All Classes") }
                )
                availableClasses.forEach { cls ->
                    FilterChip(
                        selected = filterClass == cls,
                        onClick = {
                            viewModel.filterClass.value = if (filterClass == cls) null else cls
                        },
                        label = { Text(cls) }
                    )
                }
            }

            // Section Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterSection == null,
                    onClick = { viewModel.filterSection.value = null },
                    label = { Text("All Sections") }
                )
                availableSections.forEach { sec ->
                    FilterChip(
                        selected = filterSection == sec,
                        onClick = {
                            viewModel.filterSection.value = if (filterSection == sec) null else sec
                        },
                        label = { Text("Sec $sec") }
                    )
                }
            }

            // Header Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${students.size} Students found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Students List
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add a student or clear filters",
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
                    items(students, key = { it.id }) { student ->
                        StudentItemCard(
                            student = student,
                            onViewProfile = { studentToViewProfile = student },
                            onEdit = {
                                studentToEdit = student
                                showStudentFormDialog = true
                            },
                            onDelete = { studentToDelete = student }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showStudentFormDialog) {
        StudentFormDialog(
            student = studentToEdit,
            availableClasses = availableClasses,
            availableSections = availableSections,
            onDismiss = {
                showStudentFormDialog = false
                studentToEdit = null
            },
            onSave = { entity, isEdit ->
                viewModel.addOrUpdateStudent(entity, isEdit) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showStudentFormDialog = false
                        studentToEdit = null
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (studentToDelete != null) {
        val st = studentToDelete!!
        ConfirmationDialog(
            title = "Delete Student",
            message = "Are you sure you want to delete ${st.name} (Roll: ${st.rollNumber})? This will also remove all associated attendance logs.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteStudent(st) { success ->
                    if (success) {
                        Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show()
                    }
                    studentToDelete = null
                }
            },
            onDismiss = { studentToDelete = null }
        )
    }

    // Student Profile Dialog
    if (studentToViewProfile != null) {
        StudentProfileDialog(
            student = studentToViewProfile!!,
            viewModel = viewModel,
            onDismiss = { studentToViewProfile = null },
            onEdit = {
                val st = studentToViewProfile!!
                studentToViewProfile = null
                studentToEdit = st
                showStudentFormDialog = true
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: StudentEntity,
    onViewProfile: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewProfile() }
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(name = student.name, colorHex = student.avatarColorHex, size = 46.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (student.status.equals("Inactive", ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Inactive",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Roll: ${student.rollNumber}  •  ${student.studentClass} - Sec ${student.section}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (student.mobileNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = student.mobileNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Profile") },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onViewProfile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Student") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Student", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    student: StudentEntity?,
    availableClasses: List<String>,
    availableSections: List<String>,
    onDismiss: () -> Unit,
    onSave: (entity: StudentEntity, isEdit: Boolean) -> Unit
) {
    val isEdit = student != null

    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var studentClass by remember { mutableStateOf(student?.studentClass ?: "Class 10") }
    var section by remember { mutableStateOf(student?.section ?: "A") }
    var fatherName by remember { mutableStateOf(student?.fatherName ?: "") }
    var motherName by remember { mutableStateOf(student?.motherName ?: "") }
    var mobileNumber by remember { mutableStateOf(student?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var dateOfBirth by remember { mutableStateOf(student?.dateOfBirth ?: "") }
    var admissionNumber by remember { mutableStateOf(student?.admissionNumber ?: "") }
    var status by remember { mutableStateOf(student?.status ?: "Active") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Student Details" else "Add New Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Student Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_form_name")
                )

                // Roll Number & Admission Number
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it; errorMessage = null },
                        label = { Text("Roll No *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_form_roll")
                    )
                    OutlinedTextField(
                        value = admissionNumber,
                        onValueChange = { admissionNumber = it },
                        label = { Text("Admission No") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Class & Section
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = studentClass,
                        onValueChange = { studentClass = it; errorMessage = null },
                        label = { Text("Class *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("student_form_class")
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it; errorMessage = null },
                        label = { Text("Section *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_form_section")
                    )
                }

                // Father's Name
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mother's Name
                OutlinedTextField(
                    value = motherName,
                    onValueChange = { motherName = it },
                    label = { Text("Mother's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mobile Number
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date of Birth & Status
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("DOB (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Status") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Student name is required"
                                return@Button
                            }
                            if (rollNumber.isBlank()) {
                                errorMessage = "Roll number is required"
                                return@Button
                            }
                            if (studentClass.isBlank()) {
                                errorMessage = "Class is required"
                                return@Button
                            }
                            if (section.isBlank()) {
                                errorMessage = "Section is required"
                                return@Button
                            }

                            val avatarColors = listOf("#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#FF9800", "#009688", "#3F51B5", "#00BCD4")
                            val chosenColor = student?.avatarColorHex ?: avatarColors.random()

                            val entity = StudentEntity(
                                id = student?.id ?: 0L,
                                rollNumber = rollNumber.trim(),
                                name = name.trim(),
                                fatherName = fatherName.trim(),
                                motherName = motherName.trim(),
                                studentClass = studentClass.trim(),
                                section = section.trim(),
                                mobileNumber = mobileNumber.trim(),
                                address = address.trim(),
                                dateOfBirth = dateOfBirth.trim(),
                                admissionNumber = admissionNumber.trim(),
                                status = status.trim().ifEmpty { "Active" },
                                avatarColorHex = chosenColor,
                                createdAt = student?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(entity, isEdit)
                        },
                        modifier = Modifier.testTag("save_student_button")
                    ) {
                        Text(if (isEdit) "Update Student" else "Save Student")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileDialog(
    student: StudentEntity,
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val attendanceLogs by viewModel.getStudentSummary(student.id).collectAsStateWithLifecycle(emptyList())

    val totalLogs = attendanceLogs.size
    val presentCount = attendanceLogs.count { it.status.equals("PRESENT", ignoreCase = true) }
    val absentCount = attendanceLogs.count { it.status.equals("ABSENT", ignoreCase = true) }
    val leaveCount = attendanceLogs.count { it.status.equals("LEAVE", ignoreCase = true) }
    val percentage = if (totalLogs > 0) (presentCount.toFloat() / totalLogs) * 100f else 0f

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Profile Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentAvatar(name = student.name, colorHex = student.avatarColorHex, size = 60.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Roll: ${student.rollNumber}  •  ${student.studentClass} - Sec ${student.section}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Admission: ${student.admissionNumber.ifEmpty { "N/A" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Attendance Stats Card
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
                                text = "Overall Attendance Rate",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MiniProfileStat("Present", presentCount.toString(), StatusPresent)
                            MiniProfileStat("Absent", absentCount.toString(), StatusAbsent)
                            MiniProfileStat("Leave", leaveCount.toString(), StatusLeave)
                            MiniProfileStat("Total Days", totalLogs.toString(), MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Detailed Bio Information
                Text(
                    text = "Personal & Contact Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                ProfileFieldRow("Father's Name", student.fatherName)
                ProfileFieldRow("Mother's Name", student.motherName)
                ProfileFieldRow("Mobile Number", student.mobileNumber)
                ProfileFieldRow("Date of Birth", student.dateOfBirth)
                ProfileFieldRow("Address", student.address)
                ProfileFieldRow("Status", student.status)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Student")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun MiniProfileStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.ifEmpty { "-" },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}
