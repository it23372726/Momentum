package com.example.projectpbd.data.datasource

import com.example.projectpbd.core.utils.FirestorePaths
import com.example.projectpbd.data.model.FinancialInsightEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class InsightRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeInsights(uid: String): Flow<List<FinancialInsightEntity>> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INSIGHTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(FinancialInsightEntity::class.java).orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addInsight(uid: String, insight: FinancialInsightEntity): String {
        val collection = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INSIGHTS)
        val doc = if (insight.id.isBlank()) collection.document() else collection.document(insight.id)
        val payload = insight.copy(id = doc.id)
        doc.set(payload).await()
        return doc.id
    }

    suspend fun deleteInsight(uid: String, id: String) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.INSIGHTS)
            .document(id)
            .delete()
            .await()
    }
}

