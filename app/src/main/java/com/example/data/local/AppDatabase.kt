package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StudentEntity::class, AttendanceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_attendance_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    seedInitialData(database)
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val studentDao = database.studentDao()
            val attendanceDao = database.attendanceDao()

            if (studentDao.getAllStudentsSync().isEmpty()) {
                val sampleStudents = listOf(
                    StudentEntity(
                        rollNumber = "101",
                        name = "Aarav Sharma",
                        fatherName = "Rajesh Sharma",
                        motherName = "Sunita Sharma",
                        studentClass = "Class 10",
                        section = "A",
                        mobileNumber = "+91 98765 43210",
                        address = "42 Sunrise Enclave, New Delhi",
                        dateOfBirth = "2010-04-15",
                        admissionNumber = "ADM-2022-001",
                        status = "Active",
                        avatarColorHex = "#4CAF50"
                    ),
                    StudentEntity(
                        rollNumber = "102",
                        name = "Diya Patel",
                        fatherName = "Kiran Patel",
                        motherName = "Meena Patel",
                        studentClass = "Class 10",
                        section = "A",
                        mobileNumber = "+91 98765 43211",
                        address = "12 Harmony Park, Ahmedabad",
                        dateOfBirth = "2010-08-22",
                        admissionNumber = "ADM-2022-002",
                        status = "Active",
                        avatarColorHex = "#2196F3"
                    ),
                    StudentEntity(
                        rollNumber = "103",
                        name = "Rohan Verma",
                        fatherName = "Anil Verma",
                        motherName = "Rekha Verma",
                        studentClass = "Class 10",
                        section = "A",
                        mobileNumber = "+91 98765 43212",
                        address = "78 Green Valley, Bengaluru",
                        dateOfBirth = "2010-02-10",
                        admissionNumber = "ADM-2022-003",
                        status = "Active",
                        avatarColorHex = "#9C27B0"
                    ),
                    StudentEntity(
                        rollNumber = "104",
                        name = "Ananya Iyer",
                        fatherName = "Suresh Iyer",
                        motherName = "Lakshmi Iyer",
                        studentClass = "Class 10",
                        section = "A",
                        mobileNumber = "+91 98765 43213",
                        address = "55 Palm Grove, Chennai",
                        dateOfBirth = "2010-11-05",
                        admissionNumber = "ADM-2022-004",
                        status = "Active",
                        avatarColorHex = "#E91E63"
                    ),
                    StudentEntity(
                        rollNumber = "105",
                        name = "Kabir Singh",
                        fatherName = "Harpreet Singh",
                        motherName = "Jaspreet Kaur",
                        studentClass = "Class 10",
                        section = "A",
                        mobileNumber = "+91 98765 43214",
                        address = "19 Model Town, Chandigarh",
                        dateOfBirth = "2010-06-18",
                        admissionNumber = "ADM-2022-005",
                        status = "Active",
                        avatarColorHex = "#FF9800"
                    ),
                    StudentEntity(
                        rollNumber = "201",
                        name = "Ishaan Gupta",
                        fatherName = "Manoj Gupta",
                        motherName = "Pooja Gupta",
                        studentClass = "Class 10",
                        section = "B",
                        mobileNumber = "+91 98765 43215",
                        address = "88 Silver Oaks, Jaipur",
                        dateOfBirth = "2010-01-30",
                        admissionNumber = "ADM-2022-006",
                        status = "Active",
                        avatarColorHex = "#009688"
                    ),
                    StudentEntity(
                        rollNumber = "202",
                        name = "Pari Deshmukh",
                        fatherName = "Vikram Deshmukh",
                        motherName = "Smita Deshmukh",
                        studentClass = "Class 10",
                        section = "B",
                        mobileNumber = "+91 98765 43216",
                        address = "23 Shivaji Nagar, Pune",
                        dateOfBirth = "2010-09-12",
                        admissionNumber = "ADM-2022-007",
                        status = "Active",
                        avatarColorHex = "#673AB7"
                    ),
                    StudentEntity(
                        rollNumber = "101",
                        name = "Siddharth Rao",
                        fatherName = "Venkat Rao",
                        motherName = "Geetha Rao",
                        studentClass = "Class 9",
                        section = "A",
                        mobileNumber = "+91 98765 43217",
                        address = "90 Jubilee Hills, Hyderabad",
                        dateOfBirth = "2011-03-25",
                        admissionNumber = "ADM-2023-001",
                        status = "Active",
                        avatarColorHex = "#3F51B5"
                    ),
                    StudentEntity(
                        rollNumber = "102",
                        name = "Meera Nair",
                        fatherName = "Madhavan Nair",
                        motherName = "Radhika Nair",
                        studentClass = "Class 9",
                        section = "A",
                        mobileNumber = "+91 98765 43218",
                        address = "14 Marine Drive, Kochi",
                        dateOfBirth = "2011-07-14",
                        admissionNumber = "ADM-2023-002",
                        status = "Active",
                        avatarColorHex = "#00BCD4"
                    )
                )

                studentDao.insertAll(sampleStudents)

                // Also seed some initial past attendance records for demo and testing!
                val insertedStudents = studentDao.getAllStudentsSync()
                val todayStr = "2026-08-17"
                val yesterdayStr = "2026-08-16"
                val pastDateStr = "2026-08-15"

                val sampleAttendance = mutableListOf<AttendanceEntity>()
                val class10AStudents = insertedStudents.filter { it.studentClass == "Class 10" && it.section == "A" }

                // Attendance for today
                class10AStudents.forEachIndexed { index, student ->
                    val status = when (index) {
                        0 -> "PRESENT"
                        1 -> "PRESENT"
                        2 -> "ABSENT"
                        3 -> "PRESENT"
                        4 -> "LEAVE"
                        else -> "PRESENT"
                    }
                    sampleAttendance.add(
                        AttendanceEntity(
                            studentId = student.id,
                            date = todayStr,
                            studentClass = student.studentClass,
                            section = student.section,
                            status = status
                        )
                    )
                }

                // Attendance for yesterday
                class10AStudents.forEachIndexed { index, student ->
                    val status = if (index == 2) "LEAVE" else "PRESENT"
                    sampleAttendance.add(
                        AttendanceEntity(
                            studentId = student.id,
                            date = yesterdayStr,
                            studentClass = student.studentClass,
                            section = student.section,
                            status = status
                        )
                    )
                }

                // Attendance for past date
                class10AStudents.forEachIndexed { index, student ->
                    val status = if (index == 1) "ABSENT" else "PRESENT"
                    sampleAttendance.add(
                        AttendanceEntity(
                            studentId = student.id,
                            date = pastDateStr,
                            studentClass = student.studentClass,
                            section = student.section,
                            status = status
                        )
                    )
                }

                attendanceDao.insertOrUpdateAll(sampleAttendance)
            }
        }
    }
}
