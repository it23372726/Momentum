package com.example.projectpbd.data.repository

import com.example.projectpbd.data.remote.FirebaseAuthDataSource
import com.example.projectpbd.presentation.auth.state.AuthState
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.userProfileChangeRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) : AuthRepository {
    override fun authState(): Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                trySend(AuthState.Authenticated)
            } else {
                trySend(AuthState.Unauthenticated)
            }
        }
        dataSource.auth.addAuthStateListener(listener)
        awaitClose { dataSource.auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return dataSource.auth.signInWithEmailAndPassword(email, password)
            .awaitResult()
            .map {}
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        val createResult = dataSource.auth.createUserWithEmailAndPassword(email, password).awaitResult()
        val user = createResult.getOrNull()?.user
            ?: return Result.failure(IllegalStateException("User not created."))
        val profileUpdates = userProfileChangeRequest { displayName = name }
        return user.updateProfile(profileUpdates).awaitResult().map {}
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        dataSource.auth.signOut()
    }.map {}
}

private suspend fun <T> Task<T>.awaitResult(): Result<T> = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (!cont.isActive) return@addOnCompleteListener
        if (task.isSuccessful) {
            cont.resume(Result.success(task.result))
        } else {
            cont.resume(Result.failure(task.exception ?: Exception("Authentication failed.")))
        }
    }
    addOnFailureListener { error ->
        if (cont.isActive) {
            cont.resume(Result.failure(error))
        }
    }
    addOnCanceledListener {
        if (cont.isActive) {
            cont.resume(Result.failure(Exception("Authentication cancelled.")))
        }
    }
}
