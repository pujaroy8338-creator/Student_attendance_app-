package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceEntity
import com.example.data.local.StudentEntity
import com.example.data.model.AppSettings
import com.example.data.model.AttendanceStatus
import com.example.data.model.DashboardStats
import com.example.data.model.MonthlyStudentRow
import com.example.data.model.SortOption
import com.example.data.model.StudentAttendanceSummary
import com.example.data.model.ThemeMode
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = AttendanceRepository(
        database.studentDao(),
        database.attendanceDao(),
        database
    )

    private val prefs = application.getSharedPreferences("smart_attendance_prefs", Context.MODE_PRIVATE)

    // App Settings
    private val _settings = MutableStateFlow(loadSettingsFromPrefs())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Date Formatters
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayDateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
    val shortDisplayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    // Today's String
    val todayDateString: String = dateFormat.format(Date())

    // All Students from DB
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendance selection state
    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedClass = MutableStateFlow("Class 10")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _selectedSection = MutableStateFlow("A")
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()

    // Attendance in-memory map for the currently selected Date + Class + Section
    private val _currentAttendanceMap = MutableStateFlow<Map<Long, AttendanceStatus>>(emptyMap())
    val currentAttendanceMap: StateFlow<Map<Long, AttendanceStatus>> = _currentAttendanceMap.asStateFlow()

    private val _isSavedInDb = MutableStateFlow(false)
    val isSavedInDb: StateFlow<Boolean> = _isSavedInDb.asStateFlow()

    // Student filter & search state for Students Screen
    val searchQuery = MutableStateFlow("")
    val filterClass = MutableStateFlow<String?>(null)
    val filterSection = MutableStateFlow<String?>(null)
    val sortOption = MutableStateFlow(SortOption.ROLL_NUMBER_ASC)

    // Filtered & Sorted students
    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        allStudents,
        searchQuery,
        filterClass,
        filterSection,
        sortOption
    ) { list, query, fClass, fSection, sort ->
        list.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.rollNumber.contains(query, ignoreCase = true) ||
                    student.admissionNumber.contains(query, ignoreCase = true) ||
                    student.mobileNumber.contains(query, ignoreCase = true)
            val matchesClass = fClass == null || student.studentClass.equals(fClass, ignoreCase = true)
            val matchesSection = fSection == null || student.section.equals(fSection, ignoreCase = true)
            matchesQuery && matchesClass && matchesSection
        }.let { filtered ->
            when (sort) {
                SortOption.ROLL_NUMBER_ASC -> filtered.sortedWith(compareBy<StudentEntity> { it.studentClass }.thenBy { it.section }.thenBy { it.rollNumber })
                SortOption.ROLL_NUMBER_DESC -> filtered.sortedWith(compareByDescending<StudentEntity> { it.rollNumber })
                SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase(Locale.ROOT) }
                SortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase(Locale.ROOT) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All available classes and sections derived from student records
    val availableClasses: StateFlow<List<String>> = allStudents.combine(MutableStateFlow(emptyList<String>())) { students, _ ->
        val set = mutableSetOf("Class 10", "Class 9", "Class 8", "Class 7", "Class 6")
        students.forEach { if (it.studentClass.isNotBlank()) set.add(it.studentClass) }
        set.toList().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Class 10", "Class 9", "Class 8"))

    val availableSections: StateFlow<List<String>> = allStudents.combine(MutableStateFlow(emptyList<String>())) { students, _ ->
        val set = mutableSetOf("A", "B", "C", "D")
        students.forEach { if (it.section.isNotBlank()) set.add(it.section) }
        set.toList().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("A", "B", "C", "D"))

    // Today's Dashboard Stats
    val todayAttendanceRecords: StateFlow<List<AttendanceEntity>> = repository.getAttendanceForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayStats: StateFlow<DashboardStats> = combine(allStudents, todayAttendanceRecords) { students, records ->
        val totalStudents = students.size
        var present = 0
        var absent = 0
        var leave = 0

        records.forEach { r ->
            when (r.status.uppercase(Locale.ROOT)) {
                "PRESENT" -> present++
                "ABSENT" -> absent++
                "LEAVE" -> leave++
            }
        }

        val percentage = if (records.isNotEmpty()) {
            (present.toFloat() / records.size) * 100f
        } else 0f

        val recordedClasses = records.map { "${it.studentClass}-${it.section}" }.distinct().size
        val totalClassPairs = students.map { "${it.studentClass}-${it.section}" }.distinct().size

        DashboardStats(
            totalStudents = totalStudents,
            presentCount = present,
            absentCount = absent,
            leaveCount = leave,
            attendancePercentage = percentage,
            totalClasses = totalClassPairs,
            classesTakenToday = recordedClasses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        // Load initial attendance for default class/section and today's date
        reloadAttendanceForCurrentSelection()
    }

    private fun loadSettingsFromPrefs(): AppSettings {
        return AppSettings(
            schoolName = prefs.getString("school_name", "Smart Academy") ?: "Smart Academy",
            academicYear = prefs.getString("academic_year", "2026-2027") ?: "2026-2027",
            defaultClass = prefs.getString("default_class", "Class 10") ?: "Class 10",
            defaultSection = prefs.getString("default_section", "A") ?: "A",
            allowFutureDates = prefs.getBoolean("allow_future_dates", false),
            themeMode = ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putString("school_name", newSettings.schoolName)
            .putString("academic_year", newSettings.academicYear)
            .putString("default_class", newSettings.defaultClass)
            .putString("default_section", newSettings.defaultSection)
            .putBoolean("allow_future_dates", newSettings.allowFutureDates)
            .putString("theme_mode", newSettings.themeMode.name)
            .apply()
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        reloadAttendanceForCurrentSelection()
    }

    fun selectClass(className: String) {
        _selectedClass.value = className
        reloadAttendanceForCurrentSelection()
    }

    fun selectSection(sectionName: String) {
        _selectedSection.value = sectionName
        reloadAttendanceForCurrentSelection()
    }

    fun reloadAttendanceForCurrentSelection() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val sClass = _selectedClass.value
            val sSec = _selectedSection.value

            val existingRecords = repository.getAttendanceForDateClassSectionSync(date, sClass, sSec)
            val students = repository.getStudentsByClassAndSectionSync(sClass, sSec)

            val newMap = mutableMapOf<Long, AttendanceStatus>()

            if (existingRecords.isNotEmpty()) {
                _isSavedInDb.value = true
                for (rec in existingRecords) {
                    val status = try {
                        AttendanceStatus.valueOf(rec.status.uppercase(Locale.ROOT))
                    } catch (e: Exception) {
                        AttendanceStatus.PRESENT
                    }
                    newMap[rec.studentId] = status
                }
                // For any student who doesn't have a record yet, default to PRESENT
                for (st in students) {
                    if (!newMap.containsKey(st.id)) {
                        newMap[st.id] = AttendanceStatus.PRESENT
                    }
                }
            } else {
                _isSavedInDb.value = false
                // Default all to PRESENT for easy marking
                for (st in students) {
                    newMap[st.id] = AttendanceStatus.PRESENT
                }
            }
            _currentAttendanceMap.value = newMap
        }
    }

    fun setStudentStatus(studentId: Long, status: AttendanceStatus) {
        val current = _currentAttendanceMap.value.toMutableMap()
        current[studentId] = status
        _currentAttendanceMap.value = current
    }

    fun markAll(status: AttendanceStatus) {
        val current = _currentAttendanceMap.value.toMutableMap()
        for (key in current.keys) {
            current[key] = status
        }
        _currentAttendanceMap.value = current
    }

    fun saveCurrentAttendance(onSuccess: (count: Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val date = _selectedDate.value
                val sClass = _selectedClass.value
                val sSec = _selectedSection.value
                val map = _currentAttendanceMap.value

                if (map.isEmpty()) {
                    onError("No students to mark attendance for.")
                    return@launch
                }

                val records = map.map { (studentId, status) ->
                    AttendanceEntity(
                        id = 0L,
                        studentId = studentId,
                        date = date,
                        studentClass = sClass,
                        section = sSec,
                        status = status.name,
                        updatedAt = System.currentTimeMillis()
                    )
                }

                repository.saveAttendanceBatch(records)
                _isSavedInDb.value = true
                onSuccess(records.size)
            } catch (e: Exception) {
                onError("Failed to save attendance: ${e.localizedMessage}")
            }
        }
    }

    fun addOrUpdateStudent(
        student: StudentEntity,
        isEdit: Boolean,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Validation checks
                if (student.name.isBlank()) {
                    onResult(false, "Student Name is required")
                    return@launch
                }
                if (student.rollNumber.isBlank()) {
                    onResult(false, "Roll Number is required")
                    return@launch
                }
                if (student.studentClass.isBlank()) {
                    onResult(false, "Class is required")
                    return@launch
                }
                if (student.section.isBlank()) {
                    onResult(false, "Section is required")
                    return@launch
                }

                if (isEdit) {
                    repository.updateStudent(student)
                    onResult(true, "Student updated successfully")
                } else {
                    val result = repository.insertStudent(student)
                    if (result.isSuccess) {
                        onResult(true, "Student added successfully")
                    } else {
                        onResult(false, "Roll No '${student.rollNumber}' already exists in ${student.studentClass} - Sec ${student.section}")
                    }
                }
                reloadAttendanceForCurrentSelection()
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun deleteStudent(student: StudentEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                reloadAttendanceForCurrentSelection()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // Student profile attendance stats
    fun getStudentSummary(studentId: Long) = repository.getAttendanceForStudent(studentId)

    // Monthly Report Data calculation
    fun getMonthlyReport(
        yearMonth: String, // e.g. "2026-08"
        sClass: String,
        sSection: String
    ) = repository.getAttendanceForMonthClassSection(yearMonth, sClass, sSection)

    fun getDailyReportForDate(date: String) = repository.getAttendanceForDate(date)

    suspend fun exportDataAsJson(): String {
        return repository.exportBackupJson()
    }

    fun importDataFromJson(json: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importBackupJson(json)
            if (result.isSuccess) {
                val (stCount, attCount) = result.getOrThrow()
                reloadAttendanceForCurrentSelection()
                onResult(true, "Successfully imported $stCount students and $attCount attendance records.")
            } else {
                onResult(false, "Failed to import JSON: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun resetSampleData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetToSampleData()
            reloadAttendanceForCurrentSelection()
            onComplete()
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            _currentAttendanceMap.value = emptyMap()
            _isSavedInDb.value = false
            onComplete()
        }
    }
}
