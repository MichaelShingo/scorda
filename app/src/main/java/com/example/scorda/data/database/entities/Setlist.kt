package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setlists")
data class SetList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,

    )