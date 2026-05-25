package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.ProfileRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.model.UserProfile
import com.example.projectpbd.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val remote: ProfileRemoteDataSource,
    private val sessionProvider: UserSessionProvider
) : ProfileRepository {
    override fun observeProfile(): Flow<Resource<UserProfile>> {
        val uid = sessionProvider.requireUserId()
        return remote.observeProfile(uid)
            .map { profile ->
                val data = profile?.toDomain()
                if (data == null) Resource.Error("Profile not found.") else Resource.Success(data)
            }
            .onStart { emit(Resource.Loading) }
            .catch { error -> emit(Resource.Error(error.message ?: "Failed to load profile.", error)) }
    }

    override suspend fun upsertProfile(profile: UserProfile): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.upsertProfile(uid, profile.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to save profile.", error)
    }

    override suspend fun getProfile(): Resource<UserProfile> = try {
        val uid = sessionProvider.requireUserId()
        val profile = remote.getProfile(uid)?.toDomain()
            ?: return Resource.Error("Profile not found.")
        Resource.Success(profile)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to load profile.", error)
    }
}
