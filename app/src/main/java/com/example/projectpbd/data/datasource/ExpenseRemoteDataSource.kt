package com.example.projectpbd.data.datasource

import com.example.projectpbd.core.utils.FirestorePaths
import com.example.projectpbd.data.model.ExpenseEntity
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
class ExpenseRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeExpenses(uid: String): Flow<List<ExpenseEntity>> = callbackFlow {
        Log.d("DASHBOARD_DEBUG", "Observing expenses for uid=$uid")
        val registration: ListenerRegistration = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.EXPENSES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DASHBOARD_DEBUG", "Expense listener error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(ExpenseEntity::class.java).orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addExpense(uid: String, expense: ExpenseEntity): String {
        val collection = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.EXPENSES)
        val doc = if (expense.id.isBlank()) collection.document() else collection.document(expense.id)
        val payload = expense.copy(id = doc.id)
        doc.set(payload).await()
        return doc.id
    }

    suspend fun updateExpense(uid: String, expense: ExpenseEntity) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.EXPENSES)
            .document(expense.id)
            .set(expense)
            .await()
    }

    suspend fun deleteExpense(uid: String, id: String) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.EXPENSES)
            .document(id)
            .delete()
            .await()
    }

    suspend fun getExpense(uid: String, id: String): ExpenseEntity? {
        val snapshot = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.EXPENSES)
            .document(id)
            .get()
            .await()
        return snapshot.toObject(ExpenseEntity::class.java)
    }
}
