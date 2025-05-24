enum FormulaTokenType {
  VARIABLE,
  NUMBER_LITERAL,
  STRING_LITERAL,
  OPERATOR_ARITHMETIC,
  OPERATOR_LOGICAL,
  OPERATOR_COMPARISON,
  PARENTHESIS_OPEN,
  PARENTHESIS_CLOSE,
  WHITESPACE,
  UNKNOWN,
}

class FormulaToken {
  final FormulaTokenType type;
  final String text;
  final int startIndex;
  final int endIndex;

  FormulaToken({
    required this.type,
    required this.text,
    required this.startIndex,
    required this.endIndex,
  });

  @override
  String toString() {
    return 'FormulaToken(type: $type, text: "$text", startIndex: $startIndex, endIndex: $endIndex)';
  }
}

// Also need Variable model for the tokenizer service method signature
// For now, let's define a minimal version here if not already globally available
// or assume it exists in 'variable_model.dart' as per previous context.
// Assuming 'package:example_project/models/variable_model.dart' exists
// For the purpose of this file, if Variable is needed for testing, it would be imported.
// For now, FormulaToken and FormulaTokenType are self-contained.

/*
// Minimal Variable class for context if it were needed directly in this file
// (it's actually needed by the service, which will import this file and the variable model)
enum VariableType {
  text,
  number,
  boolean,
  date,
  // other types as needed
}

class Variable {
  final String id;
  final String name;
  final VariableType type;
  dynamic initialValue; // Or specific types based on VariableType
  String? description;
  List<String>? allowedValues; // For dropdowns/enums

  Variable({
    required this.id,
    required this.name,
    required this.type,
    this.initialValue,
    this.description,
    this.allowedValues,
  });
}
*/
