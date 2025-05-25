package com.example.finalpaycalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalpaycalculator.data.model.DataType
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.domain.logic.InMemoryDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VariableViewModel : ViewModel() {

    private val _variables = MutableStateFlow<List<Variable>>(emptyList())
    val variables: StateFlow<List<Variable>> = _variables.asStateFlow()

    private val _selectedVariable = MutableStateFlow<Variable?>(null)
    val selectedVariable: StateFlow<Variable?> = _selectedVariable.asStateFlow()

    init {
        loadVariables()
    }

    private fun loadVariables() {
        viewModelScope.launch {
            _variables.value = InMemoryDataStore.getAllVariables()
        }
    }

    fun addVariable(
        name: String,
        description: String?,
        initialValueString: String,
        type: DataType,
        options: List<String>?
    ) {
        viewModelScope.launch {
            val newVariable = Variable(
                id = UUID.randomUUID().toString(), // ID generated here
                name = name,
                description = description,
                initialValueString = initialValueString,
                type = type,
                options = if (type == DataType.CHOICE) options else null
            )
            InMemoryDataStore.addVariable(newVariable)
            loadVariables() // Refresh the list
        }
    }

    fun updateVariable(variable: Variable) {
        viewModelScope.launch {
            InMemoryDataStore.updateVariable(variable)
            loadVariables() // Refresh the list
            _selectedVariable.value = null // Clear selection after update
        }
    }

    fun deleteVariable(variableId: String) {
        viewModelScope.launch {
            InMemoryDataStore.deleteVariable(variableId)
            loadVariables() // Refresh the list
        }
    }

    fun getVariable(variableId: String): Variable? {
        val variable = InMemoryDataStore.getVariable(variableId)
        _selectedVariable.value = variable
        return variable
    }

    fun clearSelectedVariable() {
        _selectedVariable.value = null
    }
}
