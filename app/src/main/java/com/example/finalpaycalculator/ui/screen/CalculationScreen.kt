package com.example.finalpaycalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalpaycalculator.data.model.DataType
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.viewmodel.CalculationViewModel
import com.example.finalpaycalculator.viewmodel.InputFieldWithVariable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(
    calculationViewModel: CalculationViewModel = viewModel()
) {
    val inputFieldsWithVariables by calculationViewModel.inputFieldsWithVariables.collectAsState()
    val inputValues by calculationViewModel.inputValues.collectAsState()
    val formulaResults by calculationViewModel.formulaResults.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // --- Input Fields Section ---
        Text("Inputs", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(inputFieldsWithVariables, key = { it.inputField.id }) { fieldWithValue ->
                val variable = fieldWithValue.variable
                val inputField = fieldWithValue.inputField
                val currentValue = inputValues[variable.id] ?: variable.initialValueString

                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(inputField.label, style = MaterialTheme.typography.titleMedium)
                    if (inputField.description != null) {
                        Text(inputField.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                    }

                    when (variable.type) {
                        DataType.NUMBER -> {
                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { calculationViewModel.updateInputValue(variable.id, it) },
                                label = { Text("Enter Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        DataType.TEXT -> {
                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { calculationViewModel.updateInputValue(variable.id, it) },
                                label = { Text("Enter Text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        DataType.BOOLEAN -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = currentValue.toBooleanStrictOrNull() ?: false,
                                    onCheckedChange = { calculationViewModel.updateInputValue(variable.id, it.toString()) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (currentValue.toBooleanStrictOrNull() == true) "Yes" else "No")
                            }
                        }
                        DataType.CHOICE -> {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Option") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    variable.options?.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                calculationViewModel.updateInputValue(variable.id, option)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
             if (inputFieldsWithVariables.isEmpty()) {
                item {
                    Text("No input fields configured yet. Go to the 'Inputs' tab to add some.",
                         modifier = Modifier.padding(vertical = 16.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Calculate Button ---
        Button(
            onClick = { calculationViewModel.calculateResults() },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputFieldsWithVariables.isNotEmpty() // Disable if no inputs
        ) {
            Text("Calculate")
        }

        Spacer(Modifier.height(16.dp))

        // --- Results Section ---
        Text("Results", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(formulaResults, key = { it.first.id }) { (formula, result) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(formula.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(result, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (formulaResults.isEmpty()) {
                item {
                     Text("Click 'Calculate' to see results.",
                          modifier = Modifier.padding(vertical = 16.dp))
                }
            }
        }
    }
}
