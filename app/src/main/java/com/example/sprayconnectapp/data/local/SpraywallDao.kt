
package com.example.sprayconnectapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sprayconnectapp.data.model.SpraywallEntity

@Dao
interface SpraywallDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SpraywallEntity>)

    /** Nach Gym + Archiv-Flag filtern */
    @Query("SELECT * FROM spraywalls WHERE gymId = :gymId AND isArchived = :archived ORDER BY name")
    suspend fun getByGymAndArchived(gymId: String, archived: Boolean): List<SpraywallEntity>

    @Query("DELETE FROM spraywalls WHERE id = :id")
    suspend fun deleteById(id: String): Int

    /** Für Sync hilfreich */
    @Query("DELETE FROM spraywalls WHERE gymId = :gymId AND isArchived = :archived")
    suspend fun purgeByGymAndArchived(gymId: String, archived: Boolean)
}

