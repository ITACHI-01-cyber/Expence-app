package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.BudgetConfigEntity
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.UserSettingsEntity
import com.example.model.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class,
        BudgetConfigEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expencetrack.db"
                )
                // "No Mock Data" Rule: Start with pristine empty tables.
                // All core data is fetched live from the Render MongoDB backend upon user authentication.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
