package org.example.services;

import org.example.services.ai.AiRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursePlan {

    private static final List<String> JAVA_TOPICS = List.of(
            "Вступ до Java та JVM",
            "Типи даних і змінні",
            "Оператори та вирази",
            "Керування потоком: if/else, switch",
            "Цикли: for, while, do-while",
            "Масиви та робота з ними",
            "Методи, параметри, перевантаження",
            "Рекурсія",
            "Класи та об'єкти",
            "Інкапсуляція, гетери/сетери",
            "Статичні члени і ініціалізація",
            "Наслідування",
            "Поліморфізм і пізнє зв'язування",
            "Абстрактні класи та інтерфейси",
            "Внутрішні та анонімні класи",
            "Перелічення (enum)",
            "Узагальнення (Generics) – основи",
            "Узагальнення – обмеження та PECS",
            "Колекції: List, Set, Map",
            "Колекції: ітератори та порівняння",
            "Порівняння об'єктів: equals, hashCode, Comparable, Comparator",
            "Обробка винятків – основи",
            "Створення власних винятків",
            "Пакети та модульна система (Java 9+)",
            "Рядки: String, StringBuilder, StringBuffer",
            "Дата і час: java.time",
            "Ввід/вивід: java.io",
            "NIO.2: Path, Files, Channels",
            "Серіалізація",
            "Рефлексія та анотації",
            "Лямбда-вирази",
            "Функціональні інтерфейси",
            "Stream API – основи",
            "Stream API – колектори та паралельні потоки",
            "Конкурентність: Thread, Runnable",
            "Executors, Future, CompletableFuture",
            "Синхронізація: synchronized, locks, atomics",
            "Пул потоків та планування задач",
            "Мережеве програмування: сокети, HTTP-клієнт",
            "Робота з БД: JDBC",
            "ORM основи: JPA/Hibernate",
            "Логування: SLF4J, Logback",
            "Тестування: JUnit 5, Mockito",
            "Інструменти збирання: Maven/Gradle",
            "Профілювання і налагодження",
            "Управління пам’яттю та GC",
            "Патерни проєктування – огляд",
            "Патерни: Singleton, Factory, Strategy, Observer",
            "Безпека: основи криптографії в Java",
            "Робота з JSON/XML"
    );

    private static final List<String> PYTHON_TOPICS = List.of(
            "Вступ до Python та інтерпретатора",
            "Типи даних і змінні",
            "Оператори та вирази",
            "Керування потоком: if/elif/else",
            "Цикли for/while та range",
            "Рядки та форматування",
            "Списки (list) – основи",
            "Кортежі (tuple)",
            "Множини (set)",
            "Словники (dict)",
            "Включення: list/set/dict comprehensions",
            "Функції та параметри, *args/**kwargs",
            "Замикання та функції вищого порядку",
            "Генератори та ітератори",
            "Модулі та пакети",
            "Огляд стандартної бібліотеки",
            "Файли та контекстні менеджери",
            "Обробка винятків",
            "ООП: класи та об'єкти",
            "Наслідування і поліморфізм",
            "Декоратори",
            "Типізація: typing, type hints",
            "Колекції з модуля collections",
            "Дата і час: datetime",
            "Регулярні вирази: re",
            "HTTP-клієнти: requests/httpx",
            "Парсинг HTML: BeautifulSoup",
            "JSON, CSV, XML",
            "Асинхронність: asyncio (async/await)",
            "Потоки та процеси: threading, multiprocessing",
            "Віртуальні середовища: venv/poetry/pip",
            "Логування",
            "Тестування: unittest/pytest",
            "Профілювання і налагодження",
            "Робота з БД: sqlite3, SQLAlchemy",
            "Мережеве програмування: сокети",
            "Шляхи та файли: pathlib",
            "NumPy – основи",
            "Pandas – основи",
            "Візуалізація: Matplotlib/Seaborn",
            "ML (огляд): scikit-learn – базові поняття",
            "Веб: Flask – основи",
            "Веб: FastAPI – основи",
            "Веб: Django – основи",
            "Черги та задачі: Celery, брокери (огляд)",
            "Документація і стиль коду: PEP 8, docstring",
            "Пакетування і дистрибуція: setuptools",
            "Безпека: секрети, .env",
            "Хмара/CLI (огляд): boto3",
            "Найкращі практики та патерни"
    );

    public static class PlanResult {
        public final boolean knowledgeCheck;
        public final String title;
        public final String task;
        public final String template;
        public final String topic;

        public PlanResult(boolean knowledgeCheck, String title, String task, String template, String topic) {
            this.knowledgeCheck = knowledgeCheck;
            this.title = title;
            this.task = task;
            this.template = template;
            this.topic = topic;
        }
    }

    public PlanResult nextFor(String lang, int lessonIndex, int checkIndex, double difus) {
        boolean isCheck = (lessonIndex % 5) == 0;
        if ("Python".equalsIgnoreCase(lang)) {
            if (isCheck) {
                String title = "Контроль знань №" + (checkIndex);
                String task = "Виконайте контроль знань з Python за останні 5 уроків. Напишіть 3 короткі функції, що демонструють ключові ідеї тем.";
                String template = pythonTemplate();
                return new PlanResult(true, title, task, template, "Контроль знань");
            } else {
                String topic = PYTHON_TOPICS.get((lessonIndex - 1) % PYTHON_TOPICS.size());
                String title = "Урок " + lessonIndex + ": " + topic;
                String task = AiRequest.requestToAI("Ти вчитель програмування мови " + lang + ", згенеруй для учня завдання за темою: " + topic + ", зважайте його КЗЗ (0-легкі завдання, 1-важкі завдання, і завдання по темі)" + difus);
                String template = pythonTemplate();
                return new PlanResult(false, title, task, template, topic);
            }
        } else {
            if (isCheck) {
                String title = "Контроль знань №" + (checkIndex);
                String task = "Виконайте контроль знань з Java за останні 5 уроків. Напишіть 2 класи і тести (main), що демонструють ключові ідеї тем.";
                String template = javaTemplate();
                return new PlanResult(true, title, task, template, "Контроль знань");
            } else {
                String topic = JAVA_TOPICS.get((lessonIndex - 1) % JAVA_TOPICS.size());
                String title = "Урок " + lessonIndex + ": " + topic;
                String task = AiRequest.requestToAI("Ти вчитель програмування мови " + lang + ", згенеруй для учня завдання за темою: " + topic + ", зважайте його КЗЗ (0-легкі завдання, 1-важкі завдання, і завдання по темі)" + difus);
                String template = javaTemplate();
                return new PlanResult(false, title, task, template, topic);
            }
        }
    }

    public static String lessonText(String lang, String topic){
        return AiRequest.requestToAI("Ти вчитель програмування мови " + lang + ", розроби текст для уроку на самоопрацювання по темі: " + topic + ", не використовуй ніякого форматування лише текст");
    }

    public static String javaTemplate() {
        return """
                public class Main {
                    public static void main(String[] args) {
                        // Ваша реалізація тут
                    }
                }
                """;
    }

    public static String pythonTemplate() {
        return """
                def main():
                    # Ваша реалізація тут
                    pass

                if __name__ == "__main__":
                    main()
                """;
    }
}
