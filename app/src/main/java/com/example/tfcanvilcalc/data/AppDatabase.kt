package com.example.tfcanvilcalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SavedResultEntity::class, FolderEntity::class], version = 2, exportSchema = false)
@TypeConverters(ActionListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedResultDao(): SavedResultDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anvil_calc_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем таблицу папок
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
                
                // Добавляем колонку folderId в таблицу saved_results
                database.execSQL(
                    "ALTER TABLE `saved_results` ADD COLUMN `folderId` INTEGER"
                )
            }
        }
    }
} 