package com.example.projectpbd.data.datasource

import com.example.projectpbd.core.utils.FirestorePaths
import com.example.projectpbd.data.model.SavingsGoalEntity
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
class GoalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeGoals(uid: String): Flow<List<SavingsGoalEntity>> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.GOALS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(SavingsGoalEntity::class.java).orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addGoal(uid: String, goal: SavingsGoalEntity): String {
        val collection = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.GOALS)
        val doc = if (goal.id.isBlank()) collection.document() else collection.document(goal.id)
        val payload = goal.copy(id = doc.id)
        doc.set(payload).await()
        return doc.id
    }

    suspend fun updateGoal(uid: String, goal: SavingsGoalEntity) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.GOALS)
            .document(goal.id)
            .set(goal)
            .await()
    }

    suspend fun deleteGoal(uid: String, id: String) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.GOALS)
            .document(id)
            .delete()
            .await()
    }

    suspend fun getGoal(uid: String, id: String): SavingsGoalEntity? {
        val snapshot = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.GOALS)
            .document(id)
            .get()
            .await()
        return snapshot.toObject(SavingsGoalEntity::class.java)
    }
}
