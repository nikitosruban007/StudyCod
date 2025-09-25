package org.example;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.CodeArea;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterHints {

    private static final Map<String, List<String>> METHOD_SIGNATURES = new HashMap<>();

    static {
        METHOD_SIGNATURES.put("println", List.of("println()", "println(Object x)", "println(String x)", "println(int x)"));
        METHOD_SIGNATURES.put("print", List.of("print(Object x)", "print(String x)", "print(int x)"));
        METHOD_SIGNATURES.put("substring", List.of("substring(int beginIndex)", "substring(int beginIndex, int endIndex)"));
        METHOD_SIGNATURES.put("indexOf", List.of("indexOf(int ch)", "indexOf(String str)", "indexOf(String str, int fromIndex)"));
        METHOD_SIGNATURES.put("charAt", List.of("charAt(int index)"));
        METHOD_SIGNATURES.put("add", List.of("add(E e)", "add(int index, E element)"));
        METHOD_SIGNATURES.put("put", List.of("put(K key, V value)"));
        METHOD_SIGNATURES.put("get", List.of("get(int index)", "get(Object key)"));
        METHOD_SIGNATURES.put("sort", List.of("Arrays.sort(T[] a)", "Collections.sort(List<T> list)", "Collections.sort(List<T> list, Comparator<? super T> c)"));
        METHOD_SIGNATURES.put("max", List.of("Math.max(int a, int b)", "Math.max(long a, long b)", "Math.max(double a, double b)"));
        METHOD_SIGNATURES.put("min", List.of("Math.min(int a, int b)", "Math.min(long a, long b)", "Math.min(double a, double b)"));
        METHOD_SIGNATURES.put("format", List.of("String.format(String format, Object... args)"));
        METHOD_SIGNATURES.put("equals", List.of("equals(Object obj)"));
        METHOD_SIGNATURES.put("valueOf", List.of("String.valueOf(Object obj)", "String.valueOf(int i)", "Integer.valueOf(String s)"));
        METHOD_SIGNATURES.put("split", List.of("split(String regex)", "split(String regex, int limit)"));
        METHOD_SIGNATURES.put("replace", List.of("replace(CharSequence target, CharSequence replacement)", "replace(char oldChar, char newChar)"));
        METHOD_SIGNATURES.put("join", List.of("String.join(CharSequence delimiter, CharSequence... elements)", "String.join(CharSequence delimiter, Iterable<? extends CharSequence> elements)"));
        METHOD_SIGNATURES.put("map", List.of("stream.map(Function<? super T,? extends R> mapper)"));
        METHOD_SIGNATURES.put("filter", List.of("stream.filter(Predicate<? super T> predicate)"));
        METHOD_SIGNATURES.put("reduce", List.of("stream.reduce(T identity, BinaryOperator<T> accumulator)", "stream.reduce(BinaryOperator<T> accumulator)"));
    }

    public static void attach(CodeArea codeArea) {
        PopupControl popup = new PopupControl();
        ListView<Label> listView = new ListView<>();
        listView.setMaxHeight(160);
        popup.getScene().setRoot(listView);
        popup.setAutoHide(true);

        codeArea.setOnKeyTyped(e -> {
            String ch = e.getCharacter();
            if (ch == null || ch.isEmpty()) return;

            if ("(".equals(ch) || ",".equals(ch)) {
                showOrUpdateHints(codeArea, popup, listView);
            } else if (")".equals(ch) || ";".equals(ch) || "\n".equals(ch)) {
                popup.hide();
            }
        });

        codeArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
            if (popup.isShowing()) {
                if (!updateHints(codeArea, listView)) {
                    popup.hide();
                }
            }
        });

        codeArea.focusedProperty().addListener((obs, was, is) -> {
            if (!is) popup.hide();
        });

        codeArea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) popup.hide();
        });
    }

    private static void showOrUpdateHints(CodeArea codeArea, PopupControl popup, ListView<Label> listView) {
        boolean has = updateHints(codeArea, listView);
        if (!has) {
            popup.hide();
            return;
        }
        codeArea.getCaretBounds().ifPresent(bounds -> {
            var screen = codeArea.localToScreen(bounds.getMaxX(), bounds.getMaxY());
            popup.show(codeArea, screen.getX(), screen.getY() + 6);
        });
    }

    private static boolean updateHints(CodeArea codeArea, ListView<Label> listView) {
        int caret = codeArea.getCaretPosition();
        String text = codeArea.getText(0, Math.max(caret, 0));
        CallContext ctx = findEnclosingCall(text);
        if (ctx == null) return false;

        List<String> overloads = METHOD_SIGNATURES.get(ctx.method);
        if (overloads == null || overloads.isEmpty()) return false;

        listView.getItems().clear();
        int paramIndex = Math.max(ctx.argIndex, 0);

        for (String sig : overloads) {
            Label label = new Label(highlightParam(sig, paramIndex));
            label.getStyleClass().add("param-hint");
            listView.getItems().add(label);
        }
        return true;
    }

    private static String highlightParam(String signature, int index) {
        int l = signature.indexOf('(');
        int r = signature.lastIndexOf(')');
        if (l < 0 || r < l) return signature;

        String inside = signature.substring(l + 1, r);
        if (inside.isBlank()) return signature;

        String[] params = inside.split("\\s*,\\s*");
        if (index < 0 || index >= params.length) return signature;

        params[index] = "[" + params[index] + "]";
        return signature.substring(0, l + 1) + String.join(", ", params) + signature.substring(r);
    }

    private static CallContext findEnclosingCall(String textUpToCaret) {
        int depth = 0;
        int argIndex = 0;

        for (int i = textUpToCaret.length() - 1; i >= 0; i--) {
            char c = textUpToCaret.charAt(i);
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                if (depth == 0) {
                    // find method identifier before '('
                    int j = i - 1;
                    // skip spaces
                    while (j >= 0 && Character.isWhitespace(textUpToCaret.charAt(j))) j--;
                    int end = j;
                    while (j >= 0) {
                        char cj = textUpToCaret.charAt(j);
                        if (Character.isLetterOrDigit(cj) || cj == '_') {
                            j--;
                        } else {
                            break;
                        }
                    }
                    String method = textUpToCaret.substring(j + 1, end + 1);
                    if (!method.isEmpty()) {
                        return new CallContext(method, argIndex);
                    } else {
                        return null;
                    }
                } else {
                    depth--;
                }
            } else if (c == ',' && depth == 0) {
                argIndex++;
            }
        }
        return null;
    }

    private static class CallContext {
        final String method;
        final int argIndex;

        CallContext(String method, int argIndex) {
            this.method = method;
            this.argIndex = argIndex;
        }
    }
}
