package com.example.travellupa.database

import android.content.Context
import androidx.room.Room

object DatabaseInstance {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "travelupa-database"
            )
                .addMigrations(MIGRATION_1_2) // Pastikan migrasi ditambahkan di sini
                .fallbackToDestructiveMigration() // Hapus data jika tidak ada migrasi
                .build()
            INSTANCE = instance
            instance
        }
    }
}
