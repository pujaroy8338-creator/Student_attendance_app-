package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceDao
import com.example.data.local.AttendanceEntity
import com.example.data.local.StudentDao
import com.example.data.local.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val database: AppDatabase
) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allClasses: Flow<List<String>> = studentDao.getAllClasses()
    val allSections: Flow<List<String>> = studentDao.getAllSections()
    val allRecordedDates: Flow<List<String>> = attendanceDao.getRecordedDates()

    fun getStudentsByClassAndSection(studentClass: String, section: String): Flow<List<StudentEntity>> {
        return studentDao.getStudentsByClassAndSection(studentClass, section)
    }

    suspend fun getStudentsByClassAndSectionSync(studentClass: String, section: String): List<StudentEntity> {
        return withContext(Dispatchers.IO) {
            studentDao.getStudentsByClassAndSectionSync(studentClass, section)
        }
    }

    fun getStudentById(id: Long): Flow<StudentEntity?> {
        return studentDao.getStudentById(id)
    }

    suspend fun insertStudent(student: StudentEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val id = studentDao.insertStudent(student)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateStudent(student: StudentEntity) {
        withContext(Dispatchers.IO) {
            studentDao.updateStudent(student)
        }
    }

    suspend fun deleteStudent(student: StudentEntity) {
        withContext(Dispatchers.IO) {
            attendanceDao.deleteForStudent(student.id)
            studentDao.deleteStudent(student)
        }
    }

    fun getAttendanceForDateClassSection(date: String, studentClass: String, section: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForDateClassSection(date, studentClass, section)
    }

    suspend fun getAttendanceForDateClassSectionSync(date: String, studentClass: String, section: String): List<AttendanceEntity> {
        return withContext(Dispatchers.IO) {
            attendanceDao.getAttendanceForDateClassSectionSync(date, studentClass, section)
        }
    }

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForDate(date)
    }

    suspend fun getAttendanceForDateSync(date: String): List<AttendanceEntity> {
        return withContext(Dispatchers.IO) {
            attendanceDao.getAttendanceForDateSync(date)
        }
    }

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForStudent(studentId)
    }

    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForMonth(monthPrefix)
    }

    fun getAttendanceForMonthClassSection(monthPrefix: String, studentClass: String, section: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForMonthClassSection(monthPrefix, studentClass, section)
    }

    suspend fun saveAttendanceBatch(records: List<AttendanceEntity>) {
        withContext(Dispatchers.IO) {
            attendanceDao.insertOrUpdateAll(records)
        }
    }

    suspend fun deleteAttendanceForDateClassSection(date: String, studentClass: String, section: String) {
        withContext(Dispatchers.IO) {
            attendanceDao.deleteForDateClassSection(date, studentClass, section)
        }
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val students = studentDao.getAllStudentsSync()
        val attendances = attendanceDao.getAllAttendanceSync()

        val rootObj = JSONObject()
        rootObj.put("version", "1.0")
        rootObj.put("exportedAt", System.currentTimeMillis())
        rootObj.put("appName", "Smart Attendance")

        val studentsArray = JSONArray()
        for (s in students) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("rollNumber", s.rollNumber)
                put("name", s.name)
                put("fatherName", s.fatherName)
                put("motherName", s.motherName)
                put("studentClass", s.studentClass)
                put("section", s.section)
                put("mobileNumber", s.mobileNumber)
                put("address", s.address)
                put("dateOfBirth", s.dateOfBirth)
                put("admissionNumber", s.admissionNumber)
                put("status", s.status)
                put("avatarColorHex", s.avatarColorHex)
                put("createdAt", s.createdAt)
            }
            studentsArray.put(sObj)
        }
        rootObj.put("students", studentsArray)

        val attendancesArray = JSONArray()
        for (a in attendances) {
            val aObj = JSONObject().apply {
                put("id", a.id)
                put("studentId", a.studentId)
                put("date", a.date)
                put("studentClass", a.studentClass)
                put("section", a.section)
                put("status", a.status)
                put("note", a.note)
                put("createdAt", a.createdAt)
                put("updatedAt", a.updatedAt)
            }
            attendancesArray.put(aObj)
        }
        rootObj.put("attendances", attendancesArray)

        rootObj.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val rootObj = JSONObject(jsonString)
            val studentsArray = rootObj.optJSONArray("students") ?: JSONArray()
            val attendancesArray = rootObj.optJSONArray("attendances") ?: JSONArray()

            val studentIdMap = mutableMapOf<Long, Long>()
            var importedStudentsCount = 0
            var importedAttendanceCount = 0

            // Clear and insert
            studentDao.deleteAll()
            attendanceDao.deleteAll()

            for (i in 0 until studentsArray.length()) {
                val sObj = studentsArray.getJSONObject(i)
                val oldId = sObj.optLong("id", -1L)
                val entity = StudentEntity(
                    id = 0L, // generate fresh or keep
                    rollNumber = sObj.optString("rollNumber", ""),
                    name = sObj.optString("name", ""),
                    fatherName = sObj.optString("fatherName", ""),
                    motherName = sObj.optString("motherName", ""),
                    studentClass = sObj.optString("studentClass", ""),
                    section = sObj.optString("section", ""),
                    mobileNumber = sObj.optString("mobileNumber", ""),
                    address = sObj.optString("address", ""),
                    dateOfBirth = sObj.optString("dateOfBirth", ""),
                    admissionNumber = sObj.optString("admissionNumber", ""),
                    status = sObj.optString("status", "Active"),
                    avatarColorHex = sObj.optString("avatarColorHex", "#3F51B5"),
                    createdAt = sObj.optLong("createdAt", System.currentTimeMillis())
                )
                val newId = studentDao.insertStudent(entity)
                if (oldId != -1L) {
                    studentIdMap[oldId] = newId
                }
                importedStudentsCount++
            }

            val attendanceList = mutableListOf<AttendanceEntity>()
            for (i in 0 until attendancesArray.length()) {
                val aObj = attendancesArray.getJSONObject(i)
                val originalStudentId = aObj.optLong("studentId", -1L)
                val targetStudentId = studentIdMap[originalStudentId] ?: originalStudentId

                if (targetStudentId > 0) {
                    attendanceList.add(
                        AttendanceEntity(
                            id = 0L,
                            studentId = targetStudentId,
                            date = aObj.optString("date", ""),
                            studentClass = aObj.optString("studentClass", ""),
                            section = aObj.optString("section", ""),
                            status = aObj.optString("status", "PRESENT"),
                            note = aObj.optString("note", ""),
                            createdAt = aObj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = aObj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                    importedAttendanceCount++
                }
            }

            if (attendanceList.isNotEmpty()) {
                attendanceDao.insertOrUpdateAll(attendanceList)
            }

            Result.success(Pair(importedStudentsCount, importedAttendanceCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        studentDao.deleteAll()
        attendanceDao.deleteAll()
        AppDatabase.seedInitialData(database)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        studentDao.deleteAll()
        attendanceDao.deleteAll()
    }
}
