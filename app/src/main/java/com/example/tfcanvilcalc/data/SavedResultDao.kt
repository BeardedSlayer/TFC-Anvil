package com.example.tfcanvilcalc.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedResultDao {
    @Query("SELECT * FROM saved_results")
    fun getAllResults(): Flow<List<SavedResultEntity>>
    
    @Query("SELECT * FROM saved_results WHERE folderId = :folderId")
    fun getResultsByFolder(folderId: Int): Flow<List<SavedResultEntity>>
    
    @Query("SELECT * FROM saved_results WHERE folderId IS NULL")
    fun getResultsWithoutFolder(): Flow<List<SavedResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: SavedResultEntity)

    @Delete
    suspend fun deleteResult(result: SavedResultEntity)

    @Query("UPDATE saved_results SET name = :newName WHERE id = :id")
    suspend fun updateResultName(id: Int, newName: String)
    
    @Query("UPDATE saved_results SET folderId = :folderId WHERE id = :resultId")
    suspend fun moveResultToFolder(resultId: Int, folderId: Int?)
} 