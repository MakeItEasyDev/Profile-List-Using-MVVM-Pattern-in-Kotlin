package com.kotlin.task.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kotlin.task.database.entity.ProfileList

@Dao
interface ProfileDao {
    @Insert
    fun insert(profileList: List<ProfileList>)

    @Query("SELECT * FROM all_profile_data")
    fun getAllProfileList(): LiveData<List<ProfileList>>

    @Query("SELECT * FROM all_profile_data WHERE name LIKE '%' || :name || '%'")
    fun getSearchList(name: String): LiveData<List<ProfileList>>
}