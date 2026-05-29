package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val expression by viewModel.calcExpression.collectAsStateWithLifecycle()
    val result by viewModel.calcResult.collectAsStateWithLifecycle()
    val error by viewModel.calcError.collectAsStateWithLifecycle()
    val isDegreeMode by viewModel.isDegreeMode.collectAsStateWithLifecycle()
    val explanation by viewModel.calcExplanation.collectAsStateWithLifecycle()
    val isLoadingExplanation by viewModel.calcLoadingExplanation.collectAsStateWithLifecycle()

    var showExplanationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(explanation) {
        if (explanation != null) {
            showExplanationDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // DISPLAY AREA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Mode Indicator Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { viewModel.toggleAngleMode() },
                        label = { Text(if (isDegreeMode) "DEG" else "RAD", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("mode_chip")
                    )
                    
                    if (result.isNotEmpty() || expression.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearCalculator() },
                            modifier = Modifier.testTag("clear_icon")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear")
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Input mathematically formatted string
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = if (expression.length > 15) 24.sp else 36.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("calc_display")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (result.isNotEmpty()) {
                        Text(
                            text = "= $result",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().testTag("result_display")
                        )
                    }
                }
            }
        }

        // TRIGGER ADVANCED EXPLANATION IF RESULT IS READY
        if (result.isNotEmpty() && error == null) {
            Button(
                onClick = { viewModel.requestCalculatorExplanation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp)
                    .testTag("explain_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoadingExplanation
            ) {
                if (isLoadingExplanation) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI is generating derivation...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Explain Steps with AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // KEYBOARD CORES
        val keyRows = listOf(
            listOf("sin", "cos", "tan", "sqrt"),
            listOf("ln", "log", "^", "pi"),
            listOf("e", "abs", "(", ")"),
            listOf("C", "DEL", "%", "/"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "deg", "=")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in keyRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (key in row) {
                        CalculatorKey(
                            key = key,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (key) {
                                    "C" -> viewModel.clearCalculator()
                                    "DEL" -> viewModel.backspaceCalculator()
                                    "=" -> viewModel.evaluateLocalCalculator()
                                    "deg" -> viewModel.toggleAngleMode()
                                    "sin", "cos", "tan", "sqrt", "log", "ln", "abs" -> viewModel.appendToExpression("$key(")
                                    else -> viewModel.appendToExpression(key)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // EXPLANATION DETAILS DIALOG
    if (showExplanationDialog && explanation != null) {
        Dialog(
            onDismissRequest = { showExplanationDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step-by-Step Derivation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showExplanationDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Calculation:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "$expression = $result",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        MarkdownText(
                            text = explanation ?: "",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorKey(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = key in listOf("/", "×", "-", "+", "=")
    val isScientific = key in listOf("sin", "cos", "tan", "sqrt", "ln", "log", "^", "pi", "e", "abs", "(", ")", "deg")
    val isAction = key in listOf("C", "DEL", "%")

    val containerColor = when {
        key == "=" -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.secondaryContainer
        isAction -> MaterialTheme.colorScheme.errorContainer
        isScientific -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        key == "=" -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
        isAction -> MaterialTheme.colorScheme.onErrorContainer
        isScientific -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable { onClick() }
            .testTag("key_$key"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (key == "deg") "DEG" else key,
            fontSize = if (key.length > 3) 14.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
