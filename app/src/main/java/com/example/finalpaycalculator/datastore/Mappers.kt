package com.example.finalpaycalculator.datastore

import com.example.finalpaycalculator.data.model.*

// --- To Proto Mappers ---

fun DataType.toProto(): ProtoDataType {
    return when (this) {
        DataType.NUMBER -> ProtoDataType.NUMBER
        DataType.TEXT -> ProtoDataType.TEXT
        DataType.BOOLEAN -> ProtoDataType.BOOLEAN
        DataType.CHOICE -> ProtoDataType.CHOICE
        // Add a default or throw exception if unspecified is not desired
    }
}

fun Variable.toProto(): ProtoVariable {
    val builder = ProtoVariable.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setInitialValueString(this.initialValueString)
        .setType(this.type.toProto())
    this.description?.let { builder.description = it }
    this.options?.let { builder.addAllOptions(it) }
    return builder.build()
}

fun ExpressionElement.toProto(): ProtoExpressionElement {
    val builder = ProtoExpressionElement.newBuilder()
    when (this) {
        is VariableElement -> builder.variableElement = ProtoVariableElement.newBuilder().setVariableId(this.variableId).build()
        is OperatorElement -> builder.operatorElement = ProtoOperatorElement.newBuilder().setOperatorString(this.operator).build()
        is LiteralElement -> builder.literalElement = ProtoLiteralElement.newBuilder().setValueString(this.value).build()
    }
    return builder.build()
}

fun Expression.toProto(): ProtoExpression {
    return ProtoExpression.newBuilder()
        .addAllElements(this.elements.map { it.toProto() })
        .build()
}

fun ConditionalExpression.toProto(): ProtoConditionalExpression {
    return ProtoConditionalExpression.newBuilder()
        .setCondition(this.condition.toProto())
        .setResultExpression(this.resultExpression.toProto())
        .build()
}

fun Formula.toProto(): ProtoFormula {
    val builder = ProtoFormula.newBuilder()
        .setId(this.id)
        .setName(this.name)
    this.expression?.let { builder.expression = it.toProto() }
    this.conditions?.let { builder.addAllConditions(it.map { c -> c.toProto() }) }
    this.defaultExpression?.let { builder.defaultExpression = it.toProto() }
    return builder.build()
}

fun InputField.toProto(): ProtoInputField {
    val builder = ProtoInputField.newBuilder()
        .setId(this.id)
        .setLabel(this.label)
        .setLinkedVariableId(this.linkedVariableId)
    this.description?.let { builder.description = it }
    return builder.build()
}

// --- From Proto Mappers ---

fun ProtoDataType.toDomain(): DataType {
    return when (this) {
        ProtoDataType.NUMBER -> DataType.NUMBER
        ProtoDataType.TEXT -> DataType.TEXT
        ProtoDataType.BOOLEAN -> DataType.BOOLEAN
        ProtoDataType.CHOICE -> DataType.CHOICE
        ProtoDataType.UNRECOGNIZED, ProtoDataType.PROTO_DATA_TYPE_UNSPECIFIED -> DataType.TEXT // Default or throw
    }
}

fun ProtoVariable.toDomain(): Variable {
    return Variable(
        id = this.id,
        name = this.name,
        description = if (this.hasDescription()) this.description else null,
        initialValueString = this.initialValueString,
        type = this.type.toDomain(),
        options = if (this.optionsList.isNotEmpty()) this.optionsList else null
    )
}

fun ProtoExpressionElement.toDomain(): ExpressionElement {
    return when (this.elementTypeCase) {
        ProtoExpressionElement.ElementTypeCase.VARIABLE_ELEMENT -> VariableElement(this.variableElement.variableId)
        ProtoExpressionElement.ElementTypeCase.OPERATOR_ELEMENT -> OperatorElement(this.operatorElement.operatorString)
        ProtoExpressionElement.ElementTypeCase.LITERAL_ELEMENT -> LiteralElement(this.literalElement.valueString)
        ProtoExpressionElement.ElementTypeCase.ELEMENTTYPE_NOT_SET, null -> throw IllegalArgumentException("Unknown ExpressionElement type")
    }
}

fun ProtoExpression.toDomain(): Expression {
    return Expression(
        elements = this.elementsList.map { it.toDomain() }
    )
}

fun ProtoConditionalExpression.toDomain(): ConditionalExpression {
    return ConditionalExpression(
        condition = this.condition.toDomain(),
        resultExpression = this.resultExpression.toDomain()
    )
}

fun ProtoFormula.toDomain(): Formula {
    return Formula(
        id = this.id,
        name = this.name,
        expression = if (this.hasExpression()) this.expression.toDomain() else null,
        conditions = if (this.conditionsList.isNotEmpty()) this.conditionsList.map { it.toDomain() } else null,
        defaultExpression = if (this.hasDefaultExpression()) this.defaultExpression.toDomain() else null
    )
}

fun ProtoInputField.toDomain(): InputField {
    return InputField(
        id = this.id,
        label = this.label,
        description = if (this.hasDescription()) this.description else null,
        linkedVariableId = this.linkedVariableId
    )
}
