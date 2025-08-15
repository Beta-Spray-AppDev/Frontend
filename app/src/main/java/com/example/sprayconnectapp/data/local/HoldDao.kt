package com.example.sprayconnectapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sprayconnectapp.data.model.HoldEntity

@Dao
interface HoldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HoldEntity>)

    @Query("DELETE FROM holds WHERE boulderId = :boulderId")
    suspend fun deleteByBoulder(boulderId: String): Int

    @Query("SELECT * FROM holds WHERE boulderId = :boulderId")
    suspend fun getByBoulder(boulderId: String): List<HoldEntity>
}
