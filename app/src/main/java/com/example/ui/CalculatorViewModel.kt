package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.CalculationHistory
import com.example.data.repository.CalculatorRepository
import com.example.util.MathEvaluator
import com.example.util.UnitConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.content.Context
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.flow.combine

class CalculatorViewModel(
    private val repository: CalculatorRepository,
    private val context: Context
) : ViewModel() {

    private val tag = "CalculatorViewModel"

    // Theme Preference State
    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val _selectedTheme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString("selected_theme", AppTheme.DEFAULT.name) ?: AppTheme.DEFAULT.name)
    )
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()

    fun setAppTheme(theme: AppTheme) {
        _selectedTheme.value = theme
        prefs.edit().putString("selected_theme", theme.name).apply()
    }

    // App Navigation Bar states
    enum class Screen {
        CALCULATOR, SOLVER, CONVERTER, HISTORY
    }

    private val _currentScreen = MutableStateFlow(Screen.CALCULATOR)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- 1. LOCAL CALCULATOR STATES ---
    private val _calcExpression = MutableStateFlow("")
    val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

    private val _calcResult = MutableStateFlow("")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(true)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _calcExplanation = MutableStateFlow<String?>(null)
    val calcExplanation: StateFlow<String?> = _calcExplanation.asStateFlow()

    private val _calcLoadingExplanation = MutableStateFlow(false)
    val calcLoadingExplanation: StateFlow<Boolean> = _calcLoadingExplanation.asStateFlow()

    private val _calcError = MutableStateFlow<String?>(null)
    val calcError: StateFlow<String?> = _calcError.asStateFlow()

    fun toggleAngleMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        // Re-evaluate calculation if an expression is already present
        if (_calcExpression.value.isNotEmpty()) {
            evaluateLocalCalculator()
        }
    }

    fun appendToExpression(value: String) {
        _calcError.value = null
        // Clear previous output result if we start a new computation after a calculation
        if (_calcResult.value.isNotEmpty() && _calcExpression.value.isEmpty()) {
            _calcResult.value = ""
            _calcExplanation.value = null
        }
        _calcExpression.value += value
    }

    fun clearCalculator() {
        _calcExpression.value = ""
        _calcResult.value = ""
        _calcExplanation.value = null
        _calcError.value = null
        _calcLoadingExplanation.value = false
    }

    fun backspaceCalculator() {
        _calcError.value = null
        val cur = _calcExpression.value
        if (cur.isNotEmpty()) {
            _calcExpression.value = cur.substring(0, cur.length - 1)
        }
    }

    fun evaluateLocalCalculator() {
        val expr = _calcExpression.value
        if (expr.trim().isEmpty()) return
        try {
            val res = MathEvaluator.evaluate(expr, _isDegreeMode.value)
            
            // Format result elegantly: eliminate trailing values if integer
            val resStr = if (res.isNaN()) {
                "Error"
            } else if (res.isInfinite()) {
                "Infinity"
            } else if (res % 1 == 0.0) {
                res.toLong().toString()
            } else {
                // Limit to 10 decimal places safely
                _calcError.value = null
                String.format("%.10g", res).replace(Regex("0+$"), "").replace(Regex("\\.$"), "")
            }
            _calcResult.value = resStr
        } catch (e: Exception) {
            _calcError.value = e.message ?: "Invalid Expression"
            _calcResult.value = ""
        }
    }

    fun requestCalculatorExplanation() {
        val expr = _calcExpression.value
        val result = _calcResult.value
        if (expr.isEmpty() || result.isEmpty()) return

        _calcLoadingExplanation.value = true
        _calcExplanation.value = null

        viewModelScope.launch {
            try {
                val prompt = "You are an expert scientific calculator assistant. Explain the step-by-step derivation and calculation of this mathematical expression: '$expr' which equals '$result'. Indicate intermediate values, mathematical operator precedence, and relevant formulas in a clear, formatted markdown. Give concise context when applicable."
                val responseText = callGeminiApi(prompt)
                
                _calcExplanation.value = responseText

                // Save calculation history to Database
                withContext(Dispatchers.IO) {
                    repository.insert(
                        CalculationHistory(
                            expression = expr,
                            result = result,
                            explanation = responseText,
                            category = "CALCULATOR"
                        )
                    )
                }
            } catch (e: Exception) {
                _calcExplanation.value = "Error generating explanation: ${e.message}\nPlease check your internet connection or verify your API key in the Secrets Panel."
                Log.e(tag, "Calculator explanation failed", e)
            } finally {
                _calcLoadingExplanation.value = false
            }
        }
    }

    // --- 2. AI SOLVER STATES ---
    private val _solverProblemInput = MutableStateFlow("")
    val solverProblemInput: StateFlow<String> = _solverProblemInput.asStateFlow()

    private val _solverExplanation = MutableStateFlow<String?>(null)
    val solverExplanation: StateFlow<String?> = _solverExplanation.asStateFlow()

    private val _solverLoading = MutableStateFlow(false)
    val solverLoading: StateFlow<Boolean> = _solverLoading.asStateFlow()

    fun updateProblemInput(input: String) {
        _solverProblemInput.value = input
    }

    fun solveProblem() {
        val problem = _solverProblemInput.value.trim()
        if (problem.isEmpty()) return

        _solverLoading.value = true
        _solverExplanation.value = null

        viewModelScope.launch {
            try {
                val prompt = "You are an expert advanced math, science, and engineering tutor. Solve the following scientific or mathematical problem completely: '$problem'. Highlight individual steps, intermediate formulas, derivations, and finalize with the explicit clear answer. Structure beautifully in Markdown."
                val responseText = callGeminiApi(prompt)
                
                _solverExplanation.value = responseText

                // Save to history
                withContext(Dispatchers.IO) {
                    repository.insert(
                        CalculationHistory(
                            expression = problem,
                            result = "Solved via AI",
                            explanation = responseText,
                            category = "SOLVER"
                        )
                    )
                }
            } catch (e: Exception) {
                _solverExplanation.value = "Error solving problem: ${e.message}\nVerify your internet connections and verify your Secrets configuration."
                Log.e(tag, "AI Solver failed", e)
            } finally {
                _solverLoading.value = false
            }
        }
    }

    fun clearSolver() {
        _solverProblemInput.value = ""
        _solverExplanation.value = null
        _solverLoading.value = false
    }

    // --- 3. UNIT CONVERTER STATES ---
    private val _convertCategory = MutableStateFlow(UnitConverter.Category.LENGTH)
    val convertCategory: StateFlow<UnitConverter.Category> = _convertCategory.asStateFlow()

    private val _convertFromValue = MutableStateFlow("1.0")
    val convertFromValue: StateFlow<String> = _convertFromValue.asStateFlow()

    private val _convertFromUnit = MutableStateFlow("m")
    val convertFromUnit: StateFlow<String> = _convertFromUnit.asStateFlow()

    private val _convertToUnit = MutableStateFlow("km")
    val convertToUnit: StateFlow<String> = _convertToUnit.asStateFlow()

    private val _convertResultValue = MutableStateFlow("0.001")
    val convertResultValue: StateFlow<String> = _convertResultValue.asStateFlow()

    private val _converterExplanation = MutableStateFlow<String?>(null)
    val converterExplanation: StateFlow<String?> = _converterExplanation.asStateFlow()

    private val _converterLoadingExplanation = MutableStateFlow(false)
    val converterLoadingExplanation: StateFlow<Boolean> = _converterLoadingExplanation.asStateFlow()

    fun setConvertCategory(category: UnitConverter.Category) {
        _convertCategory.value = category
        val units = UnitConverter.getUnits(category)
        _convertFromUnit.value = units.getOrNull(0)?.abbrev ?: ""
        _convertToUnit.value = units.getOrNull(1)?.abbrev ?: (units.getOrNull(0)?.abbrev ?: "")
        _converterExplanation.value = null
        performLocalConversion()
    }

    fun setConvertFromUnit(unitAbbrev: String) {
        _convertFromUnit.value = unitAbbrev
        performLocalConversion()
    }

    fun setConvertToUnit(unitAbbrev: String) {
        _convertToUnit.value = unitAbbrev
        performLocalConversion()
    }

    fun updateConvertFromValue(value: String) {
        _convertFromValue.value = value
        performLocalConversion()
    }

    private fun performLocalConversion() {
        val valDouble = _convertFromValue.value.toDoubleOrNull()
        if (valDouble == null) {
            _convertResultValue.value = "Invalid Input"
            return
        }
        try {
            val res = UnitConverter.convert(
                value = valDouble,
                fromUnit = _convertFromUnit.value,
                toUnit = _convertToUnit.value,
                category = _convertCategory.value
            )
            val resStr = if (res % 1 == 0.0) {
                res.toLong().toString()
            } else {
                String.format("%.8g", res).replace(Regex("0+$"), "").replace(Regex("\\.$"), "")
            }
            _convertResultValue.value = resStr
        } catch (e: Exception) {
            _convertResultValue.value = "Error"
        }
    }

    fun explainConversionFormula() {
        val fromVal = _convertFromValue.value
        val fromUnit = _convertFromUnit.value
        val toVal = _convertResultValue.value
        val toUnit = _convertToUnit.value
        val categoryStr = _convertCategory.value.name

        if (fromVal.isEmpty() || toVal.isEmpty() || toVal == "Error" || toVal == "Invalid Input") return

        _converterLoadingExplanation.value = true
        _converterExplanation.value = null

        viewModelScope.launch {
            try {
                val prompt = "You are an expert physics tutor. Complete a structured calculation and formula analysis. Explain how to convert $fromVal $fromUnit to $toVal $toUnit for $categoryStr. Outline the specific physical conversion formula, basic derivation steps, real-world context of these units, and calculate intermediate equations clearly. Render in polished markdown."
                val responseText = callGeminiApi(prompt)
                _converterExplanation.value = responseText

                // Save to Database
                withContext(Dispatchers.IO) {
                    repository.insert(
                        CalculationHistory(
                            expression = "Convert $fromVal $fromUnit to $toUnit",
                            result = "$toVal $toUnit",
                            explanation = responseText,
                            category = "CONVERTER"
                        )
                    )
                }
            } catch (e: Exception) {
                _converterExplanation.value = "Error creating math steps: ${e.message}\nPlease verify your API connections or secrets."
                Log.e(tag, "Conversion explanation failed", e)
            } finally {
                _converterLoadingExplanation.value = false
            }
        }
    }


    // --- 4. HISTORY AND SAVED ITEMS STATES ---
    val allHistory: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    fun updateHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    val filteredHistory: StateFlow<List<CalculationHistory>> = combine(
        allHistory,
        _historySearchQuery
    ) { history, sqlQuery ->
        if (sqlQuery.isBlank()) {
            history
        } else {
            history.filter {
                it.expression.contains(sqlQuery, ignoreCase = true) ||
                it.result.contains(sqlQuery, ignoreCase = true) ||
                (it.explanation ?: "").contains(sqlQuery, ignoreCase = true) ||
                it.category.contains(sqlQuery, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedHistoryItem = MutableStateFlow<CalculationHistory?>(null)
    val selectedHistoryItem: StateFlow<CalculationHistory?> = _selectedHistoryItem.asStateFlow()

    fun selectHistoryItem(item: CalculationHistory?) {
        _selectedHistoryItem.value = item
    }

    fun deleteHistoryItem(item: CalculationHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun rerunHistoryItem(item: CalculationHistory) {
        when (item.category) {
            "CALCULATOR" -> {
                _calcExpression.value = item.expression
                _calcResult.value = item.result
                _calcExplanation.value = item.explanation
                _currentScreen.value = Screen.CALCULATOR
            }
            "SOLVER" -> {
                _solverProblemInput.value = item.expression
                _solverExplanation.value = item.explanation
                _currentScreen.value = Screen.SOLVER
            }
            "CONVERTER" -> {
                try {
                    val parts = item.expression.split(" ")
                    if (parts.size >= 5 && parts[0].equals("Convert", ignoreCase = true)) {
                        val fromValStr = parts[1]
                        val fromUnitStr = parts[2]
                        val toUnitStr = parts[4]

                        val foundCategory = UnitConverter.categories.firstOrNull { cat ->
                            UnitConverter.getUnits(cat).any { it.abbrev == fromUnitStr }
                        }

                        if (foundCategory != null) {
                            _convertCategory.value = foundCategory
                            _convertFromValue.value = fromValStr
                            _convertFromUnit.value = fromUnitStr
                            _convertToUnit.value = toUnitStr

                            val resultParts = item.result.split(" ")
                            _convertResultValue.value = resultParts.firstOrNull() ?: ""
                            _converterExplanation.value = item.explanation
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalculatorViewModel", "Failed to parse converter history for rerun", e)
                }
                _currentScreen.value = Screen.CONVERTER
            }
        }
    }


    // --- SERVICE LAYER CONNECTION ---
    private suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        val response = RetrofitClient.service.generateContent(request = request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        responseText ?: "Unable to parse server results. Verify API Key settings."
    }
}

class CalculatorViewModelFactory(
    private val repository: CalculatorRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
