
package com.example.sprayconnectapp.util

import java.util.UUID

object Superusers {

    private val IDS = setOf(
        UUID.fromString( "e1232010-c83a-494f-9cb7-4ce8f503d75a"),
        UUID.fromString("9d6305c6-0052-48da-8fa9-3ee162260812"),
        UUID.fromString("0b6ae861-1f72-4cc4-861b-aa9ff8421e45"),
        UUID.fromString("842a8734-4125-4d25-9a88-48ba0520ac91"),


    )

    fun isSuper(userId: String?): Boolean {
        if (userId.isNullOrBlank()) return false
        return runCatching { UUID.fromString(userId) }.getOrNull()?.let { IDS.contains(it) } == true
    }
}
