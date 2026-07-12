package com.example.scorda.util

import com.example.scorda.data.database.entities.Composer

fun getCommaSeparatedFullName(composer: Composer): String {
    if (composer.firstName.isNullOrEmpty()) {
        return composer.lastName
    }
    return "${composer.lastName}, ${composer.firstName}"
}

fun parseComposerName(input: String): Pair<String?, String> {
    val trimmed = input.trim()

    if (trimmed.contains(",")) {
        val nameList = trimmed.split(",")
        return nameList[1].trim() to nameList[0].trim()
    }

    val parts = trimmed.split("\\s+".toRegex())

    return when {
        parts.isEmpty() -> null to ""
        parts.size == 1 -> null to parts[0]
        else -> {
            val lastName = parts.last()
            val firstName = parts.dropLast(1).joinToString(" ")
            firstName to lastName
        }
    }
}
