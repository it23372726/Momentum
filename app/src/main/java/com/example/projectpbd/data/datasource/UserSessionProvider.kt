package com.example.projectpbd.data.datasource

interface UserSessionProvider {
    fun currentUserId(): String?
    fun requireUserId(): String
}
