package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY studentClass ASC, section ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY studentClass ASC, section ASC, rollNumber ASC")
    suspend fun getAllStudentsSync(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdSync(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE studentClass = :studentClass AND section = :section ORDER BY rollNumber ASC")
    fun getStudentsByClassAndSection(studentClass: String, section: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE studentClass = :studentClass AND section = :section ORDER BY rollNumber ASC")
    suspend fun getStudentsByClassAndSectionSync(studentClass: String, section: String): List<StudentEntity>

    @Query("SELECT DISTINCT studentClass FROM students WHERE studentClass != '' ORDER BY studentClass ASC")
    fun getAllClasses(): Flow<List<String>>

    @Query("SELECT DISTINCT section FROM students WHERE section != '' ORDER BY section ASC")
    fun getAllSections(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Query("DELETE FROM students")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}
