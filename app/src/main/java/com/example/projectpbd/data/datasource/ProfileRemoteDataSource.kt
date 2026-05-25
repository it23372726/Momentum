package com.example.projectpbd.data.datasource

import com.example.projectpbd.core.utils.FirestorePaths
import com.example.projectpbd.data.model.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class ProfileRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeProfile(uid: String): Flow<UserProfileEntity?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.PROFILE)
            .document(FirestorePaths.PROFILE)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserProfileEntity::class.java))
            }
        awaitClose { registration.remove() }
    }

    suspend fun upsertProfile(uid: String, profile: UserProfileEntity) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.PROFILE)
            .document(FirestorePaths.PROFILE)
            .set(profile)
            .await()
    }

    suspend fun getProfile(uid: String): UserProfileEntity? {
        val snapshot = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.PROFILE)
            .document(FirestorePaths.PROFILE)
            .get()
            .await()
        return snapshot.toObject(UserProfileEntity::class.java)
    }
}
