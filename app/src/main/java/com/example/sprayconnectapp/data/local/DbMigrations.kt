package com.example.sprayconnectapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration von Version 1 auf 2:
 * Erstellt Tabellen für Spraywalls und Boulder.
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
 * Migration von Version 2 auf 3:
 * Erstellt die Holds-Tabelle.
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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE `spraywalls`
            ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
    }
}


val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // neue, NULL-able Spalten hinzufügen (keine Defaults nötig)
        db.execSQL("""ALTER TABLE `boulders` ADD COLUMN `avgStars` REAL""")
        db.execSQL("""ALTER TABLE `boulders` ADD COLUMN `starsCount` INTEGER""")
    }
}


