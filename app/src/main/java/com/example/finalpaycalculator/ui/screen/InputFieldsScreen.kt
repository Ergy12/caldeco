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
import com.example.finalpaycalculator.data.model.InputField
import com.example.finalpaycalculator.viewmodel.InputFieldViewModel
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
fun InputFieldsScreen(
    navController: NavController,
    inputFieldViewModel: InputFieldViewModel = viewModel()
) {
    val inputFields by inputFieldViewModel.inputFields.collectAsState()
    val variables by inputFieldViewModel.variables.collectAsState() // Needed to display variable name

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(NavRoutes.CREATE_EDIT_INPUT_FIELD) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Input Field")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(inputFields, key = { it.id }) { inputField ->
                val dismissState = rememberDismissState(
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                            inputFieldViewModel.deleteInputField(inputField.id)
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
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Icon",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    dismissContent = {
                        val linkedVar = variables.find { it.id == inputField.linkedVariableId }
                        InputFieldListItem(
                            inputField = inputField,
                            linkedVariableName = linkedVar?.name ?: "Unknown Variable"
                        ) {
                            navController.navigate("${NavRoutes.CREATE_EDIT_INPUT_FIELD}/${inputField.id}")
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
fun InputFieldListItem(inputField: InputField, linkedVariableName: String, onClick: () -> Unit) {
    ListItem(
        headlineText = { Text(inputField.label) },
        supportingText = { Text("Links to: $linkedVariableName") },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
