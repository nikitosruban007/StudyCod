package org.example;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AutoComplete {

    public static void setupAutoCompletion(CodeArea codeEditor) {
        List<String> suggestions = new ArrayList<>();

        suggestions.addAll(List.of(
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "try", "void", "volatile", "while"
        ));

        PopupControl popup = new PopupControl();
        ListView<String> suggestionList = new ListView<>();
        suggestionList.setMaxHeight(100); // Обмеження висоти
        popup.getScene().setRoot(suggestionList);
        popup.setAutoHide(true);

        codeEditor.setOnKeyReleased(event -> {
            if (event.getCode().isLetterKey() || event.getCode() == KeyCode.BACK_SPACE) {
                String currentWord = getCurrentWord(codeEditor);
                List<String> filteredSuggestions = suggestions.stream()
                        .filter(s -> s.startsWith(currentWord))
                        .collect(Collectors.toList());

                if (!filteredSuggestions.isEmpty()) {
                    suggestionList.getItems().setAll(filteredSuggestions);
                    showPopup(codeEditor, popup, suggestionList);
                } else {
                    popup.hide();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                popup.hide();
            }
        });

        codeEditor.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && popup.isShowing() &&
                    !suggestionList.getSelectionModel().isEmpty()) {
                String selectedSuggestion = suggestionList.getSelectionModel().getSelectedItem();
                replaceCurrentWord(codeEditor, selectedSuggestion);
                popup.hide();
                event.consume();
            }
        });

        suggestionList.setOnMouseClicked(event -> {
            if (!suggestionList.getSelectionModel().isEmpty()) {
                String selectedSuggestion = suggestionList.getSelectionModel().getSelectedItem();
                replaceCurrentWord(codeEditor, selectedSuggestion);
                popup.hide();
            }
        });
    }

    private static String getCurrentWord(CodeArea codeEditor) {
        int caretPosition = codeEditor.getCaretPosition();
        String textUpToCaret = codeEditor.getText(0, caretPosition);
        int lastSpaceIndex = Math.max(
                textUpToCaret.lastIndexOf(' '),
                Math.max(textUpToCaret.lastIndexOf('\n'), textUpToCaret.lastIndexOf('\t'))
        );
        return textUpToCaret.substring(lastSpaceIndex + 1);
    }

    private static void replaceCurrentWord(CodeArea codeEditor, String replacement) {
        int caretPosition = codeEditor.getCaretPosition();
        String textUpToCaret = codeEditor.getText(0, caretPosition);
        int lastSpaceIndex = Math.max(
                textUpToCaret.lastIndexOf(' '),
                Math.max(textUpToCaret.lastIndexOf('\n'), textUpToCaret.lastIndexOf('\t'))
        );
        codeEditor.replaceText(lastSpaceIndex + 1, caretPosition, replacement);
    }

    private static void showPopup(CodeArea codeEditor, PopupControl popup, ListView<String> suggestionList) {
        codeEditor.getCaretBounds().ifPresent(bounds -> {
            Point2D screenPosition = codeEditor.localToScreen(bounds.getMaxX(), bounds.getMinY());
            popup.show(codeEditor, screenPosition.getX(), screenPosition.getY());
        });
    }
}
