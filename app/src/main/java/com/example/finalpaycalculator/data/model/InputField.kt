package com.example.finalpaycalculator.data.model

data class InputField(
    val id: String,
    val label: String,
    val description: String? = null,
    val linkedVariableId: String
)
