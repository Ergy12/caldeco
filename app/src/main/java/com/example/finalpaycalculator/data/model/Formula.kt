package com.example.finalpaycalculator.data.model

data class Formula(
    val id: String,
    val name: String,
    val expression: Expression? = null,
    val conditions: List<ConditionalExpression>? = null,
    val defaultExpression: Expression? = null
)
