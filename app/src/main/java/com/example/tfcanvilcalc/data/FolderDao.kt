package com.example.tfcanvilcalc.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>
    
    @Insert
    suspend fun insertFolder(folder: FolderEntity): Long
    
    @Update
    suspend fun updateFolder(folder: FolderEntity)
    
    @Delete
    suspend fun deleteFolder(folder: FolderEntity)
    
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Int): FolderEntity?
} 