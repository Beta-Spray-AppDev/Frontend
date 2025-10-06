package com.example.sprayconnectapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sprayconnectapp.data.model.BoulderEntity
import com.example.sprayconnectapp.data.model.GymEntity
import com.example.sprayconnectapp.data.model.HoldEntity
import com.example.sprayconnectapp.data.model.SpraywallEntity


/**
 * Room-Database für die App.
 * Enthält alle Entities (Gym, Spraywall, Boulder, Hold).
 */

@Database(
    entities = [
        GymEntity::class,
        SpraywallEntity::class,
        BoulderEntity::class,
        HoldEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gymDao(): GymDao
    abstract fun spraywallDao(): SpraywallDao
    abstract fun boulderDao(): BoulderDao
    abstract fun holdDao(): HoldDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val builder = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sprayconnect-db"
                )


                builder.addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5
                )

                builder.build().also { INSTANCE = it }
            }
    }
}




