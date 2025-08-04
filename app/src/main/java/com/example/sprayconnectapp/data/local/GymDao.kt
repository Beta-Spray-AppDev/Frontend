package com.example.sprayconnectapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sprayconnectapp.data.model.GymEntity

@Dao
interface GymDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gym: GymEntity)

    @Query("SELECT * FROM gyms WHERE id = :id")
    suspend fun getById(id: String): GymEntity?

    @Query("SELECT * FROM gyms")
    suspend fun getAll(): List<GymEntity>

    @Query("DELETE FROM gyms WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM gyms WHERE isPinned = 0 AND lastAccessed < :threshold")
    suspend fun deleteStaleGyms(threshold: Long)
}
