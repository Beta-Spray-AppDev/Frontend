package com.example.sprayconnectapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.sprayconnectapp.data.model.BoulderEntity

@Dao
interface BoulderDao {

    /** Alle Boulder einer Spraywall laden */
    @Query("SELECT * FROM boulders WHERE spraywallId = :spraywallId ORDER BY name")
    suspend fun getBySpraywall(spraywallId: String): List<BoulderEntity>


    /** Einzelnen Boulder nach ID laden */
    @Query("SELECT * FROM boulders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BoulderEntity?

    /** Mehrere/alle Boulder einfügen/ersetzen */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BoulderEntity>)


    /** Boulder einfügen oder updaten */
    @Upsert
    suspend fun upsert(item: BoulderEntity)

    /** Boulder nach ID löschen */
    @Query("DELETE FROM boulders WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
