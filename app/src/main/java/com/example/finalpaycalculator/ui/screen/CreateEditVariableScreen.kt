package com.example.finalpaycalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.finalpaycalculator.data.model.DataType
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.viewmodel.VariableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditVariableScreen(
    navController: NavController,
    variableId: String? = null,
    variableViewModel: VariableViewModel = viewModel()
) {
    val isEditing = variableId != null
    val existingVariable = if (isEditing) variableViewModel.getVariable(variableId!!) else null

    var name by remember { mutableStateOf(existingVariable?.name ?: "") }
    var description by remember { mutableStateOf(existingVariable?.description ?: "") }
    var selectedDataType by remember { mutableStateOf(existingVariable?.type ?: DataType.NUMBER) }
    var initialValueString by remember { mutableStateOf(existingVariable?.initialValueString ?: "") }
    var options by remember { mutableStateOf(existingVariable?.options?.toMutableList() ?: mutableListOf<String>()) }

    LaunchedEffect(isEditing, existingVariable) {
        if (isEditing && existingVariable != null) {
            name = existingVariable.name
            description = existingVariable.description ?: ""
            selectedDataType = existingVariable.type
            initialValueString = existingVariable.initialValueString
            options = existingVariable.options?.toMutableList() ?: mutableListOf()
        } else {
            // Reset for "Create" mode or if variable not found
            name = ""
            description = ""
            selectedDataType = DataType.NUMBER
            initialValueString = ""
            options = mutableListOf()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Variable" else "Create Variable") },
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
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = false, // Manage expanded state
                onExpandedChange = { /* Manage expanded state */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDataType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = false, // Manage expanded state
                    onDismissRequest = { /* Manage expanded state */ }
                ) {
                    DataType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                selectedDataType = type
                                // Reset options if type is not CHOICE
                                if (type != DataType.CHOICE) {
                                    options = mutableListOf()
                                }
                                // Reset initial value if type changes
                                initialValueString = ""
                            }
                        )
                    }
                }
            }


            if (selectedDataType == DataType.CHOICE) {
                Text("Options:", style = MaterialTheme.typography.titleMedium)
                options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { options[index] = it },
                            label = { Text("Option ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { options.removeAt(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove Option")
                        }
                    }
                }
                Button(onClick = { options.add("") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Option")
                    Spacer(Modifier.width(4.dp))
                    Text("Add Option")
                }
            }

            OutlinedTextField(
                value = initialValueString,
                onValueChange = { initialValueString = it },
                label = { Text("Initial Value") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (selectedDataType) {
                        DataType.NUMBER -> KeyboardType.Number
                        else -> KeyboardType.Text
                    }
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (name.isNotBlank()) { // Basic validation
                        if (isEditing && existingVariable != null) {
                            val updatedVariable = existingVariable.copy(
                                name = name,
                                description = description.ifBlank { null },
                                initialValueString = initialValueString,
                                type = selectedDataType,
                                options = if (selectedDataType == DataType.CHOICE) options.toList() else null
                            )
                            variableViewModel.updateVariable(updatedVariable)
                        } else {
                            variableViewModel.addVariable(
                                name = name,
                                description = description.ifBlank { null },
                                initialValueString = initialValueString,
                                type = selectedDataType,
                                options = if (selectedDataType == DataType.CHOICE) options.toList() else null
                            )
                        }
                        navController.popBackStack()
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
    // Clean up selected variable when the screen is left
    DisposableEffect(Unit) {
        onDispose {
            if (isEditing) {
                variableViewModel.clearSelectedVariable()
            }
        }
    }
}
