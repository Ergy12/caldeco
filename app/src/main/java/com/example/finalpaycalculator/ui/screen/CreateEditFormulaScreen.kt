package com.example.finalpaycalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.finalpaycalculator.data.model.ExpressionElement
import com.example.finalpaycalculator.data.model.LiteralElement
import com.example.finalpaycalculator.data.model.OperatorElement
import com.example.finalpaycalculator.data.model.VariableElement
import com.example.finalpaycalculator.viewmodel.FormulaViewModel
// import com.google.accompanist.flowlayout.FlowRow // Not used anymore
import com.example.finalpaycalculator.data.model.Expression
import com.example.finalpaycalculator.data.model.ConditionalExpression
import com.example.finalpaycalculator.ui.component.ExpressionBuilder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete

val arithmeticOperators = listOf("+", "-", "*", "/")
val logicalOperators = listOf("==", "!=", "<", ">", "<=", ">=", "AND", "OR")
val allOperators = arithmeticOperators + logicalOperators

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditFormulaScreen(
    navController: NavController,
    formulaId: String? = null,
    formulaViewModel: FormulaViewModel = viewModel()
) {
    val isEditing = formulaId != null
    val selectedFormula by formulaViewModel.selectedFormula.collectAsState()
    val availableVariables by formulaViewModel.variables.collectAsState()

    var isConditionalMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    val simpleExpressionElements = remember { mutableStateListOf<ExpressionElement>() }
    val conditionalExpressions = remember { mutableStateListOf<ConditionalExpression>() }
    val defaultExpressionElements = remember { mutableStateListOf<ExpressionElement>() }
    var showDefaultExpressionBuilder by remember { mutableStateOf(false) }


    LaunchedEffect(formulaId) {
        if (formulaId != null) {
            formulaViewModel.getFormula(formulaId)
        } else {
            formulaViewModel.clearSelectedFormula()
            // Reset local states for create mode
            name = ""
            isConditionalMode = false
            simpleExpressionElements.clear()
            conditionalExpressions.clear()
            defaultExpressionElements.clear()
            showDefaultExpressionBuilder = false
        }
    }

    LaunchedEffect(selectedFormula, isEditing) {
        if (isEditing && selectedFormula != null) {
            val formula = selectedFormula!!
            name = formula.name
            if (formula.conditions != null && formula.conditions.isNotEmpty()) {
                isConditionalMode = true
                conditionalExpressions.clear()
                conditionalExpressions.addAll(formula.conditions.map {
                    // Ensure mutable lists for elements within each loaded condition
                    ConditionalExpression(
                        condition = Expression(elements = it.condition.elements.toMutableList()),
                        resultExpression = Expression(elements = it.resultExpression.elements.toMutableList())
                    )
                })
                defaultExpressionElements.clear()
                formula.defaultExpression?.elements?.let { defaultExpressionElements.addAll(it) }
                showDefaultExpressionBuilder = formula.defaultExpression != null
                simpleExpressionElements.clear()
            } else {
                isConditionalMode = false
                simpleExpressionElements.clear()
                formula.expression?.elements?.let { simpleExpressionElements.addAll(it) }
                conditionalExpressions.clear()
                defaultExpressionElements.clear()
                showDefaultExpressionBuilder = false
            }
        } else if (!isEditing) { // Ensure reset for create mode if selectedFormula becomes null
            name = ""
            isConditionalMode = false
            simpleExpressionElements.clear()
            conditionalExpressions.clear()
            defaultExpressionElements.clear()
            showDefaultExpressionBuilder = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing && selectedFormula != null) "Edit Formula" else "Create Formula") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Formula Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { isConditionalMode = false },
                    colors = if (!isConditionalMode) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simple")
                    if(!isConditionalMode) Spacer(Modifier.width(4.dp))
                    if(!isConditionalMode) Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { isConditionalMode = true },
                    colors = if (isConditionalMode) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Conditional")
                     if(isConditionalMode) Spacer(Modifier.width(4.dp))
                    if(isConditionalMode) Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(18.dp))
                }
            }

            if (isConditionalMode) {
                // --- Conditional Formula UI ---
                Button(
                    onClick = {
                        conditionalExpressions.add(
                            ConditionalExpression(
                                condition = Expression(emptyList()),
                                resultExpression = Expression(emptyList())
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Condition")
                    Spacer(Modifier.width(4.dp))
                    Text("Add Condition")
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(), // Use weight to make it scrollable within Column
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(conditionalExpressions.toList()) { index, condExpr -> // Use toList for stable list
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                            .padding(8.dp)
                        ) {
                            // Condition Part
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)).padding(8.dp)) {
                                ExpressionBuilder(
                                    elements = condExpr.condition.elements,
                                    availableVariables = availableVariables,
                                    availableOperators = allOperators, // Use all operators for conditions
                                    onElementsChanged = { newElements ->
                                        conditionalExpressions[index] = condExpr.copy(condition = Expression(newElements))
                                    },
                                    expressionLabel = "Condition ${index + 1}"
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            // Result Expression Part
                             Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)).padding(8.dp)) {
                                ExpressionBuilder(
                                    elements = condExpr.resultExpression.elements,
                                    availableVariables = availableVariables,
                                    availableOperators = arithmeticOperators, // Only arithmetic for results
                                    onElementsChanged = { newElements ->
                                        conditionalExpressions[index] = condExpr.copy(resultExpression = Expression(newElements))
                                    },
                                    expressionLabel = "Then Execute ${index + 1}"
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { conditionalExpressions.removeAt(index) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove Condition ${index + 1}")
                                Spacer(Modifier.width(4.dp))
                                Text("Remove Condition ${index + 1}")
                            }
                        }
                    }
                }

                // Default Expression Section
                Spacer(Modifier.height(16.dp))
                Text("Default Expression (Optional):", style = MaterialTheme.typography.titleMedium)
                if (showDefaultExpressionBuilder) {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)).padding(8.dp)) {
                        ExpressionBuilder(
                            elements = defaultExpressionElements,
                            availableVariables = availableVariables,
                            availableOperators = arithmeticOperators,
                            onElementsChanged = { newElements ->
                                defaultExpressionElements.clear()
                                defaultExpressionElements.addAll(newElements)
                            },
                            expressionLabel = "Default Result"
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        defaultExpressionElements.clear()
                        showDefaultExpressionBuilder = false
                    }) {
                        Text("Remove Default Expression")
                    }
                } else {
                    Button(onClick = { showDefaultExpressionBuilder = true }) {
                        Text("Add Default Expression")
                    }
                }

            } else {
                // --- Simple Formula UI ---
                ExpressionBuilder(
                    elements = simpleExpressionElements,
                    availableVariables = availableVariables,
                    availableOperators = arithmeticOperators, // Only arithmetic for simple formula results
                    onElementsChanged = { newElements ->
                        simpleExpressionElements.clear()
                        simpleExpressionElements.addAll(newElements)
                    },
                    expressionLabel = "Expression"
                )
            }

            // --- Save/Cancel Buttons ---
            Row( // This Row should be outside the LazyColumn if it's part of the main screen layout
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (name.isNotBlank()) {
                        if (isEditing && selectedFormula != null) { // Check selectedFormula for ID
                            formulaViewModel.updateFormula(
                                formulaId = selectedFormula!!.id,
                                name = name,
                                expressionElements = if (!isConditionalMode) simpleExpressionElements.toList() else null,
                                conditions = if (isConditionalMode) conditionalExpressions.toList() else null,
                                defaultExpression = if (isConditionalMode && defaultExpressionElements.isNotEmpty()) Expression(defaultExpressionElements.toList()) else null
                            )
                        } else {
                            formulaViewModel.addFormula(
                                name = name,
                                expressionElements = if (!isConditionalMode) simpleExpressionElements.toList() else null,
                                conditions = if (isConditionalMode) conditionalExpressions.toList() else null,
                                defaultExpression = if (isConditionalMode && defaultExpressionElements.isNotEmpty()) Expression(defaultExpressionElements.toList()) else null
                            )
                        }
                        navController.popBackStack()
                    }
                }) { Text("Save") }
            }
        }
    }

    // Clear selected formula from ViewModel when screen is disposed if in edit mode
    DisposableEffect(Unit) {
        onDispose {
            if (isEditing) { // isEditing flag is sufficient here
                formulaViewModel.clearSelectedFormula()
            }
        }
    }
}
