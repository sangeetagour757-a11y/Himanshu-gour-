package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.components.MarkdownText
import com.example.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(viewModel: CalculatorViewModel) {
    val activeCategory by viewModel.convertCategory.collectAsStateWithLifecycle()
    val fromValue by viewModel.convertFromValue.collectAsStateWithLifecycle()
    val fromUnit by viewModel.convertFromUnit.collectAsStateWithLifecycle()
    val toUnit by viewModel.convertToUnit.collectAsStateWithLifecycle()
    val resultValue by viewModel.convertResultValue.collectAsStateWithLifecycle()
    val explanation by viewModel.converterExplanation.collectAsStateWithLifecycle()
    val isLoadingExplanation by viewModel.converterLoadingExplanation.collectAsStateWithLifecycle()

    val units = UnitConverter.getUnits(activeCategory)

    var fromMenuExpanded by remember { mutableStateOf(false) }
    var toMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Categories horizontal selection list
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Unit Conversion Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UnitConverter.categories) { cat ->
                        val isSelected = cat == activeCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setConvertCategory(cat) },
                            label = { Text(cat.name) },
                            modifier = Modifier.testTag("chip_${cat.name}")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)

                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("km → mi", Pair(UnitConverter.Category.LENGTH, Pair("km", "mi")), "1.0"),
                        Triple("mi → km", Pair(UnitConverter.Category.LENGTH, Pair("mi", "km")), "1.0"),
                        Triple("kg → lbs", Pair(UnitConverter.Category.MASS, Pair("kg", "lbs")), "1.0"),
                        Triple("lbs → kg", Pair(UnitConverter.Category.MASS, Pair("lbs", "kg")), "1.0"),
                        Triple("°C → °F", Pair(UnitConverter.Category.TEMPERATURE, Pair("°C", "°F")), "0.0"),
                        Triple("°F → °C", Pair(UnitConverter.Category.TEMPERATURE, Pair("°F", "°C")), "32.0"),
                        Triple("USD → EUR", Pair(UnitConverter.Category.CURRENCY, Pair("USD", "EUR")), "100.0"),
                        Triple("EUR → USD", Pair(UnitConverter.Category.CURRENCY, Pair("EUR", "USD")), "100.0")
                    )

                    items(presets) { preset ->
                        val label = preset.first
                        val category = preset.second.first
                        val fromU = preset.second.second.first
                        val toU = preset.second.second.second
                        val defVal = preset.third
                        
                        SuggestionChip(
                            onClick = {
                                viewModel.setConvertCategory(category)
                                viewModel.setConvertFromUnit(fromU)
                                viewModel.setConvertToUnit(toU)
                                viewModel.updateConvertFromValue(defVal)
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Conversion Fields Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "From" Source Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = fromValue,
                        onValueChange = { viewModel.updateConvertFromValue(it) },
                        label = { Text("From Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("from_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Source Unit Dropdown Button
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { fromMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("from_unit_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(text = fromUnit, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        DropdownMenu(
                            expanded = fromMenuExpanded,
                            onDismissRequest = { fromMenuExpanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.abbrev})") },
                                    onClick = {
                                        viewModel.setConvertFromUnit(unit.abbrev)
                                        fromMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // "To" target Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = resultValue,
                        onValueChange = {},
                        label = { Text("Result") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("to_result_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Target Unit Dropdown Button
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { toMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("to_unit_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(text = toUnit, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        DropdownMenu(
                            expanded = toMenuExpanded,
                            onDismissRequest = { toMenuExpanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.abbrev})") },
                                    onClick = {
                                        viewModel.setConvertToUnit(unit.abbrev)
                                        toMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Formula Explainer Button
        Button(
            onClick = { viewModel.explainConversionFormula() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("explain_conversion_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoadingExplanation && fromValue.isNotEmpty()
        ) {
            if (isLoadingExplanation) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing physical conversion...", fontWeight = FontWeight.Bold)
            } else {
                Text("Explain Formula with AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Scrollable AI Formula Output
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (explanation != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Conversion Formula Derivation:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        MarkdownText(
                            text = explanation ?: "",
                            modifier = Modifier.fillMaxWidth().testTag("converter_explanation_text")
                        )
                    }
                } else if (isLoadingExplanation) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Formulating scientific derivation...", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Want to know the physical logic?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Tap 'Explain Formula' above to derive equations step-by-step",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
