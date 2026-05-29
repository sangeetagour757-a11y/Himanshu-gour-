package com.example.data.repository

import com.example.data.database.CalculationDao
import com.example.data.database.CalculationHistory
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val calculationDao: CalculationDao) {
    val allHistory: Flow<List<CalculationHistory>> = calculationDao.getAllHistory()

    fun getHistoryByCategory(category: String): Flow<List<CalculationHistory>> =
        calculationDao.getHistoryByCategory(category)

    suspend fun insert(item: CalculationHistory): Long =
        calculationDao.insertHistory(item)

    suspend fun delete(item: CalculationHistory) =
        calculationDao.deleteHistory(item)

    suspend fun deleteById(id: Int) =
        calculationDao.deleteById(id)

    suspend fun clearAll() =
        calculationDao.clearAllHistory()
}
