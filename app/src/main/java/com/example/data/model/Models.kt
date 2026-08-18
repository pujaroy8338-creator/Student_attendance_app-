package com.example.data.model

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LEAVE;

    fun displayName(): String = when (this) {
        PRESENT -> "Present"
        ABSENT -> "Absent"
        LEAVE -> "Leave"
    }
}

enum class SortOption {
    ROLL_NUMBER_ASC,
    ROLL_NUMBER_DESC,
    NAME_ASC,
    NAME_DESC
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val schoolName: String = "Smart Academy",
    val academicYear: String = "2026-2027",
    val defaultClass: String = "Class 10",
    val defaultSection: String = "A",
    val allowFutureDates: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

data class DashboardStats(
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val leaveCount: Int = 0,
    val attendancePercentage: Float = 0f,
    val totalClasses: Int = 0,
    val classesTakenToday: Int = 0
)

data class StudentAttendanceSummary(
    val totalDays: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val leaveDays: Int = 0,
    val percentage: Float = 0f
)

data class MonthlyStudentRow(
    val studentId: Long,
    val rollNumber: String,
    val name: String,
    val presentDays: Int,
    val absentDays: Int,
    val leaveDays: Int,
    val totalDays: Int,
    val percentage: Float,
    val dailyStatusMap: Map<Int, AttendanceStatus> // Day of month -> Status
)
