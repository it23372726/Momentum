package com.example.projectpbd.di

import com.example.projectpbd.data.datasource.CategoryRemoteDataSource
import com.example.projectpbd.data.datasource.ExpenseRemoteDataSource
import com.example.projectpbd.data.datasource.FirebaseUserSessionProvider
import com.example.projectpbd.data.datasource.GoalRemoteDataSource
import com.example.projectpbd.data.datasource.IncomeRemoteDataSource
import com.example.projectpbd.data.datasource.InsightRemoteDataSource
import com.example.projectpbd.data.datasource.ProfileRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.repository.CategoryRepositoryImpl
import com.example.projectpbd.data.repository.ExpenseRepositoryImpl
import com.example.projectpbd.data.repository.GoalRepositoryImpl
import com.example.projectpbd.data.repository.IncomeRepositoryImpl
import com.example.projectpbd.data.repository.InsightRepositoryImpl
import com.example.projectpbd.data.repository.ProfileRepositoryImpl
import com.example.projectpbd.domain.repository.CategoryRepository
import com.example.projectpbd.domain.repository.ExpenseRepository
import com.example.projectpbd.domain.repository.GoalRepository
import com.example.projectpbd.domain.repository.IncomeRepository
import com.example.projectpbd.domain.repository.InsightRepository
import com.example.projectpbd.domain.repository.ProfileRepository
import com.example.projectpbd.domain.repository.WalletRepository
import com.example.projectpbd.domain.repository.SettingsRepository
import com.example.projectpbd.data.repository.WalletRepositoryImpl
import com.example.projectpbd.data.repository.SettingsRepositoryImpl
import com.example.projectpbd.data.datasource.WalletRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FinanceBindingsModule {
    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindInsightRepository(impl: InsightRepositoryImpl): InsightRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindUserSessionProvider(impl: FirebaseUserSessionProvider): UserSessionProvider

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FinanceModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideCategoryRemoteDataSource(firestore: FirebaseFirestore): CategoryRemoteDataSource =
        CategoryRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideIncomeRemoteDataSource(firestore: FirebaseFirestore): IncomeRemoteDataSource =
        IncomeRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideExpenseRemoteDataSource(firestore: FirebaseFirestore): ExpenseRemoteDataSource =
        ExpenseRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideGoalRemoteDataSource(firestore: FirebaseFirestore): GoalRemoteDataSource =
        GoalRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideProfileRemoteDataSource(firestore: FirebaseFirestore): ProfileRemoteDataSource =
        ProfileRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideInsightRemoteDataSource(firestore: FirebaseFirestore): InsightRemoteDataSource =
        InsightRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideWalletRemoteDataSource(firestore: FirebaseFirestore): WalletRemoteDataSource =
        WalletRemoteDataSource(firestore)
}

