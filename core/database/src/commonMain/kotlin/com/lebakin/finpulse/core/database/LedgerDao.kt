package com.lebakin.finpulse.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LedgerDao {
    @Query("SELECT * FROM transactions ORDER BY occurredOn DESC, id ASC")
    abstract fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    abstract fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertCategory(entity: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertTransaction(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE origin = :origin")
    abstract suspend fun deleteTransactions(origin: String)

    @Query("DELETE FROM categories WHERE origin = :origin")
    abstract suspend fun deleteCategories(origin: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE origin = :origin")
    abstract suspend fun countTransactions(origin: String): Int

    @Query("SELECT COUNT(*) FROM categories WHERE origin = :origin")
    abstract suspend fun countCategories(origin: String): Int

    @Transaction
    open suspend fun replaceOriginWhenAbsent(
        blockingOrigin: String,
        replacedOrigin: String,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
    ): Boolean {
        if (countTransactions(blockingOrigin) > 0) {
            return false
        }
        deleteTransactions(replacedOrigin)
        deleteCategories(replacedOrigin)
        categories.forEach { category -> insertCategory(category) }
        transactions.forEach { transaction -> insertTransaction(transaction) }
        return true
    }

    @Transaction
    open suspend fun deleteOrigin(origin: String) {
        deleteTransactions(origin)
        deleteCategories(origin)
    }

    @Transaction
    open suspend fun replaceOrigin(
        removedOrigin: String,
        category: CategoryEntity,
        transaction: TransactionEntity,
    ): Boolean {
        val removed = countTransactions(removedOrigin) > 0 || countCategories(removedOrigin) > 0
        deleteTransactions(removedOrigin)
        deleteCategories(removedOrigin)
        insertCategory(category)
        insertTransaction(transaction)
        return removed
    }
}
