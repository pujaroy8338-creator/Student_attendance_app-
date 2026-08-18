package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["rollNumber", "studentClass", "section"], unique = true)
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rollNumber: String,
    val name: String,
    val fatherName: String = "",
    val motherName: String = "",
    val studentClass: String,
    val section: String,
    val mobileNumber: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val admissionNumber: String = "",
    val status: String = "Active", // "Active" or "Inactive"
    val avatarColorHex: String = "#3F51B5",
    val createdAt: Long = System.currentTimeMillis()
)
