package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.BudgetConfigEntity
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.UserSettingsEntity
import com.example.model.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // ── Wallets ──
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    suspend fun getAllWalletsList(): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET balance = balance + :amount WHERE id = :id")
    suspend fun addMoneyToWallet(id: String, amount: Double)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteWalletById(id: String)

    @Query("DELETE FROM wallets")
    suspend fun clearWallets()

    // ── Transactions ──
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    // ── Savings Goals ──
    @Query("SELECT * FROM savings_goals ORDER BY completed ASC, title ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals ORDER BY completed ASC, title ASC")
    suspend fun getAllGoalsList(): List<SavingsGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET completed = :completed WHERE id = :id")
    suspend fun updateGoalStatus(id: String, completed: Boolean)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("DELETE FROM savings_goals")
    suspend fun clearGoals()

    // ── Budget Config ──
    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    fun getBudgetConfig(): Flow<BudgetConfigEntity?>

    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    suspend fun getBudgetConfigOnce(): BudgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBudgetConfig(config: BudgetConfigEntity)

    // ── User Settings ──
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsOnce(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
}
