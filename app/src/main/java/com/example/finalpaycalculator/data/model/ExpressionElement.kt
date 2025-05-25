package com.example.finalpaycalculator.data.model

sealed interface ExpressionElement

data class VariableElement(val variableId: String) : ExpressionElement

data class OperatorElement(val operator: String) : ExpressionElement // e.g., "+", "-", "==", "AND"

data class LiteralElement(val value: String) : ExpressionElement // Store as String, parse as needed
