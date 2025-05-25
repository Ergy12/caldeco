package com.example.finalpaycalculator.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.core.DataStoreFactory
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.data.model.InputField
import com.example.finalpaycalculator.data.model.Variable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

class UserPreferencesRepository(private val context: Context) {

    private val dataStore: DataStore<AppSettings> = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { context.dataStoreFile("app_settings.pb") }
    )

    val appSettingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(AppSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun saveVariables(variables: List<Variable>) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearVariables() // Clear existing variables
                .addAllVariables(variables.map { it.toProto() })
                .build()
        }
    }

    suspend fun saveFormulas(formulas: List<Formula>) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearFormulas() // Clear existing formulas
                .addAllFormulas(formulas.map { it.toProto() })
                .build()
        }
    }

    suspend fun saveInputFields(inputFields: List<InputField>) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearInputFields() // Clear existing input fields
                .addAllInputFields(inputFields.map { it.toProto() })
                .build()
        }
    }

    // Example of how to save all settings at once if needed
    suspend fun saveAllSettings(
        variables: List<Variable>,
        formulas: List<Formula>,
        inputFields: List<InputField>
    ) {
        dataStore.updateData {
            AppSettings.newBuilder()
                .addAllVariables(variables.map { it.toProto() })
                .addAllFormulas(formulas.map { it.toProto() })
                .addAllInputFields(inputFields.map { it.toProto() })
                .build()
        }
    }
    
    // Function to get the current settings once, useful for initial load
    suspend fun getInitialSettings(): AppSettings {
        return dataStore.data.first()
    }
}
