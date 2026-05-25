package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.WalletRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.manager.CurrencyConversionManager
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.model.Transfer
import com.example.projectpbd.domain.repository.WalletRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val remote: WalletRemoteDataSource,
    private val sessionProvider: UserSessionProvider,
    private val conversionManager: CurrencyConversionManager,
    private val firestore: FirebaseFirestore
) : WalletRepository {

    override fun observeWallets(): Flow<Resource<List<Wallet>>> = flow {
        emit(Resource.Loading)
        val uid = sessionProvider.currentUserId()
        if (uid == null) {
            emit(Resource.Error("Sign in to view wallets."))
            return@flow
        }
        emitAll(
            remote.observeWallets(uid)
                .map { items -> Resource.Success(items.map { it.toDomain() }) as Resource<List<Wallet>> }
                .catch { error -> emit(Resource.Error(error.message ?: "Failed to load wallets.", error)) }
        )
    }

    override fun observeTransfers(): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading)
        val uid = sessionProvider.currentUserId()
        if (uid == null) {
            emit(Resource.Error("Sign in to view transfers."))
            return@flow
        }
        emitAll(
            remote.observeTransfers(uid)
                .map { items -> Resource.Success(items.map { it.toDomain() }) as Resource<List<Transfer>> }
                .catch { error -> emit(Resource.Error(error.message ?: "Failed to load transfers.", error)) }
        )
    }

    override suspend fun addWallet(wallet: Wallet): Resource<String> = try {
        val uid = sessionProvider.requireUserId()
        val id = remote.addWallet(uid, wallet.toEntity())
        Resource.Success(id)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to add wallet.", error)
    }

    override suspend fun updateWallet(wallet: Wallet): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.updateWallet(uid, wallet.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to update wallet.", error)
    }

    override suspend fun deleteWallet(id: String): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.deleteWallet(uid, id)
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to delete wallet.", error)
    }

    override suspend fun getWallet(id: String): Resource<Wallet> = try {
        val uid = sessionProvider.requireUserId()
        val wallet = remote.getWallet(uid, id)?.toDomain()
            ?: return Resource.Error("Wallet not found.")
        Resource.Success(wallet)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to load wallet.", error)
    }

    override suspend fun transferFunds(transfer: Transfer): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        
        // 1. Fetch currencies to calculate conversion
        val sourceWallet = getWallet(transfer.sourceWalletId).let { (it as? Resource.Success)?.data } ?: throw Exception("Source wallet not found")
        val destWallet = getWallet(transfer.destinationWalletId).let { (it as? Resource.Success)?.data } ?: throw Exception("Destination wallet not found")
        
        val enrichedTransfer = conversionManager.enrichTransfer(transfer, sourceWallet.currency, destWallet.currency)

        firestore.runTransaction { transaction ->
            val sourceRef = firestore.collection("users").document(uid).collection("wallets").document(transfer.sourceWalletId)
            val destRef = firestore.collection("users").document(uid).collection("wallets").document(transfer.destinationWalletId)
            
            val sourceSnap = transaction.get(sourceRef)
            val currentSourceBalance = sourceSnap.getDouble("currentBalance") ?: 0.0
            
            if (currentSourceBalance < transfer.amount) {
                throw Exception("Insufficient funds in source wallet.")
            }
            
            transaction.update(sourceRef, "currentBalance", FieldValue.increment(-transfer.amount))
            transaction.update(destRef, "currentBalance", FieldValue.increment(enrichedTransfer.targetAmount))
            
            // Add transfer record
            val transferRef = firestore.collection("users").document(uid).collection("transfers").document()
            transaction.set(transferRef, enrichedTransfer.copy(id = transferRef.id).toEntity())
        }.await()
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Transfer failed.", error)
    }

    override suspend fun updateBalance(walletId: String, amount: Double, isAddition: Boolean): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        val walletRef = firestore.collection("users").document(uid).collection("wallets").document(walletId)
        val increment = if (isAddition) amount else -amount
        walletRef.update("currentBalance", FieldValue.increment(increment)).await()
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Balance update failed.", error)
    }
}
