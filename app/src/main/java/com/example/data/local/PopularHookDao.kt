package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PopularHook
import kotlinx.coroutines.flow.Flow

@Dao
interface PopularHookDao {
    @Query("SELECT * FROM popular_hooks ORDER BY (copyCount + shareCount) DESC, id ASC")
    fun getAllPopularHooksSorted(): Flow<List<PopularHook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPopularHook(hook: PopularHook)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPopularHooks(hooks: List<PopularHook>)

    @Update
    suspend fun updatePopularHook(hook: PopularHook)

    @Query("UPDATE popular_hooks SET copyCount = copyCount + 1 WHERE id = :id")
    suspend fun incrementCopyCount(id: Int)

    @Query("UPDATE popular_hooks SET shareCount = shareCount + 1 WHERE id = :id")
    suspend fun incrementShareCount(id: Int)

    @Query("SELECT * FROM popular_hooks WHERE hookText = :hookText LIMIT 1")
    suspend fun findPopularHookByText(hookText: String): PopularHook?

    @Query("SELECT COUNT(*) FROM popular_hooks")
    suspend fun getPopularHooksCount(): Int
}
