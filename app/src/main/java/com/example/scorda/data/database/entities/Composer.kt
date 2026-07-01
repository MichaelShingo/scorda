package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "composers")
data class Composer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String?,
    val lastName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


/*
When the user types a name that doesn't exist (e.g., "Clara Schumann") and clicks "Create New":
1.
Simple Logic: In your ViewModel, split the string by the last space.
◦
firstName = "Clara"
◦
lastName = "Schumann"
2.
Edge Cases: If they only type one name ("Prince"), put it in lastName and leave firstName empty
 */