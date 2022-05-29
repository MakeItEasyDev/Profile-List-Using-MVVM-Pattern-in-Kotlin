package com.kotlin.task.database.repository

import androidx.lifecycle.LiveData
import com.kotlin.task.database.dao.ProfileDao
import com.kotlin.task.database.entity.ProfileList

class ProfileRepository(private val profileDao: ProfileDao) {
    val allProfileList: LiveData<List<ProfileList>> = profileDao.getAllProfileList()

    suspend fun insert(profileList: List<ProfileList>) {
        profileDao.insert(profileList)
    }
}