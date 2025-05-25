package com.example.finalpaycalculator.domain.logic

import com.example.finalpaycalculator.data.model.ConditionalExpression
import com.example.finalpaycalculator.data.model.DataType
import com.example.finalpaycalculator.data.model.Expression
import com.example.finalpaycalculator.data.model.ExpressionElement
import com.example.finalpaycalculator.data.model.Formula
import com.example.finalpaycalculator.data.model.LiteralElement
import com.example.finalpaycalculator.data.model.OperatorElement
import com.example.finalpaycalculator.data.model.Variable
import com.example.finalpaycalculator.data.model.VariableElement
import java.util.Stack

object FormulaEvaluationEngine {

    fun evaluate(
        formula: Formula,
        variableValues: Map<String, String>,
        allVariables: List<Variable>
    ): String {
        return try {
            val result = when {
                formula.conditions != null && formula.conditions.isNotEmpty() ->
                    evaluateConditionalFormula(formula, variableValues, allVariables)
                formula.expression != null ->
                    evaluateSimpleFormula(formula.expression, variableValues, allVariables)
                else -> "Invalid formula: No expression or conditions."
            }
            result?.toString() ?: "Error: Null result"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun evaluateSimpleFormula(
        expression: Expression,
        variableValues: Map<String, String>,
        allVariables: List<Variable>
    ): Any? {
        // Placeholder for Shunting-yard and RPN evaluation
        // For now, let's try a very basic direct evaluation for simple cases if possible,
        // or just return a string indicating it needs proper parsing.
        // This will be fully implemented in subsequent steps.
        val postfixExpression = infixToPostfix(expression.elements, variableValues, allVariables)
        return evaluatePostfix(postfixExpression, variableValues, allVariables)
    }

    private fun evaluateConditionalFormula(
        formula: Formula,
        variableValues: Map<String, String>,
        allVariables: List<Variable>
    ): Any? {
        formula.conditions?.forEach { conditionalExpression ->
            val conditionResult = evaluateExpression(conditionalExpression.condition, variableValues, allVariables)
            if (conditionResult is Boolean && conditionResult) {
                return evaluateExpression(conditionalExpression.resultExpression, variableValues, allVariables)
            }
        }
        if (formula.defaultExpression != null) {
            return evaluateExpression(formula.defaultExpression, variableValues, allVariables)
        }
        return "No condition met and no default value."
    }
    
    // Renamed from evaluateSimpleFormula to avoid confusion, this is the core expression evaluator
    internal fun evaluateExpression(
        expression: Expression,
        variableValues: Map<String, String>,
        allVariables: List<Variable>
    ): Any? {
        val postfixExpression = infixToPostfix(expression.elements, variableValues, allVariables)
        return evaluatePostfix(postfixExpression, variableValues, allVariables)
    }


    internal fun parseValue(value: String, dataType: DataType): Any? {
        return when (dataType) {
            DataType.NUMBER -> value.toDoubleOrNull()
            DataType.TEXT -> value
            DataType.BOOLEAN -> value.toBooleanStrictOrNull()
            DataType.CHOICE -> value // Choices are treated as text for now in expressions
        }
    }

    internal fun getValueFromElement(
        element: ExpressionElement,
        variableValues: Map<String, String>,
        allVariables: List<Variable>
    ): Any? {
        return when (element) {
            is LiteralElement -> {
                // Attempt to parse as Double, if not, keep as String.
                // This is a simplification. Proper type handling based on context is better.
                element.value.toDoubleOrNull() ?: element.value
            }
            is VariableElement -> {
                val variable = allVariables.find { it.id == element.variableId }
                    ?: throw IllegalArgumentException("Undefined variable: ${element.variableId}")
                val stringValue = variableValues[element.variableId]
                    ?: throw IllegalArgumentException("No value for variable: ${variable.name}")
                parseValue(stringValue, variable.type)
                    ?: throw IllegalArgumentException("Cannot parse value '$stringValue' for variable ${variable.name} as ${variable.type}")
            }
            is OperatorElement -> element.operator // Should not be called for operators directly
        }
    }

    // --- Shunting-yard Algorithm (Infix to Postfix) ---
    private fun getPrecedence(operator: String): Int {
        return when (operator) {
            "OR" -> 1
            "AND" -> 2
            "==", "!=", "<", ">", "<=", ">=" -> 3
            "+", "-" -> 4
            "*", "/" -> 5
            else -> 0 // For parentheses or unknown
        }
    }

    internal fun infixToPostfix(elements: List<ExpressionElement>, variableValues: Map<String, String>, allVariables: List<Variable>): List<ExpressionElement> {
        val outputQueue = mutableListOf<ExpressionElement>()
        val operatorStack = Stack<OperatorElement>()

        for (element in elements) {
            when (element) {
                is LiteralElement, is VariableElement -> {
                    outputQueue.add(element)
                }
                is OperatorElement -> {
                    while (operatorStack.isNotEmpty() &&
                           getPrecedence(operatorStack.peek().operator) >= getPrecedence(element.operator)) {
                        outputQueue.add(operatorStack.pop())
                    }
                    operatorStack.push(element)
                }
            }
        }
        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.pop())
        }
        return outputQueue
    }

    // --- RPN (Postfix) Evaluation ---
    internal fun evaluatePostfix(postfixElements: List<ExpressionElement>, variableValues: Map<String, String>, allVariables: List<Variable>): Any? {
        val stack = Stack<Any>()

        for (element in postfixElements) {
            when (element) {
                is LiteralElement -> {
                    // Try to parse as Double, then Boolean, else String. This order is important.
                    val value = element.value
                    val parsedValue = value.toDoubleOrNull() ?: value.toBooleanStrictOrNull() ?: value
                    stack.push(parsedValue)
                }
                is VariableElement -> {
                    val variable = allVariables.find { it.id == element.variableId }
                        ?: throw IllegalArgumentException("Undefined variable: ${element.variableId}")
                    val stringValue = variableValues[element.variableId]
                        ?: throw IllegalArgumentException("No value for variable: ${variable.name}")
                    val parsedValue = parseValue(stringValue, variable.type)
                        ?: throw IllegalArgumentException("Cannot parse value '$stringValue' for variable ${variable.name} as ${variable.type}")
                    stack.push(parsedValue)
                }
                is OperatorElement -> {
                    if (stack.size < 2 && !(element.operator == "NOT")) { // "NOT" is unary, others binary
                         throw IllegalArgumentException("Invalid expression: Not enough operands for operator ${element.operator}")
                    }
                    // Note: Order of pop might matter for non-commutative ops like subtraction, division
                    val rightOperand = stack.pop()
                    val leftOperand = if (element.operator == "NOT" && stack.isNotEmpty()) Unit else stack.pop() // NOT is unary

                    val result = performOperation(leftOperand, rightOperand, element.operator)
                    stack.push(result)
                }
            }
        }
        return if (stack.isNotEmpty()) stack.pop() else throw IllegalArgumentException("Invalid expression: Empty stack at the end")
    }
    
    private fun performOperation(left: Any?, right: Any, operator: String): Any {
        // Handle NOT separately as it's unary
        if (operator == "NOT") {
            if (right !is Boolean) throw IllegalArgumentException("NOT operator requires a Boolean operand, got $right")
            return !right
        }

        if (left == null) throw IllegalArgumentException("Left operand missing for operator $operator")


        return when (operator) {
            "+" -> {
                if (left is Double && right is Double) left + right
                else if (left is String && right is String) left + right // String concatenation
                else throw IllegalArgumentException("'+' operator supports Double or String types, got ${left::class.simpleName} and ${right::class.simpleName}")
            }
            "-" -> {
                if (left is Double && right is Double) left - right
                else throw IllegalArgumentException("'-' operator supports Double types, got ${left::class.simpleName} and ${right::class.simpleName}")
            }
            "*" -> {
                if (left is Double && right is Double) left * right
                else throw IllegalArgumentException("'*' operator supports Double types, got ${left::class.simpleName} and ${right::class.simpleName}")
            }
            "/" -> {
                if (left is Double && right is Double) {
                    if (right == 0.0) throw ArithmeticException("Division by zero")
                    left / right
                } else throw IllegalArgumentException("'/' operator supports Double types, got ${left::class.simpleName} and ${right::class.simpleName}")
            }
            "==" -> compareValues(left, right) { a, b -> a == b }
            "!=" -> compareValues(left, right) { a, b -> a != b }
            "<" -> compareValues(left, right, requireNumeric = true) { a, b -> (a as Double) < (b as Double) }
            ">" -> compareValues(left, right, requireNumeric = true) { a, b -> (a as Double) > (b as Double) }
            "<=" -> compareValues(left, right, requireNumeric = true) { a, b -> (a as Double) <= (b as Double) }
            ">=" -> compareValues(left, right, requireNumeric = true) { a, b -> (a as Double) >= (b as Double) }
            "AND" -> {
                if (left is Boolean && right is Boolean) left && right
                else throw IllegalArgumentException("'AND' operator requires Boolean operands, got ${left::class.simpleName} and ${right::class.simpleName}")
            }
            "OR" -> {
                if (left is Boolean && right is Boolean) left || right
                else throw IllegalArgumentException("'OR' operator requires Boolean operands, got ${left::class.simpleName} and ${right::class.simpleName}")
            }
            else -> throw IllegalArgumentException("Unknown operator: $operator")
        }
    }

    private fun compareValues(left: Any, right: Any, requireNumeric: Boolean = false, comparison: (Any, Any) -> Boolean): Boolean {
        if (left is Double && right is Double) return comparison(left, right)
        if (!requireNumeric && left is String && right is String) return comparison(left, right)
        if (!requireNumeric && left is Boolean && right is Boolean) return comparison(left, right)
        
        // Type promotion for comparison: if one is Double and other is String representation of Double
        if (left is Double && right is String) {
            right.toDoubleOrNull()?.let { return comparison(left, it) }
        }
        if (left is String && right is Double) {
            left.toDoubleOrNull()?.let { return comparison(it, right) }
        }

        throw IllegalArgumentException("Comparison error: Incompatible types ${left::class.simpleName} and ${right::class.simpleName}")
    }
}
