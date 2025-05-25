package com.example.finalpaycalculator.data.model

data class Variable(
    val id: String,
    val name: String,
    val description: String? = null,
    val initialValueString: String,
    val type: DataType,
    val options: List<String>? = null // Applicable if type is CHOICE
)
