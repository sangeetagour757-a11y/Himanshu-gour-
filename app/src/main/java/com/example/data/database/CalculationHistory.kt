package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val explanation: String?,
    val category: String, // "CALCULATOR", "SOLVER", "CONVERTER"
    val timestamp: Long = System.currentTimeMillis()
)
