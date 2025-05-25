package com.example.finalpaycalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalpaycalculator.data.model.InputField
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.domain.logic.InMemoryDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class InputFieldViewModel : ViewModel() {

    private val _inputFields = MutableStateFlow<List<InputField>>(emptyList())
    val inputFields: StateFlow<List<InputField>> = _inputFields.asStateFlow()

    private val _variables = MutableStateFlow<List<Variable>>(emptyList())
    val variables: StateFlow<List<Variable>> = _variables.asStateFlow()

    private val _selectedInputField = MutableStateFlow<InputField?>(null)
    val selectedInputField: StateFlow<InputField?> = _selectedInputField.asStateFlow()

    init {
        loadInputFields()
        loadVariables()
    }

    private fun loadInputFields() {
        viewModelScope.launch {
            _inputFields.value = InMemoryDataStore.getAllInputFields()
        }
    }

    private fun loadVariables() {
        viewModelScope.launch {
            _variables.value = InMemoryDataStore.getAllVariables()
        }
    }

    fun addInputField(label: String, description: String?, linkedVariableId: String) {
        viewModelScope.launch {
            val newInputField = InputField(
                id = UUID.randomUUID().toString(),
                label = label,
                description = description,
                linkedVariableId = linkedVariableId
            )
            InMemoryDataStore.addInputField(newInputField)
            loadInputFields()
        }
    }

    fun updateInputField(inputField: InputField) {
        viewModelScope.launch {
            InMemoryDataStore.updateInputField(inputField)
            loadInputFields()
            _selectedInputField.value = null // Clear selection
        }
    }

    fun deleteInputField(inputFieldId: String) {
        viewModelScope.launch {
            InMemoryDataStore.deleteInputField(inputFieldId)
            loadInputFields()
        }
    }

    fun getInputField(inputFieldId: String): InputField? {
        val field = InMemoryDataStore.getInputField(inputFieldId)
        _selectedInputField.value = field
        return field
    }

    fun clearSelectedInputField() {
        _selectedInputField.value = null
    }
}
