package com.example.finalpaycalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.finalpaycalculator.data.model.InputField
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.viewmodel.InputFieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditInputFieldScreen(
    navController: NavController,
    inputFieldId: String? = null,
    inputFieldViewModel: InputFieldViewModel = viewModel()
) {
    val isEditing = inputFieldId != null
    val existingInputField = if (isEditing) inputFieldViewModel.getInputField(inputFieldId!!) else null
    val availableVariables by inputFieldViewModel.variables.collectAsState()

    var label by remember { mutableStateOf(existingInputField?.label ?: "") }
    var description by remember { mutableStateOf(existingInputField?.description ?: "") }
    var selectedVariableId by remember { mutableStateOf(existingInputField?.linkedVariableId ?: "") }
    var variableDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isEditing, existingInputField) {
        if (isEditing && existingInputField != null) {
            label = existingInputField.label
            description = existingInputField.description ?: ""
            selectedVariableId = existingInputField.linkedVariableId
        } else {
            // Reset for "Create" mode
            label = ""
            description = ""
            selectedVariableId = availableVariables.firstOrNull()?.id ?: "" // Default to first if available
        }
    }
     // Ensure selectedVariableId is valid if availableVariables loads/changes
    LaunchedEffect(availableVariables) {
        if (availableVariables.isNotEmpty() && (selectedVariableId.isBlank() || availableVariables.none { it.id == selectedVariableId })) {
            if (!isEditing) { // Only auto-select for new fields or if previous selection is invalid
                 selectedVariableId = availableVariables.first().id
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Input Field" else "Create Input Field") },
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
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                isError = label.isBlank() && isEditing // Show error if blank during edit, or after first save attempt
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = variableDropdownExpanded,
                onExpandedChange = { variableDropdownExpanded = !variableDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedVarName = availableVariables.find { it.id == selectedVariableId }?.name ?: "Select Variable"
                OutlinedTextField(
                    value = selectedVarName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Link to Variable") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = variableDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                     isError = selectedVariableId.isBlank()
                )
                ExposedDropdownMenu(
                    expanded = variableDropdownExpanded,
                    onDismissRequest = { variableDropdownExpanded = false }
                ) {
                    availableVariables.forEach { variable ->
                        DropdownMenuItem(
                            text = { Text(variable.name) },
                            onClick = {
                                selectedVariableId = variable.id
                                variableDropdownExpanded = false
                            }
                        )
                    }
                     if (availableVariables.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No variables available. Create one first.") },
                            onClick = { variableDropdownExpanded = false },
                            enabled = false
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (label.isNotBlank() && selectedVariableId.isNotBlank()) {
                            if (isEditing && existingInputField != null) {
                                val updatedField = existingInputField.copy(
                                    label = label,
                                    description = description.ifBlank { null },
                                    linkedVariableId = selectedVariableId
                                )
                                inputFieldViewModel.updateInputField(updatedField)
                            } else {
                                inputFieldViewModel.addInputField(
                                    label = label,
                                    description = description.ifBlank { null },
                                    linkedVariableId = selectedVariableId
                                )
                            }
                            navController.popBackStack()
                        }
                        // TODO: Add user feedback for validation errors
                    },
                    enabled = label.isNotBlank() && selectedVariableId.isNotBlank() && availableVariables.isNotEmpty()
                ) {
                    Text("Save")
                }
            }
        }
    }
    // Clean up selected input field when the screen is left
    DisposableEffect(Unit) {
        onDispose {
            if (isEditing) {
                inputFieldViewModel.clearSelectedInputField()
            }
        }
    }
}
