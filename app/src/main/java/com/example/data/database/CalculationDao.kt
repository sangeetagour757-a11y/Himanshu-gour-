package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    @Query("SELECT * FROM calculation_history WHERE category = :category ORDER BY timestamp DESC")
    fun getHistoryByCategory(category: String): Flow<List<CalculationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalculationHistory): Long

    @Delete
    suspend fun deleteHistory(item: CalculationHistory)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()
}
