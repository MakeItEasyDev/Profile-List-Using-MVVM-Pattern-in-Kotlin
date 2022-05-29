package com.kotlin.task.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "all_profile_data")
data class ProfileList(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val email: String,
    val gender: String,
    val dob: String,
    val phone: String,
    val latitude: String,
    val longitude: String,
    val large: String,
    val medium: String,
    val thumbnail: String,
)
