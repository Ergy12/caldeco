package com.example.finalpaycalculator.domain.logic

import android.content.Context
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.data.model.InputField
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.datastore.UserPreferencesRepository
import com.example.finalpaycalculator.datastore.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

object InMemoryDataStore {
    private val variables = mutableListOf<Variable>()
    private val formulas = mutableListOf<Formula>()
    private val inputFields = mutableListOf<InputField>()

    private var repository: UserPreferencesRepository? = null
    private val scope = CoroutineScope(Dispatchers.IO) // Use IO dispatcher for file operations

    // Call this from Application.onCreate()
    fun initialize(context: Context) {
        if (repository == null) {
            repository = UserPreferencesRepository(context)
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        scope.launch {
            val settings = repository?.getInitialSettings() ?: return@launch
            variables.clear()
            variables.addAll(settings.variablesList.map { it.toDomain() })
            formulas.clear()
            formulas.addAll(settings.formulasList.map { it.toDomain() })
            inputFields.clear()
            inputFields.addAll(settings.inputFieldsList.map { it.toDomain() })
        }
    }

    // Variable CRUD
    fun addVariable(variable: Variable) {
        val newVariable = variable.copy(id = if(variable.id.isBlank()) UUID.randomUUID().toString() else variable.id)
        variables.add(newVariable)
        scope.launch { repository?.saveVariables(variables.toList()) }
    }
    fun getVariable(id: String): Variable? = variables.find { it.id == id }
    fun getAllVariables(): List<Variable> = variables.toList()
    fun updateVariable(updatedVariable: Variable) {
        val index = variables.indexOfFirst { it.id == updatedVariable.id }
        if (index != -1) {
            variables[index] = updatedVariable
            scope.launch { repository?.saveVariables(variables.toList()) }
        }
    }
    fun deleteVariable(id: String) {
        variables.removeAll { it.id == id }
        scope.launch { repository?.saveVariables(variables.toList()) }
    }

    // Formula CRUD
    fun addFormula(formula: Formula) {
        val newFormula = formula.copy(id = if(formula.id.isBlank()) UUID.randomUUID().toString() else formula.id)
        formulas.add(newFormula)
        scope.launch { repository?.saveFormulas(formulas.toList()) }
    }
    fun getFormula(id: String): Formula? = formulas.find { it.id == id }
    fun getAllFormulas(): List<Formula> = formulas.toList()
    fun updateFormula(updatedFormula: Formula) {
        val index = formulas.indexOfFirst { it.id == updatedFormula.id }
        if (index != -1) {
            formulas[index] = updatedFormula
            scope.launch { repository?.saveFormulas(formulas.toList()) }
        }
    }
    fun deleteFormula(id: String) {
        formulas.removeAll { it.id == id }
        scope.launch { repository?.saveFormulas(formulas.toList()) }
    }

    // InputField CRUD
    fun addInputField(inputField: InputField) {
        val newInputField = inputField.copy(id = if(inputField.id.isBlank()) UUID.randomUUID().toString() else inputField.id)
        inputFields.add(newInputField)
        scope.launch { repository?.saveInputFields(inputFields.toList()) }
    }
    fun getInputField(id: String): InputField? = inputFields.find { it.id == id }
    fun getAllInputFields(): List<InputField> = inputFields.toList()
    fun updateInputField(updatedInputField: InputField) {
        val index = inputFields.indexOfFirst { it.id == updatedInputField.id }
        if (index != -1) {
            inputFields[index] = updatedInputField
            scope.launch { repository?.saveInputFields(inputFields.toList()) }
        }
    }
    fun deleteInputField(id: String) {
        inputFields.removeAll { it.id == id }
        scope.launch { repository?.saveInputFields(inputFields.toList()) }
    }
}
