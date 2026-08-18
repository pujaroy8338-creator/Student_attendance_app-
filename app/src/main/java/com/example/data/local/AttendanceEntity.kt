package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["studentId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["studentClass", "section", "date"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // format YYYY-MM-DD
    val studentClass: String,
    val section: String,
    val status: String, // PRESENT, ABSENT, LEAVE
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
