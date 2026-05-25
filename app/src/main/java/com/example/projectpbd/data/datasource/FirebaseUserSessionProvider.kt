package com.example.projectpbd.data.datasource

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserSessionProvider @Inject constructor(
    private val auth: FirebaseAuth
) : UserSessionProvider {
    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated.")
    }
}
