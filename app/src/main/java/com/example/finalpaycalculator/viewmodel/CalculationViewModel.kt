package com.example.finalpaycalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.data.model.InputField
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.domain.logic.FormulaEvaluationEngine // Import the engine
import com.example.finalpaycalculator.domain.logic.InMemoryDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class InputFieldWithVariable(
    val inputField: InputField,
    val variable: Variable
)

class CalculationViewModel : ViewModel() {

    private val _inputFields = MutableStateFlow<List<InputField>>(emptyList())
    private val _variables = MutableStateFlow<Map<String, Variable>>(emptyMap()) // Map for easy lookup

    // Exposed StateFlow for UI: List of Pairs of InputField and its linked Variable
    private val _inputFieldsWithVariables = MutableStateFlow<List<InputFieldWithVariable>>(emptyList())
    val inputFieldsWithVariables: StateFlow<List<InputFieldWithVariable>> = _inputFieldsWithVariables.asStateFlow()

    // Stores current input values from the UI, key is variableId
    private val _inputValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val inputValues: StateFlow<Map<String, String>> = _inputValues.asStateFlow()

    // Stores formula results for display
    private val _formulaResults = MutableStateFlow<List<Pair<Formula, String>>>(emptyList())
    val formulaResults: StateFlow<List<Pair<Formula, String>>> = _formulaResults.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load all input fields
            val allInputFields = InMemoryDataStore.getAllInputFields()
            _inputFields.value = allInputFields

            // Load all variables and store them in a map for efficient lookup
            val allVariablesList = InMemoryDataStore.getAllVariables()
            _variables.value = allVariablesList.associateBy { it.id }

            // Combine InputFields with their Variables
            val pairedList = allInputFields.mapNotNull { inputField ->
                _variables.value[inputField.linkedVariableId]?.let { variable ->
                    InputFieldWithVariable(inputField, variable)
                }
            }
            _inputFieldsWithVariables.value = pairedList

            // Initialize inputValues based on variables' initial values
            val initialValues = pairedList.associate { pair ->
                pair.variable.id to pair.variable.initialValueString
            }
            _inputValues.value = initialValues
        }
    }


    fun updateInputValue(variableId: String, value: String) {
        _inputValues.value = _inputValues.value.toMutableMap().apply {
            this[variableId] = value
        }
    }

    fun calculateResults() {
        viewModelScope.launch {
            val formulas = InMemoryDataStore.getAllFormulas()
            val currentInputs = _inputValues.value
            val allVarsList = _variables.value.values.toList() // Get the list of all variables

            val results = formulas.map { formula ->
                val resultString = FormulaEvaluationEngine.evaluate(
                    formula = formula,
                    variableValues = currentInputs,
                    allVariables = allVarsList
                )
                Pair(formula, resultString)
            }
            _formulaResults.value = results
        }
    }
}
