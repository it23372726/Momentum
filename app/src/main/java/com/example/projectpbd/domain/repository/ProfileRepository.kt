package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<Resource<UserProfile>>
    suspend fun upsertProfile(profile: UserProfile): Resource<Unit>
    suspend fun getProfile(): Resource<UserProfile>
}

