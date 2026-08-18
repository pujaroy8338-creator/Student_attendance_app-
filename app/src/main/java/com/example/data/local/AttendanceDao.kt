package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    suspend fun getAllAttendanceSync(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getAttendanceForDateSync(date: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND studentClass = :studentClass AND section = :section")
    fun getAttendanceForDateClassSection(date: String, studentClass: String, section: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND studentClass = :studentClass AND section = :section")
    suspend fun getAttendanceForDateClassSectionSync(date: String, studentClass: String, section: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    suspend fun getAttendanceForStudentSync(studentId: Long): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    suspend fun getAttendanceForMonthSync(monthPrefix: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%' AND studentClass = :studentClass AND section = :section ORDER BY date ASC")
    fun getAttendanceForMonthClassSection(monthPrefix: String, studentClass: String, section: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceEntity>)

    @Query("DELETE FROM attendance_records WHERE date = :date AND studentClass = :studentClass AND section = :section")
    suspend fun deleteForDateClassSection(date: String, studentClass: String, section: String)

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId")
    suspend fun deleteForStudent(studentId: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT date FROM attendance_records ORDER BY date DESC")
    fun getRecordedDates(): Flow<List<String>>

    @Query("SELECT COUNT(DISTINCT date) FROM attendance_records WHERE studentClass = :studentClass AND section = :section AND date LIKE :monthPrefix || '%'")
    fun getWorkingDaysCountForMonth(studentClass: String, section: String, monthPrefix: String): Flow<Int>
}
