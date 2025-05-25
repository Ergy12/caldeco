package com.example.finalpaycalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalpaycalculator.data.model.ExpressionElement
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.data.model.ConditionalExpression
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.domain.logic.InMemoryDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.finalpaycalculator.data.model.Expression

class FormulaViewModel : ViewModel() {

    private val _formulas = MutableStateFlow<List<Formula>>(emptyList())
    val formulas: StateFlow<List<Formula>> = _formulas.asStateFlow()

    private val _variables = MutableStateFlow<List<Variable>>(emptyList())
    val variables: StateFlow<List<Variable>> = _variables.asStateFlow()

    private val _selectedFormula = MutableStateFlow<Formula?>(null)
    val selectedFormula: StateFlow<Formula?> = _selectedFormula.asStateFlow()

    init {
        loadFormulas()
        loadVariables()
    }

    private fun loadFormulas() {
        viewModelScope.launch {
            _formulas.value = InMemoryDataStore.getAllFormulas()
        }
    }

    private fun loadVariables() {
        viewModelScope.launch {
            _variables.value = InMemoryDataStore.getAllVariables()
        }
    }

    fun addFormula(
        name: String,
        expressionElements: List<ExpressionElement>?, // Nullable for conditional
        conditions: List<ConditionalExpression>?,
        defaultExpression: Expression?
    ) {
        viewModelScope.launch {
            val newFormula = Formula(
                id = UUID.randomUUID().toString(),
                name = name,
                expression = expressionElements?.let { Expression(elements = it) },
                conditions = conditions,
                defaultExpression = defaultExpression
            )
            InMemoryDataStore.addFormula(newFormula)
            loadFormulas()
        }
    }

    fun updateFormula(
        formulaId: String, // Need id to find the existing formula
        name: String,
        expressionElements: List<ExpressionElement>?,
        conditions: List<ConditionalExpression>?,
        defaultExpression: Expression?
    ) {
        viewModelScope.launch {
            val existingFormula = InMemoryDataStore.getFormula(formulaId) ?: return@launch // Or handle error
            val updatedFormula = existingFormula.copy(
                name = name,
                expression = expressionElements?.let { Expression(elements = it) },
                conditions = conditions,
                defaultExpression = defaultExpression
            )
            InMemoryDataStore.updateFormula(updatedFormula)
            loadFormulas()
            _selectedFormula.value = null // Clear selection
        }
    }

    fun deleteFormula(formulaId: String) {
        viewModelScope.launch {
            InMemoryDataStore.deleteFormula(formulaId)
            loadFormulas()
        }
    }

    fun getFormula(formulaId: String): Formula? {
        val formula = InMemoryDataStore.getFormula(formulaId)
        _selectedFormula.value = formula
        return formula
    }

    fun clearSelectedFormula() {
        _selectedFormula.value = null
    }
}
