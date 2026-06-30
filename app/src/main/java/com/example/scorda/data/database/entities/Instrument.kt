package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instruments")
data class Instrument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)