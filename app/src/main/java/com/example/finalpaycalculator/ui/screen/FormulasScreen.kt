package com.example.finalpaycalculator.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.viewmodel.FormulaViewModel
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import com.example.finalpaycalculator.ui.NavRoutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun FormulasScreen(
    navController: NavController,
    formulaViewModel: FormulaViewModel = viewModel()
) {
    val formulas by formulaViewModel.formulas.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(NavRoutes.CREATE_EDIT_FORMULA) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Formula")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(formulas, key = { it.id }) { formula ->
                val dismissState = rememberDismissState(
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                            formulaViewModel.deleteFormula(formula.id)
                            true
                        } else false
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                    background = {
                        val color = when (dismissState.dismissDirection) {
                            DismissDirection.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                            DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            null -> MaterialTheme.colorScheme.surface
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterEnd // Or Start for other swipe direction
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Icon",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    dismissContent = {
                        FormulaListItem(formula = formula) {
                            navController.navigate("${NavRoutes.CREATE_EDIT_FORMULA}/${formula.id}")
                        }
                    }
                )
                Divider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaListItem(formula: Formula, onClick: () -> Unit) {
    ListItem(
        headlineText = { Text(formula.name) },
        supportingText = {
            val description = if (formula.conditions != null && formula.conditions.isNotEmpty()) {
                "Conditional: ${formula.conditions.size} condition(s)" +
                        if (formula.defaultExpression != null) ", with default" else ""
            } else if (formula.expression != null) {
                formula.expression.elements.joinToString(separator = " ") {
                    when (it) {
                        is com.example.finalpaycalculator.data.model.VariableElement -> "Var(${it.variableId.take(4)})" // Simplified
                        is com.example.finalpaycalculator.data.model.OperatorElement -> it.operator
                        is com.example.finalpaycalculator.data.model.LiteralElement -> it.value
                        else -> ""
                    }
                }.take(50) + if (formula.expression.elements.joinToString("").length > 50) "..." else ""
            } else {
                "No expression defined"
            }
            Text(description)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
