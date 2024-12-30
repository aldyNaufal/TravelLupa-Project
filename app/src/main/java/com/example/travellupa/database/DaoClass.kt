package com.example.travellupa.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow


@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: ImageEntity): Long

    @Query("SELECT * FROM images WHERE id = :imageId")
    fun getImageById(imageId: Long): ImageEntity?

    @Query("SELECT * FROM images WHERE tempatWisataId = :firestoreId")
    fun getImageByTempatWisataId(firestoreId: String): ImageEntity?

    @Query("SELECT * FROM images")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Delete
    fun delete(image: ImageEntity)


    @Query("SELECT * FROM images")
    fun getAllImagesFlow(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE tempatWisataId = :firestoreId")
    fun getImageByTempatWisataIdFlow(firestoreId: String): Flow<List<ImageEntity>>
}

