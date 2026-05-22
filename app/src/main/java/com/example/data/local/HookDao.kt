package com.example.data.local

import androidx.room.*
import com.example.data.model.SavedHook
import kotlinx.coroutines.flow.Flow

@Dao
interface HookDao {
    @Query("SELECT * FROM saved_hooks ORDER BY timestamp DESC")
    fun getAllSavedHooks(): Flow<List<SavedHook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHook(hook: SavedHook)

    @Delete
    suspend fun deleteHook(hook: SavedHook)

    @Query("DELETE FROM saved_hooks WHERE id = :id")
    suspend fun deleteHookById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_hooks WHERE hookText = :hookText LIMIT 1)")
    suspend fun isHookSaved(hookText: String): Boolean
}
