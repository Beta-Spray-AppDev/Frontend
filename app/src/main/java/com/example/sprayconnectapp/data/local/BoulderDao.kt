package com.example.sprayconnectapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.sprayconnectapp.data.model.BoulderEntity

@Dao
interface BoulderDao {
    @Query("SELECT * FROM boulders WHERE spraywallId = :spraywallId ORDER BY name")
    suspend fun getBySpraywall(spraywallId: String): List<BoulderEntity>

    @Query("SELECT * FROM boulders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BoulderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BoulderEntity>)

    @Upsert
    suspend fun upsert(item: BoulderEntity)

    @Query("DELETE FROM boulders WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
