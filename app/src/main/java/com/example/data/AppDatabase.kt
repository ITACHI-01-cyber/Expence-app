package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.BudgetConfigEntity
import com.example.model.CardDisplayBalanceEntity
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.UserSettingsEntity
import com.example.model.WalletCustomizationEntity
import com.example.model.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        WalletCustomizationEntity::class,
        CardDisplayBalanceEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class,
        BudgetConfigEntity::class,
        UserSettingsEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS card_display_balances (
                        walletId TEXT NOT NULL PRIMARY KEY,
                        balance REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE wallets ADD COLUMN cardName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE wallets ADD COLUMN creditLimit REAL")
                database.execSQL("ALTER TABLE wallets ADD COLUMN outstandingBalance REAL")
                database.execSQL("ALTER TABLE wallets ADD COLUMN statementClosingDay INTEGER")
                database.execSQL("ALTER TABLE wallets ADD COLUMN paymentDueDay INTEGER")
                database.execSQL("ALTER TABLE wallets ADD COLUMN minimumPaymentAmount REAL")
                database.execSQL("ALTER TABLE wallets ADD COLUMN creditTermsConfigured INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE wallets ADD COLUMN timezone TEXT NOT NULL DEFAULT 'UTC'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN transactionKind TEXT NOT NULL DEFAULT 'standard'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN idempotencyKey TEXT")
                database.execSQL("ALTER TABLE transactions ADD COLUMN statementId TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN relatedTransactionId TEXT")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_settings ADD COLUMN profilePictureUri TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_settings ADD COLUMN wallpaperUri TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN wallpaperDarkness REAL NOT NULL DEFAULT 0.40")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wallet_customizations (
                        walletId TEXT NOT NULL PRIMARY KEY,
                        cardTheme TEXT NOT NULL DEFAULT 'midnight',
                        primaryColor TEXT NOT NULL DEFAULT '#1A1A2E',
                        secondaryColor TEXT NOT NULL DEFAULT '#16213E',
                        accentColor TEXT NOT NULL DEFAULT '#38BDF8',
                        artwork TEXT NOT NULL DEFAULT 'waves',
                        cardStyle TEXT NOT NULL DEFAULT 'illustrated',
                        textColor TEXT NOT NULL DEFAULT '#FFFFFF',
                        cardIcon TEXT NOT NULL DEFAULT 'none',
                        lastUpdated INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO wallet_customizations (
                        walletId, cardTheme, primaryColor, secondaryColor, accentColor, artwork, cardStyle, textColor, cardIcon, lastUpdated
                    )
                    SELECT id, cardTheme, primaryColor, secondaryColor, accentColor, artwork, cardStyle, textColor, cardIcon, ${System.currentTimeMillis()}
                    FROM wallets
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expencetrack.db"
                )
                .addMigrations(
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
