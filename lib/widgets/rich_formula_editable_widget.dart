import 'package:flutter/material.dart';
import '../models/formula_token.dart';
import '../models/variable_model.dart';
import '../services/formula_tokenizer_service.dart';
import '../utils/formula_token_styler.dart'; // Import the styler

// Custom TextEditingController
class FormulaTextEditingController extends TextEditingController {
  final FormulaTokenizerService tokenizerService;
  final List<Variable> knownVariables;
  final BuildContext context; // To access Theme for default text style

  FormulaTextEditingController({
    String? text,
    required this.tokenizerService,
    required this.knownVariables,
    required this.context,
  }) : super(text: text);

  @override
  TextSpan buildTextSpan({required BuildContext context, TextStyle? style, required bool withComposing}) {
    // If composing, use default behavior to show the intermediate composing text.
    if (withComposing) {
      return super.buildTextSpan(context: context, style: style, withComposing: withComposing);
    }

    final List<FormulaToken> tokens = tokenizerService.tokenize(text, knownVariables);
    final List<InlineSpan> spans = [];

    // Use the context passed to buildTextSpan for DefaultTextStyle, or fall back to the controller's context.
    // This 'context' is more current for styling.
    final BuildContext effectiveContext = context; 
    final TextStyle defaultStyle = style ?? DefaultTextStyle.of(effectiveContext).style.copyWith(fontSize: 16);


    if (tokens.isEmpty && text.isNotEmpty) {
      // If text is not empty but no tokens (e.g. during initial typing before a recognizable token)
      // Show the plain text to make typing feel responsive.
      return TextSpan(text: text, style: defaultStyle);
    }
    
    if (tokens.isEmpty && text.isEmpty) {
        // Hint text is handled by TextField's decoration, so return empty span for empty text.
        return TextSpan(text: "", style: defaultStyle);
    }


    for (final token in tokens) {
      if (token.type == FormulaTokenType.WHITESPACE) {
        // Use TextSpan for whitespace to allow normal text flow and selection.
        spans.add(TextSpan(text: token.text, style: defaultStyle));
      } else {
        spans.add(
          WidgetSpan(
            // Use the shared styler here, passing the more current 'context' from buildTextSpan
            child: FormulaTokenStyler.getTokenWidget(token, effectiveContext, fontSize: defaultStyle.fontSize ?? 16.0),
            alignment: PlaceholderAlignment.middle, 
          )
        );
      }
    }
    return TextSpan(children: spans, style: defaultStyle);
  }
}

class RichFormulaEditableWidget extends StatefulWidget {
  final TextEditingController textController; // The one from FormulaEditor (_expressionController)
  final List<Variable> knownVariables;
  final FocusNode? focusNode; // Optional: pass focus node from parent

  const RichFormulaEditableWidget({
    Key? key,
    required this.textController,
    required this.knownVariables,
    this.focusNode,
  }) : super(key: key);

  @override
  State<RichFormulaEditableWidget> createState() => _RichFormulaEditableWidgetState();
}

class _RichFormulaEditableWidgetState extends State<RichFormulaEditableWidget> {
  late FormulaTextEditingController _formulaController;
  final FormulaTokenizerService _tokenizerService = FormulaTokenizerService();

  @override
  void initState() {
    super.initState();
    _formulaController = FormulaTextEditingController(
      text: widget.textController.text, // Initialize with current text
      tokenizerService: _tokenizerService,
      knownVariables: widget.knownVariables,
      context: context, // Provide BuildContext
    );

    // Sync changes from the external controller to the internal one
    widget.textController.addListener(_updateInternalController);
    // Sync changes from the internal controller back to the external one
    _formulaController.addListener(_updateExternalController);
  }
  
  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Update context if it changes, although less common for controller like this
    // _formulaController.context = context; // if context needed to be dynamic
  }


  void _updateInternalController() {
    if (widget.textController.text != _formulaController.text) {
      _formulaController.text = widget.textController.text;
      // Move cursor to the end, simple sync. More complex cursor sync is hard.
      _formulaController.selection = TextSelection.fromPosition(
        TextPosition(offset: _formulaController.text.length),
      );
    }
  }

  void _updateExternalController() {
    if (_formulaController.text != widget.textController.text) {
      widget.textController.text = _formulaController.text;
      widget.textController.selection = TextSelection.fromPosition(
        TextPosition(offset: widget.textController.text.length),
      );
    }
  }

  @override
  void dispose() {
    widget.textController.removeListener(_updateInternalController);
    _formulaController.removeListener(_updateExternalController);
    _formulaController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Using TextField with onKeyEvent via FocusNode
    return TextField(
      controller: _formulaController,
      focusNode: _effectiveFocusNode, // Use the managed focus node
      decoration: const InputDecoration(
        border: OutlineInputBorder(),
        hintText: 'Saisir la formule ici...',
      ),
      keyboardType: TextInputType.text,
      maxLines: null,
      // style: TextStyle(fontSize: 16), // Base style for any plain text parts
    );
  }

  KeyEventResult _handleKeyEvent(FocusNode node, KeyEvent event) {
    if (event is KeyDownEvent && event.logicalKey == LogicalKeyboardKey.backspace) {
      final currentText = _formulaController.text;
      final currentSelection = _formulaController.selection;

      if (currentSelection.isCollapsed && currentSelection.start > 0) {
        final cursorPosition = currentSelection.start;
        
        final List<FormulaToken> tokens = _tokenizerService.tokenize(currentText, widget.knownVariables);
        
        FormulaToken? tokenToDelete;
        for (final token in tokens) {
          // Check if cursor is immediately after a non-whitespace token
          if (token.endIndex == cursorPosition && token.type != FormulaTokenType.WHITESPACE) {
            tokenToDelete = token;
            break;
          }
        }

        if (tokenToDelete != null) {
          // Found a token to delete entirely.
          final newText = currentText.substring(0, tokenToDelete.startIndex) + 
                          currentText.substring(tokenToDelete.endIndex);
          
          _formulaController.value = TextEditingValue(
            text: newText,
            selection: TextSelection.collapsed(offset: tokenToDelete.startIndex),
          );
          return KeyEventResult.handled; // Event handled, prevent default TextField behavior
        }
      }
    }
    return KeyEventResult.ignored; // Event not handled, allow default TextField behavior
  }

  // Manage FocusNode
  late FocusNode _internalFocusNode;
  FocusNode get _effectiveFocusNode => widget.focusNode ?? _internalFocusNode;

  @override
  void initState() {
    super.initState();
    _internalFocusNode = FocusNode();
    _effectiveFocusNode.onKeyEvent = _handleKeyEvent; // Assign the key event handler

    _formulaController = FormulaTextEditingController(
      text: widget.textController.text,
      tokenizerService: _tokenizerService,
      knownVariables: widget.knownVariables,
      context: context,
    );

    widget.textController.addListener(_updateInternalController);
    _formulaController.addListener(_updateExternalController);
  }

  @override
  void dispose() {
    widget.textController.removeListener(_updateInternalController);
    _formulaController.removeListener(_updateExternalController);
    _formulaController.dispose();
    // If _internalFocusNode was used (i.e. widget.focusNode was null), remove its onKeyEvent handler.
    // If widget.focusNode was provided, it's the parent's responsibility to manage its listeners.
    if (widget.focusNode == null) {
      _internalFocusNode.onKeyEvent = null; 
    }
    _internalFocusNode.dispose();
    super.dispose();
  }
}
