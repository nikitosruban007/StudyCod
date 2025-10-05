package org.example.services;

import java.util.HashMap;
import java.util.Map;

public final class LanguageManager {
    public enum Lang { UK, EN }

    private static Lang current = Lang.UK;

    private static final Map<String, String> UK = new HashMap<>();
    private static final Map<String, String> EN = new HashMap<>();

    static {
        UK.put("nav.tasks", "Завдання");
        UK.put("nav.grades", "Оцінки");
        UK.put("welcome.guest", "Вітаю в StudyCod!");
        UK.put("welcome.user", "Вітаю, %s!");
        UK.put("advice.prefix", "Мотиваційна порада перед навчанням😉: ");
        UK.put("advice.loading", "Завантаження поради...");
        UK.put("advice.askAuth", "Увійдіть або зареєструйтесь");
        UK.put("auth.required", "Будь ласка, увійдіть в систему");
        UK.put("auth.title", "Авторизація");
        UK.put("profile.title", "Профіль");

        UK.put("loading.lesson", "Завантаження уроку...");
        UK.put("loading.task", "Завантаження завдання...");
        UK.put("loading.grading", "Оцінювання виконується... Зачекайте, будь ласка.");
        UK.put("ai.hint.prefix", "\n\nПідказка від AI:\n");
        UK.put("compilation.failed", "Compilation failed:\n");

        UK.put("grades.export.title", "Збереження Журналу Оцінювання в PDF");
        UK.put("grades.export.filename", "Журнал Оцінювання %s %s.pdf");

        EN.put("nav.tasks", "Tasks");
        EN.put("nav.grades", "Grades");
        EN.put("welcome.guest", "Welcome to StudyCod!");
        EN.put("welcome.user", "Welcome, %s!");
        EN.put("advice.prefix", "Motivational tip before studying😉: ");
        EN.put("advice.loading", "Loading advice...");
        EN.put("advice.askAuth", "Sign in or register");
        EN.put("auth.required", "Please log in");
        EN.put("auth.title", "Login");
        EN.put("profile.title", "Profile");

        EN.put("loading.lesson", "Loading lesson...");
        EN.put("loading.task", "Loading task...");
        EN.put("loading.grading", "Grading in progress... Please wait.");
        EN.put("ai.hint.prefix", "\n\nAI hint:\n");
        EN.put("compilation.failed", "Compilation failed:\n");

        EN.put("grades.export.title", "Save Grade Journal to PDF");
        EN.put("grades.export.filename", "Grade Journal %s %s.pdf");
    }

    private LanguageManager() {}

    public static void set(Lang lang) {
        current = lang;
    }

    public static void toggle() {
        current = (current == Lang.UK) ? Lang.EN : Lang.UK;
    }

    public static Lang get() {
        return current;
    }

    public static String tr(String key) {
        String v;
        if (current == Lang.UK) {
            v = UK.get(key);
        } else {
            v = EN.get(key);
        }
        return v != null ? v : key;
    }

    public static String trf(String key, Object... args) {
        String pattern = tr(key);
        try {
            return String.format(pattern, args);
        } catch (Exception e) {
            return pattern;
        }
    }
}
