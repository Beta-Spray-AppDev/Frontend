
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


    /** Alle Spraywalls eines Gyms laden */
    @Query("SELECT * FROM spraywalls WHERE gymId = :gymId ORDER BY name")
    suspend fun getByGym(gymId: String): List<SpraywallEntity>


    /** Spraywall nach ID löschen */
    @Query("DELETE FROM spraywalls WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
