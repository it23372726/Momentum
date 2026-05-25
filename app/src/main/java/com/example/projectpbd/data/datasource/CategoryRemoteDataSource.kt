package com.example.projectpbd.data.datasource

import com.example.projectpbd.core.utils.FirestorePaths
import com.example.projectpbd.data.model.CategoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeCategories(uid: String): Flow<List<CategoryEntity>> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CATEGORIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(CategoryEntity::class.java).orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addCategory(uid: String, category: CategoryEntity): String {
        val collection = firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CATEGORIES)
        val doc = if (category.id.isBlank()) collection.document() else collection.document(category.id)
        val payload = category.copy(id = doc.id)
        doc.set(payload).await()
        return doc.id
    }

    suspend fun deleteCategory(uid: String, id: String) {
        firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CATEGORIES)
            .document(id)
            .delete()
            .await()
    }
}
