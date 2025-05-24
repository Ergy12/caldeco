import 'package:flutter/material.dart';
import '../models/formula_token.dart'; // Assuming this path is correct

class FormulaDisplayWidget extends StatelessWidget {
  final List<FormulaToken> tokens;

  const FormulaDisplayWidget({Key? key, required this.tokens}) : super(key: key);

  TextStyle _getStyleForToken(FormulaToken token) {
    switch (token.type) {
      case FormulaTokenType.VARIABLE:
        return const TextStyle(color: Colors.black);
      case FormulaTokenType.NUMBER_LITERAL:
      case FormulaTokenType.STRING_LITERAL:
        return const TextStyle(color: Colors.blue);
      case FormulaTokenType.OPERATOR_ARITHMETIC:
        return const TextStyle(color: Colors.black); // Placeholder, might need background for white text
      case FormulaTokenType.OPERATOR_LOGICAL:
      case FormulaTokenType.OPERATOR_COMPARISON:
        return const TextStyle(color: Colors.green);
      case FormulaTokenType.PARENTHESIS_OPEN:
      case FormulaTokenType.PARENTHESIS_CLOSE:
        return const TextStyle(color: Colors.grey);
      case FormulaTokenType.WHITESPACE:
        return const TextStyle(color: Colors.black); // Or default without specific color
      case FormulaTokenType.UNKNOWN:
        return const TextStyle(color: Colors.red);
      default:
        return const TextStyle(color: Colors.black); // Default style
    }
  }

  @override
  Widget build(BuildContext context) {
    if (tokens.isEmpty) {
      return const SizedBox.shrink(); // Return an empty widget if there are no tokens
    }

    List<TextSpan> textSpans = tokens.map((token) {
      return TextSpan(
        text: token.text,
        style: _getStyleForToken(token),
      );
    }).toList();

    return RichText(
      text: TextSpan(
        children: textSpans,
      ),
      // Consider adding textDirection if not inherited properly, though usually it is.
      // textDirection: TextDirection.ltr,
    );
  }
}
