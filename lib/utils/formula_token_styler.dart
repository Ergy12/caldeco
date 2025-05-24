import 'package:flutter/material.dart';
import '../models/formula_token.dart'; // Assuming path

class FormulaTokenStyler {
  static Widget getTokenWidget(FormulaToken token, BuildContext context, {double fontSize = 16.0}) {
    TextStyle textStyle;
    Color backgroundColor;
    EdgeInsets padding = const EdgeInsets.symmetric(horizontal: 4.0, vertical: 2.0);
    BorderRadius borderRadius = BorderRadius.circular(4.0);

    // Use DefaultTextStyle.of(context).style as a base for text styles
    // to ensure better theme compatibility (e.g. for dark mode)
    final defaultTextStyle = DefaultTextStyle.of(context).style;

    switch (token.type) {
      case FormulaTokenType.VARIABLE:
        textStyle = defaultTextStyle.copyWith(color: Colors.black, fontSize: fontSize);
        backgroundColor = Colors.grey[300]!; // Light grey
        break;
      case FormulaTokenType.NUMBER_LITERAL:
      case FormulaTokenType.STRING_LITERAL:
        textStyle = defaultTextStyle.copyWith(color: Colors.blue[800]!, fontSize: fontSize);
        backgroundColor = Colors.blue[50]!; // Very light blue
        break;
      case FormulaTokenType.OPERATOR_ARITHMETIC:
        textStyle = defaultTextStyle.copyWith(color: Colors.white, fontSize: fontSize);
        backgroundColor = Colors.blueGrey[600]!; // Blue-grey
        break;
      case FormulaTokenType.OPERATOR_LOGICAL:
      case FormulaTokenType.OPERATOR_COMPARISON:
        textStyle = defaultTextStyle.copyWith(color: Colors.white, fontSize: fontSize);
        backgroundColor = Colors.green[800]!; // Dark green
        break;
      case FormulaTokenType.PARENTHESIS_OPEN:
      case FormulaTokenType.PARENTHESIS_CLOSE:
        textStyle = defaultTextStyle.copyWith(color: Colors.black, fontSize: fontSize);
        backgroundColor = Colors.grey[200]!; // Very light grey
        break;
      case FormulaTokenType.UNKNOWN:
        textStyle = defaultTextStyle.copyWith(color: Colors.white, fontSize: fontSize);
        backgroundColor = Colors.red[700]!; // Red
        break;
      case FormulaTokenType.WHITESPACE:
        // For whitespace, return a simple Text widget.
        // It's recommended to use Wrap's spacing for layout rather than specific width Text widgets for spaces,
        // but if the token text itself contains multiple spaces, this will preserve them.
        return Text(token.text, style: defaultTextStyle.copyWith(fontSize: fontSize));
      default: // Should not happen if all types are covered
        textStyle = defaultTextStyle.copyWith(color: Colors.purple, fontSize: fontSize); // Distinct style for unhandled
        backgroundColor = Colors.purple[50]!;
    }

    return Container(
      padding: padding,
      margin: const EdgeInsets.symmetric(horizontal: 1.0), // Add slight margin between chips
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: borderRadius,
      ),
      child: Text(
        token.text,
        style: textStyle,
      ),
    );
  }
}
