package com.example.projectpbd.data.datasource

import com.example.projectpbd.data.model.WalletEntity
import com.example.projectpbd.data.model.TransferEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeWallets(uid: String): Flow<List<WalletEntity>> {
        return firestore.collection("users")
            .document(uid)
            .collection("wallets")
            .snapshots()
            .map { it.toObjects(WalletEntity::class.java) }
    }

    suspend fun addWallet(uid: String, wallet: WalletEntity): String {
        val collection = firestore.collection("users").document(uid).collection("wallets")
        val ref = if (wallet.id.isBlank()) collection.document() else collection.document(wallet.id)
        val walletWithId = wallet.copy(id = ref.id)
        ref.set(walletWithId).await()
        return ref.id
    }

    suspend fun updateWallet(uid: String, wallet: WalletEntity) {
        firestore.collection("users")
            .document(uid)
            .collection("wallets")
            .document(wallet.id)
            .set(wallet)
            .await()
    }

    suspend fun deleteWallet(uid: String, walletId: String) {
        firestore.collection("users")
            .document(uid)
            .collection("wallets")
            .document(walletId)
            .delete()
            .await()
    }

    suspend fun getWallet(uid: String, walletId: String): WalletEntity? {
        return firestore.collection("users")
            .document(uid)
            .collection("wallets")
            .document(walletId)
            .get()
            .await()
            .toObject(WalletEntity::class.java)
    }

    fun observeTransfers(uid: String): Flow<List<TransferEntity>> {
        return firestore.collection("users")
            .document(uid)
            .collection("transfers")
            .snapshots()
            .map { it.toObjects(TransferEntity::class.java) }
    }

    suspend fun addTransfer(uid: String, transfer: TransferEntity) {
        val ref = firestore.collection("users").document(uid).collection("transfers").document()
        val transferWithId = transfer.copy(id = ref.id)
        ref.set(transferWithId).await()
    }
}
