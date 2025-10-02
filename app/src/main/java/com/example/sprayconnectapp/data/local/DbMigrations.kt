package com.example.sprayconnectapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Hilfsfunktion: prüft, ob eine Spalte existiert.
 */
private fun hasColumn(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
    db.query("PRAGMA table_info(`$table`)").use { c ->
        val nameIdx = c.getColumnIndex("name")
        while (c.moveToNext()) {
            if (c.getString(nameIdx) == column) return true
        }
    }
    return false
}

/**
 * 1 -> 2
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `spraywalls`(
                `id` TEXT NOT NULL,
                `gymId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `photoUrl` TEXT NOT NULL,
                `isPublic` INTEGER NOT NULL,
                `createdBy` TEXT,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `boulders`(
                `id` TEXT NOT NULL,
                `spraywallId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `difficulty` TEXT NOT NULL,
                `holdsJson` TEXT NOT NULL,
                `createdBy` TEXT,
                `createdAt` INTEGER,
                `lastUpdated` INTEGER,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

/**
 * 2 -> 3
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `holds`(
                `id` TEXT NOT NULL,
                `boulderId` TEXT NOT NULL,
                `x` REAL NOT NULL,
                `y` REAL NOT NULL,
                `type` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

/**
 * 3 -> 4  (idempotent)
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!hasColumn(db, "spraywalls", "isArchived")) {
            db.execSQL("""
                ALTER TABLE `spraywalls`
                ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0
            """.trimIndent())
        }
    }
}

/**
 * 4 -> 5  (auch idempotent machen – schadet nie)
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!hasColumn(db, "boulders", "setterNote")) {
            db.execSQL("""
                ALTER TABLE `boulders`
                ADD COLUMN `setterNote` TEXT
            """.trimIndent())
        }
    }
}
