package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.repository.CalculatorRepository
import com.example.ui.CalculatorViewModel
import com.example.ui.CalculatorViewModelFactory
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SolverScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup database and repository
        val database = AppDatabase.getDatabase(this)
        val repository = CalculatorRepository(database.calculationDao())
        
        // Edge to edge immersive display
        enableEdgeToEdge()
        
        setContent {
            // Instantiate ViewModel securely with Custom Factory
            val viewModel: CalculatorViewModel by viewModels {
                CalculatorViewModelFactory(repository, applicationContext)
            }

            val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
            
            MyApplicationTheme(themeMode = currentTheme) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: CalculatorViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            CalculatorViewModel.Screen.CALCULATOR -> "Scientific Calculator"
                            CalculatorViewModel.Screen.SOLVER -> "AI Scholar Solver"
                            CalculatorViewModel.Screen.CONVERTER -> "Smart Unit Converter"
                            CalculatorViewModel.Screen.HISTORY -> "Offline Derivations"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                // Calculator Tab
                NavigationBarItem(
                    selected = currentScreen == CalculatorViewModel.Screen.CALCULATOR,
                    onClick = { viewModel.navigateTo(CalculatorViewModel.Screen.CALCULATOR) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Calculator") },
                    label = { Text("Calculator", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_calculator")
                )

                // Solver Tab
                NavigationBarItem(
                    selected = currentScreen == CalculatorViewModel.Screen.SOLVER,
                    onClick = { viewModel.navigateTo(CalculatorViewModel.Screen.SOLVER) },
                    icon = { Icon(Icons.Default.Send, contentDescription = "AI Solver") },
                    label = { Text("AI Solver", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_solver")
                )

                // Converter Tab
                NavigationBarItem(
                    selected = currentScreen == CalculatorViewModel.Screen.CONVERTER,
                    onClick = { viewModel.navigateTo(CalculatorViewModel.Screen.CONVERTER) },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Converter") },
                    label = { Text("Converter", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_converter")
                )

                // History Tab
                NavigationBarItem(
                    selected = currentScreen == CalculatorViewModel.Screen.HISTORY,
                    onClick = { viewModel.navigateTo(CalculatorViewModel.Screen.HISTORY) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                CalculatorViewModel.Screen.CALCULATOR -> CalculatorScreen(viewModel = viewModel)
                CalculatorViewModel.Screen.SOLVER -> SolverScreen(viewModel = viewModel)
                CalculatorViewModel.Screen.CONVERTER -> ConverterScreen(viewModel = viewModel)
                CalculatorViewModel.Screen.HISTORY -> HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
