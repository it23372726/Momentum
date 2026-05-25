package com.example.projectpbd

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projectpbd.core.worker.CurrencySyncWorker
import com.example.projectpbd.core.worker.RecurringTransactionWorker
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MomentumApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        scheduleRecurringWorker()
        CurrencySyncWorker.schedule(this)
    }

    private fun scheduleRecurringWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            1, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RecurringTransactionWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
