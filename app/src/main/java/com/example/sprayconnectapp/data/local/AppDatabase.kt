package com.example.sprayconnectapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sprayconnectapp.data.model.GymEntity

@Database(
    entities = [GymEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gymDao(): GymDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sprayconnect-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}



