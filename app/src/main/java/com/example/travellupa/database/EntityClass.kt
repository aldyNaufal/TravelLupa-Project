package com.example.travellupa.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val localPath: String,
    val tempatWisataId: String? = null,
    val createdAt: Long
)


