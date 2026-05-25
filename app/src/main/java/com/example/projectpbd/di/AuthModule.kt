package com.example.projectpbd.di

import com.example.projectpbd.data.remote.FirebaseAuthDataSource
import com.example.projectpbd.data.repository.AuthRepository
import com.example.projectpbd.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthDataSource(auth: FirebaseAuth): FirebaseAuthDataSource =
        FirebaseAuthDataSource(auth)

    @Provides
    @Singleton
    fun provideAuthRepository(dataSource: FirebaseAuthDataSource): AuthRepository =
        FirebaseAuthRepository(dataSource)
}

