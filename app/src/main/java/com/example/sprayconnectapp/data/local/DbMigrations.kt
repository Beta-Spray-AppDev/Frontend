package com.example.sprayconnectapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
