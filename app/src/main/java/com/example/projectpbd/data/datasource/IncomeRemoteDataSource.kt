package com.example.projectpbd.data.datasource

import com.example.projectpbd.core.utils.FirestorePaths
import com.example.projectpbd.data.model.IncomeEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

@Singleton
class IncomeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeIncomes(uid: String): Flow<List<IncomeEntity>> = callbackFlow {
        Log.d("DASHBOARD_DEBUG", "Observing incomes for uid=$uid")
        val registration: ListenerRegistration = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INCOMES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DASHBOARD_DEBUG", "Income listener error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(IncomeEntity::class.java).orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addIncome(uid: String, income: IncomeEntity): String {
        val collection = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INCOMES)
        val doc = if (income.id.isBlank()) collection.document() else collection.document(income.id)
        val payload = income.copy(id = doc.id)
        doc.set(payload).await()
        return doc.id
    }

    suspend fun updateIncome(uid: String, income: IncomeEntity) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INCOMES)
            .document(income.id)
            .set(income)
            .await()
    }

    suspend fun deleteIncome(uid: String, id: String) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INCOMES)
            .document(id)
            .delete()
            .await()
    }

    suspend fun getIncome(uid: String, id: String): IncomeEntity? {
        val snapshot = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INCOMES)
            .document(id)
            .get()
            .await()
        return snapshot.toObject(IncomeEntity::class.java)
    }
}
