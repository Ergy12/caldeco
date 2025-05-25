package com.example.finalpaycalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.finalpaycalculator.ui.theme.FinalPayCalculatorTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.finalpaycalculator.ui.screen.CalculationScreen // Import the new screen
import com.example.finalpaycalculator.ui.screen.CreateEditFormulaScreen
import com.example.finalpaycalculator.ui.screen.CreateEditInputFieldScreen
import com.example.finalpaycalculator.ui.screen.CreateEditVariableScreen
import com.example.finalpaycalculator.ui.screen.FormulasScreen
import com.example.finalpaycalculator.ui.screen.InputFieldsScreen
import com.example.finalpaycalculator.ui.screen.VariablesScreen


sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Calculator : Screen(NavRoutes.CALCULATOR, "Calculator", Icons.Filled.Calculate)
    object Formulas : Screen(NavRoutes.FORMULAS_SCREEN, "Formulas", Icons.Filled.Functions)
    object Variables : Screen(NavRoutes.VARIABLES_SCREEN, "Variables", Icons.Filled.List)
    object Inputs : Screen(NavRoutes.INPUT_FIELDS_SCREEN, "Inputs", Icons.Filled.Input)
}

val items = listOf(
    Screen.Calculator,
    Screen.Formulas,
    Screen.Variables,
    Screen.Inputs,
)

// Define navigation routes
object NavRoutes {
    const val CALCULATOR = "calculator"
    const val FORMULAS_SCREEN = "formulas_screen"
    const val VARIABLES_SCREEN = "variables_screen"
    const val INPUT_FIELDS_SCREEN = "input_fields_screen"

    const val CREATE_EDIT_VARIABLE = "create_edit_variable"
    const val CREATE_EDIT_VARIABLE_WITH_ID = "create_edit_variable/{variableId}"
    const val CREATE_EDIT_FORMULA = "create_edit_formula"
    const val CREATE_EDIT_FORMULA_WITH_ID = "create_edit_formula/{formulaId}"
    const val CREATE_EDIT_INPUT_FIELD = "create_edit_input_field"
    const val CREATE_EDIT_INPUT_FIELD_WITH_ID = "create_edit_input_field/{inputFieldId}"
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalPayCalculatorTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { navDest ->
                            // Check if the current destination's route or its parent's route matches the screen's route.
                            // This handles nested navigation graphs if any.
                            var currentRoute = navDest.route
                            var match = currentRoute == screen.route
                            if (!match) {
                                // Check parent graph if current route is part of a nested graph
                                // e.g. create_edit_variable is part of variables_screen graph
                                if (currentRoute?.startsWith(NavRoutes.CREATE_EDIT_VARIABLE) == true &&
                                    screen.route == NavRoutes.VARIABLES_SCREEN) {
                                    match = true
                                } else if (currentRoute?.startsWith(NavRoutes.CREATE_EDIT_FORMULA) == true &&
                                           screen.route == NavRoutes.FORMULAS_SCREEN) {
                                    match = true
                                } else if (currentRoute?.startsWith(NavRoutes.CREATE_EDIT_INPUT_FIELD) == true &&
                                           screen.route == NavRoutes.INPUT_FIELDS_SCREEN) {
                                    match = true
                                }
                            }
                            match
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.CALCULATOR, // Use NavRoutes
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(NavRoutes.CALCULATOR) { CalculatorScreen() }
            composable(NavRoutes.FORMULAS_SCREEN) { FormulasScreen(navController = navController) }
            composable(NavRoutes.VARIABLES_SCREEN) { VariablesScreen(navController = navController) }
            composable(NavRoutes.INPUT_FIELDS_SCREEN) { InputFieldsScreen(navController = navController) }

            composable(NavRoutes.CREATE_EDIT_VARIABLE) {
                CreateEditVariableScreen(navController = navController)
            }
            composable(
                route = NavRoutes.CREATE_EDIT_VARIABLE_WITH_ID,
                arguments = listOf(navArgument("variableId") { type = NavType.StringType })
            ) { backStackEntry ->
                CreateEditVariableScreen(
                    navController = navController,
                    variableId = backStackEntry.arguments?.getString("variableId")
                )
            }

            composable(NavRoutes.CREATE_EDIT_FORMULA) {
                CreateEditFormulaScreen(navController = navController)
            }
            composable(
                route = NavRoutes.CREATE_EDIT_FORMULA_WITH_ID,
                arguments = listOf(navArgument("formulaId") { type = NavType.StringType })
            ) { backStackEntry ->
                CreateEditFormulaScreen(
                    navController = navController,
                    formulaId = backStackEntry.arguments?.getString("formulaId")
                )
            }

            composable(NavRoutes.CREATE_EDIT_INPUT_FIELD) {
                CreateEditInputFieldScreen(navController = navController)
            }
            composable(
                route = NavRoutes.CREATE_EDIT_INPUT_FIELD_WITH_ID,
                arguments = listOf(navArgument("inputFieldId") { type = NavType.StringType })
            ) { backStackEntry ->
                CreateEditInputFieldScreen(
                    navController = navController,
                    inputFieldId = backStackEntry.arguments?.getString("inputFieldId")
                )
            }
        }
    }
}

// Placeholder CalculatorScreen function is no longer needed as it's replaced by the actual screen.
// @Composable
// fun CalculatorScreen() {
// Text(text = "Calculator Screen")
// }

// Other screens are in their respective files

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FinalPayCalculatorTheme {
        MainScreen()
    }
}
