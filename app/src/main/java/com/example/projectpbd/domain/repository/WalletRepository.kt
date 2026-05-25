package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.model.Transfer
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeWallets(): Flow<Resource<List<Wallet>>>
    fun observeTransfers(): Flow<Resource<List<Transfer>>>
    suspend fun addWallet(wallet: Wallet): Resource<String>
    suspend fun updateWallet(wallet: Wallet): Resource<Unit>
    suspend fun deleteWallet(id: String): Resource<Unit>
    suspend fun getWallet(id: String): Resource<Wallet>
    suspend fun transferFunds(transfer: Transfer): Resource<Unit>
    suspend fun updateBalance(walletId: String, amount: Double, isAddition: Boolean): Resource<Unit>
}
