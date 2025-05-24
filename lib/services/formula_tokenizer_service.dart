import '../models/formula_token.dart';
import '../models/variable_model.dart'; // Assuming this path

class FormulaTokenizerService {
  // Define operators, sorted by length to match longer ones first (e.g., "==" before "=")
  static final List<MapEntry<String, FormulaTokenType>> _operatorDefinitions = [
    // Comparison
    MapEntry(">=", FormulaTokenType.OPERATOR_COMPARISON),
    MapEntry("<=", FormulaTokenType.OPERATOR_COMPARISON),
    MapEntry("==", FormulaTokenType.OPERATOR_COMPARISON),
    MapEntry("!=", FormulaTokenType.OPERATOR_COMPARISON),
    MapEntry(">", FormulaTokenType.OPERATOR_COMPARISON),
    MapEntry("<", FormulaTokenType.OPERATOR_COMPARISON),
    // Logical
    MapEntry("&&", FormulaTokenType.OPERATOR_LOGICAL),
    MapEntry("||", FormulaTokenType.OPERATOR_LOGICAL),
    MapEntry("!", FormulaTokenType.OPERATOR_LOGICAL),
    // Arithmetic
    MapEntry("+", FormulaTokenType.OPERATOR_ARITHMETIC),
    MapEntry("-", FormulaTokenType.OPERATOR_ARITHMETIC),
    MapEntry("*", FormulaTokenType.OPERATOR_ARITHMETIC),
    MapEntry("/", FormulaTokenType.OPERATOR_ARITHMETIC),
    MapEntry("%", FormulaTokenType.OPERATOR_ARITHMETIC),
    // Parentheses
    MapEntry("(", FormulaTokenType.PARENTHESIS_OPEN),
    MapEntry(")", FormulaTokenType.PARENTHESIS_CLOSE),
  ];

  // Regex for numbers (integers and decimals)
  static final RegExp _numberRegExp = RegExp(r'^\d+(\.\d+)?');
  // Regex for string literals (double and single quoted)
  static final RegExp _stringDoubleQuoteRegExp = RegExp(r'^"[^"]*"');
  static final RegExp _stringSingleQuoteRegExp = RegExp(r"^'[^']*'");
  // Regex for whitespace
  static final RegExp _whitespaceRegExp = RegExp(r'^\s+');

  List<FormulaToken> tokenize(String expression, List<Variable> knownVariables) {
    final List<FormulaToken> tokens = [];
    int currentIndex = 0;

    // Sort knownVariables by name length in descending order
    // This helps in matching longer variable names first (e.g., "var_long" before "var")
    final List<Variable> sortedVariables = List.from(knownVariables);
    sortedVariables.sort((a, b) => b.name.length.compareTo(a.name.length));

    while (currentIndex < expression.length) {
      bool matched = false;
      final String remainingExpression = expression.substring(currentIndex);

      // Priority 1: Known variables
      for (final variable in sortedVariables) {
        if (remainingExpression.startsWith(variable.name)) {
          tokens.add(FormulaToken(
            type: FormulaTokenType.VARIABLE,
            text: variable.name,
            startIndex: currentIndex,
            endIndex: currentIndex + variable.name.length,
          ));
          currentIndex += variable.name.length;
          matched = true;
          break;
        }
      }
      if (matched) continue;

      // Priority 2: String Literals (must come before operators to correctly tokenize strings containing operator-like characters)
      Match? stringMatch = _stringDoubleQuoteRegExp.firstMatch(remainingExpression);
      if (stringMatch == null) {
        stringMatch = _stringSingleQuoteRegExp.firstMatch(remainingExpression);
      }
      if (stringMatch != null) {
        final String text = stringMatch.group(0)!;
        tokens.add(FormulaToken(
          type: FormulaTokenType.STRING_LITERAL,
          text: text,
          startIndex: currentIndex,
          endIndex: currentIndex + text.length,
        ));
        currentIndex += text.length;
        matched = true;
        if (matched) continue;
      }
      
      // Priority 3: Operators
      for (final opEntry in _operatorDefinitions) {
        if (remainingExpression.startsWith(opEntry.key)) {
          tokens.add(FormulaToken(
            type: opEntry.value,
            text: opEntry.key,
            startIndex: currentIndex,
            endIndex: currentIndex + opEntry.key.length,
          ));
          currentIndex += opEntry.key.length;
          matched = true;
          break;
        }
      }
      if (matched) continue;

      // Priority 4: Number literals
      final Match? numberMatch = _numberRegExp.firstMatch(remainingExpression);
      if (numberMatch != null) {
        final String text = numberMatch.group(0)!;
        tokens.add(FormulaToken(
          type: FormulaTokenType.NUMBER_LITERAL,
          text: text,
          startIndex: currentIndex,
          endIndex: currentIndex + text.length,
        ));
        currentIndex += text.length;
        matched = true;
        if (matched) continue;
      }

      // Priority 5: Whitespace
      final Match? whitespaceMatch = _whitespaceRegExp.firstMatch(remainingExpression);
      if (whitespaceMatch != null) {
        final String text = whitespaceMatch.group(0)!;
        tokens.add(FormulaToken(
          type: FormulaTokenType.WHITESPACE,
          text: text,
          startIndex: currentIndex,
          endIndex: currentIndex + text.length,
        ));
        currentIndex += text.length;
        matched = true;
        if (matched) continue;
      }

      // If none of the above matched, add as UNKNOWN
      if (!matched) {
        // Consume at least one character to avoid infinite loops on unknown characters
        final String unknownText = remainingExpression.substring(0, 1);
        tokens.add(FormulaToken(
          type: FormulaTokenType.UNKNOWN,
          text: unknownText, // Take one char to avoid infinite loop
          startIndex: currentIndex,
          endIndex: currentIndex + 1,
        ));
        currentIndex += 1;
      }
    }
    return tokens;
  }
}

/*
// Example Usage:
// Assuming Variable and FormulaToken/Type are imported.
// And VariableType enum exists for Variable.

void main() {
  // Define some known variables
  List<Variable> variables = [
    Variable(id: "1", name: "salaire_brut", type: VariableType.number, initialValue: 0),
    Variable(id: "2", name: "taux_impot", type: VariableType.number, initialValue: 0),
    Variable(id: "3", name: "salaire", type: VariableType.number, initialValue: 0), // Shorter, to test sorting
    Variable(id: "4", name: "message", type: VariableType.text, initialValue: ""),
  ];

  FormulaTokenizerService tokenizer = FormulaTokenizerService();

  String expression1 = "salaire_brut - (salaire_brut * taux_impot / 100)";
  print("Tokenizing: $expression1");
  List<FormulaToken> tokens1 = tokenizer.tokenize(expression1, variables);
  tokens1.forEach((token) => print('${token.type}: "${token.text}" [${token.startIndex}-${token.endIndex}]'));
  // Expected:
  // VARIABLE: "salaire_brut" [0-12]
  // WHITESPACE: " " [12-13]
  // OPERATOR_ARITHMETIC: "-" [13-14]
  // WHITESPACE: " " [14-15]
  // PARENTHESIS_OPEN: "(" [15-16]
  // VARIABLE: "salaire_brut" [16-28]
  // WHITESPACE: " " [28-29]
  // OPERATOR_ARITHMETIC: "*" [29-30]
  // WHITESPACE: " " [30-31]
  // VARIABLE: "taux_impot" [31-41]
  // WHITESPACE: " " [41-42]
  // OPERATOR_ARITHMETIC: "/" [42-43]
  // WHITESPACE: " " [43-44]
  // NUMBER_LITERAL: "100" [44-47]
  // PARENTHESIS_CLOSE: ")" [47-48]

  print("\nTokenizing: salaire > 1000 && message == \"actif\"");
  List<FormulaToken> tokens2 = tokenizer.tokenize("salaire > 1000 && message == \"actif\"", variables);
  tokens2.forEach((token) => print('${token.type}: "${token.text}" [${token.startIndex}-${token.endIndex}]'));
  // Expected:
  // VARIABLE: "salaire" [0-7]
  // WHITESPACE: " " [7-8]
  // OPERATOR_COMPARISON: ">" [8-9]
  // WHITESPACE: " " [9-10]
  // NUMBER_LITERAL: "1000" [10-14]
  // WHITESPACE: " " [14-15]
  // OPERATOR_LOGICAL: "&&" [15-17]
  // WHITESPACE: " " [17-18]
  // VARIABLE: "message" [18-25]
  // WHITESPACE: " " [25-26]
  // OPERATOR_COMPARISON: "==" [26-28]
  // WHITESPACE: " " [28-29]
  // STRING_LITERAL: "\"actif\"" [29-36]
  
  print("\nTokenizing: 'string with spaces' + unknown#char");
  List<FormulaToken> tokens3 = tokenizer.tokenize("'string with spaces' + unknown#char", variables);
  tokens3.forEach((token) => print('${token.type}: "${token.text}" [${token.startIndex}-${token.endIndex}]'));
  // Expected:
  // STRING_LITERAL: "'string with spaces'" [0-20]
  // WHITESPACE: " " [20-21]
  // OPERATOR_ARITHMETIC: "+" [21-22]
  // WHITESPACE: " " [22-23]
  // VARIABLE: "unknown" (assuming 'unknown' is a variable for this example, otherwise it'd be UNKNOWN spam)
  // -- If 'unknown' is not a variable:
  // UNKNOWN: "u"
  // UNKNOWN: "n"
  // ...
  // UNKNOWN: "#"
  // ...

  // To test the above 'unknown' case correctly, let's assume 'unknown_var' is a variable
  List<Variable> vars_with_unknown = [
      Variable(id: "5", name: "unknown_var", type: VariableType.text)
  ];
  print("\nTokenizing with 'unknown_var': unknown_var + #other");
  List<FormulaToken> tokens4 = tokenizer.tokenize("unknown_var + #other", vars_with_unknown);
  tokens4.forEach((token) => print('${token.type}: "${token.text}" [${token.startIndex}-${token.endIndex}]'));
  // Expected:
  // VARIABLE: "unknown_var" [0-11]
  // WHITESPACE: " " [11-12]
  // OPERATOR_ARITHMETIC: "+" [12-13]
  // WHITESPACE: " " [13-14]
  // UNKNOWN: "#" [14-15]
  // UNKNOWN: "o" [15-16]
  // UNKNOWN: "t" [16-17]
  // UNKNOWN: "h" [17-18]
  // UNKNOWN: "e" [18-19]
  // UNKNOWN: "r" [19-20]
}
*/
