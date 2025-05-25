package com.example.finalpaycalculator.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.finalpaycalculator.data.model.ExpressionElement
import com.example.finalpaycalculator.data.model.LiteralElement
import com.example.finalpaycalculator.data.model.OperatorElement
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.data.model.VariableElement
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpressionBuilder(
    elements: List<ExpressionElement>,
    availableVariables: List<Variable>,
    availableOperators: List<String>,
    onElementsChanged: (List<ExpressionElement>) -> Unit,
    expressionLabel: String = "Expression"
) {
    var literalInput by remember { mutableStateOf("") }

    val expressionString = remember(elements) {
        elements.joinToString(separator = " ") {
            when (it) {
                is VariableElement -> availableVariables.find { v -> v.id == it.variableId }?.name ?: "Var(${it.variableId.take(4)})"
                is OperatorElement -> it.operator
                is LiteralElement -> it.value
                else -> ""
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(expressionLabel, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = expressionString,
            onValueChange = { /* Read-only */ },
            readOnly = true,
            label = { Text("Current $expressionLabel") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = literalInput,
                onValueChange = { literalInput = it },
                label = { Text("Number/Text") }, // Allow text for logical comparisons too
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (literalInput.isNotBlank()) {
                    onElementsChanged(elements + LiteralElement(literalInput))
                    literalInput = ""
                }
            }) { Text("Add Literal") }
        }
        Button(
            onClick = { if (elements.isNotEmpty()) onElementsChanged(elements.dropLast(1)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Backspace")
        }

        if (availableVariables.isNotEmpty()) {
            Text("Variables:", style = MaterialTheme.typography.titleSmall)
            FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp, modifier = Modifier.fillMaxWidth()) {
                availableVariables.forEach { variable ->
                    Button(onClick = { onElementsChanged(elements + VariableElement(variable.id)) }) {
                        Text(variable.name)
                    }
                }
            }
        }

        if (availableOperators.isNotEmpty()) {
            Text("Operators:", style = MaterialTheme.typography.titleSmall)
            FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp, modifier = Modifier.fillMaxWidth()) {
                availableOperators.forEach { operator ->
                    Button(onClick = { onElementsChanged(elements + OperatorElement(operator)) }) {
                        Text(operator)
                    }
                }
            }
        }
    }
}
