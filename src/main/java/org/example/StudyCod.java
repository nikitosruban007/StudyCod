package org.example;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.*;

public class StudyCod extends Application {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    private static final String DB_URL = "jdbc:mysql://109.94.209.168:3306/man?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";

    private static final String DB_USER = "nikitosruban007";
    private static final String DB_PASSWORD = "Nikitos121109";

    public static String currentUsername;
    public static int currentUserId;
    public static boolean isAuthorized;
    public static double difus;

    public static void main(String[] args) {
        launch(args);
    }

    public boolean isSessionValid() {
        String[] userData = UserSession.loadUserData();

        if (userData != null && userData.length == 3) {
            long lastLoginTime = Long.parseLong(userData[2]);
            long currentTime = System.currentTimeMillis();
            long sessionDuration = currentTime - lastLoginTime;

            return sessionDuration <= 3600000;
        }
        return false;
    }

    public static void setCurrentUsername(String currentUsername) {
        StudyCod.currentUsername = currentUsername;
    }

    public static void setDifus(double difus) {
        StudyCod.difus = difus;
    }

    public static void setIsAuthorized(boolean isAuthorized) {
        StudyCod.isAuthorized = isAuthorized;
    }

    public static void setId(int currentUserId) {
        StudyCod.currentUserId = currentUserId;
    }

    public static int getId() {
        return currentUserId;
    }

    public static double getDifus() {
        return difus;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static boolean isIsAuthorized() {
        return isAuthorized;
    }

    @Override
    public void start(Stage primaryStage) {
        String[] userData = UserSession.loadUserData();

        if (userData != null && userData.length == 3 && isSessionValid()) {
            setCurrentUsername(userData[0]);
            setId(Integer.parseInt(userData[1]));
            setIsAuthorized(true);
            setDifus(getDifus(getId()));
        }


        primaryStage.setTitle("StudyCod");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));

        try {
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/HomeScene.fxml"));
            Parent homeRoot = homeLoader.load();
            HomeSceneController homeController = homeLoader.getController();
            homeController.setPrimaryStage(primaryStage);

            FXMLLoader gradesLoader = new FXMLLoader(getClass().getResource("/GradesScene.fxml"));
            Parent gradesRoot = gradesLoader.load();
            GradesSceneController gradesController = gradesLoader.getController();
            gradesController.setPrimaryStage(primaryStage);

            FXMLLoader tasksLoader = new FXMLLoader(getClass().getResource("/TasksScene.fxml"));
            Parent tasksRoot = tasksLoader.load();
            TasksSceneController tasksController = tasksLoader.getController();
            tasksController.setPrimaryStage(primaryStage);

            Scene homeScene = new Scene(homeRoot);
            primaryStage.setScene(homeScene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTaskToDB(String tasktext, String template) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "INSERT INTO tasks (user_id, description, template, task_name) VALUES (?, ?, ?, ?)");) {
                stmt.setInt(1, getId());
                stmt.setString(2, tasktext);
                stmt.setString(3, template);
                stmt.setString(4, "Завдання №" + getTaskNum());
                stmt.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTask(String tasktext, String finishCode, String comments) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "UPDATE tasks SET finish_code = ?, comments = ?, completed = ? WHERE user_id = ? AND task_id = ?");) {
                stmt.setString(1, finishCode);
                stmt.setString(2, comments);
                stmt.setInt(3, 1);
                stmt.setInt(4, getId());
                stmt.setInt(5, fetchTaskId(tasktext));
                stmt.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void registerUser(String username, String password, Label correctly, ImageView imageView) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Проверка на уникальность username
                try (PreparedStatement stmtCheck = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                    stmtCheck.setString(1, username);
                    try (ResultSet rs = stmtCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            correctly.setVisible(true);
                            PauseTransition pause = new PauseTransition(Duration.seconds(5));

                            pause.setOnFinished(e -> correctly.setVisible(false));

                            pause.play();
                            return;
                        }
                    }
                }

                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password, difus) VALUES (?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setInt(3, 0);
                    stmt.executeUpdate();
                }

                authenticateUser(username, password);
                Stage currentStage = (Stage) imageView.getScene().getWindow();
                currentStage.close();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public static void logout() {
        setCurrentUsername(null);
        UserSession.eraseData();
        setIsAuthorized(false);
        setId(0);
    }

        public static boolean authenticateUser(String username, String password) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = connection.prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?")) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        setId(rs.getInt("id"));
                        System.out.println(getId());
                        setIsAuthorized(true);
                        System.out.println(isIsAuthorized());
                        setCurrentUsername(username);
                        System.out.println(getCurrentUsername());
                        UserSession.saveUserData(username, String.valueOf(getId()));
                        setDifus(getDifus(getId()));
                        System.out.println(getDifus());
                        return true;
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL Driver not found: " + e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

    public static String getTopicsFromDB(int userId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement("SELECT topics FROM users WHERE id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("topics");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> void exportTableViewToPDF(TableView<T> tableView, Stage stage, String filePath) {
        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            PdfFont arialFont = PdfFontFactory.createFont(StudyCod.class.getResource("/ArialMT.ttf").toString(), PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfFont arialboldFont = PdfFontFactory.createFont(StudyCod.class.getResource("/Arial-BoldMT.ttf").toString(), PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            pdfDocument.addNewPage();


            Paragraph title = new Paragraph("Журнал оцінювання")
                    .setFontSize(18)
                    .setFont(arialboldFont)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(102, 205, 170))
                    .setMarginBottom(20);
            document.add(title);

            String nickname = "Юзернейм: " + getCurrentUsername();
            String id = "ID: " + getId();
            String kzs = "КСЗ: " + getDifus();
            String[] labels = {nickname, id, kzs};
            for (String label : labels) {
                Paragraph labelParagraph = new Paragraph(label)
                        .setFontSize(12)
                        .setFont(arialFont)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontColor(ColorConstants.BLACK)
                        .setMarginBottom(5);
                document.add(labelParagraph);
            }

            ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
            ObservableList<T> items = tableView.getItems();

            Table pdfTable = new Table(UnitValue.createPercentArray(columns.size()));
            pdfTable.setWidth(UnitValue.createPercentValue(100));

            for (TableColumn<T, ?> column : columns) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(column.getText())
                                .setFont(arialFont)
                                .setFontSize(12)
                                .setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(new DeviceRgb(68, 68, 68))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5);
                pdfTable.addHeaderCell(headerCell);
            }

            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                DeviceRgb rowColor = (i % 2 == 0) ? new DeviceRgb(51, 51, 51) : new DeviceRgb(34, 34, 34); // Чередование строк
                for (TableColumn<T, ?> column : columns) {
                    Object cellData = column.getCellObservableValue(item).getValue();
                    Cell cell = new Cell()
                            .add(new Paragraph(cellData != null ? cellData.toString() : "")
                                    .setFont(arialFont)
                                    .setFontSize(10)
                                    .setFontColor(new DeviceRgb(238, 238, 238)))
                            .setBackgroundColor(rowColor)
                            .setPadding(5)
                            .setTextAlignment(TextAlignment.LEFT);
                    pdfTable.addCell(cell);
                }

                if (i % 30 == 0 && i != 0) {
                    pdfDocument.addNewPage();
                }
            }

            document.add(pdfTable);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static String generateUniqueTask(double difus, String topics) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = String.format(
                    "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"УВАГА!!! КОЛИ ТИ ПРОСИШ ЩОСЬ ВИВЕСТИ НАПРИКЛАД ПРИВІТ СВІТ, ТО ПРОСИ НА АНГЛІЙСЬКІЙ МОВІ ТОБТО НАПРИКЛАД HELLO WORLD!!!!!!! МОВА ПРОГРАМУВАННЯ - JAVA!!!! БЕЗ ФОРМАТУВАННЯ!!!!! НАВІТЬ КОД ЯК ЗВИЧАЙНИЙ ТЕКСТ ПРОСТО З ТАБАМИ!!!!!  Згенеруй практичне завдання для учня на мові програмування Java, просто текст завдання, без форматування без нічого тільки текст і коротко, і ще орієнтуйся на КСЗ (коефіцієт складності завдань), де 0 - це взагалі початківець (прості задачі), тобто користувач тільки починає знайомство з джавою, 1 - це бог програмування (найскладніші завдання), КСЗ: %.1f, а ще тримай пройдені теми, щоби ти не повторювався: %s , та орієнтуйся на них, не давай того чого користувач на зараз не зрозуміє\", \"max_tokens\": 1000}",
                    difus,
                    topics
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return processResponse(response);
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }

    public static String generateControlTask(double difus, String topics) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String topicsString = String.join(", ", topics);

            String jsonBody = String.format(
                    "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", " +
                            "\"prompt\": \"УВАГА!!! МОВА ПРОГРАМУВАННЯ - JAVA!!!! БЕЗ ФОРМАТУВАННЯ!!!!! КОЛИ ТИ ПРОСИШ ЩОСЬ ВИВЕСТИ НАПРИКЛАД ПРИВІТ СВІТ, ТО ПРОСИ НА АНГЛІЙСЬКІЙ МОВІ ТОБТО НАПРИКЛАД HELLO WORLD!!!!!!! НАВІТЬ КОД ЯК ЗВИЧАЙНИЙ ТЕКСТ ПРОСТО З ТАБАМИ!!!!!  Згенеруй завдання контролю знань для учня на мові програмування Java за темами: %s, " +
                            "просто текст завдання, без форматування без нічого тільки текст і коротко, і ще орієнтуйся на КСЗ " +
                            "(коефіцієнт складності завдань), де 0 - це взагалі початківець, 1 - це бог програмування, КСЗ: %.1f\", " +
                            "\"max_tokens\": 10000}",
                    topics, difus
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return processResponse(response);
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }


    public static void saveTopicToDB(String topic) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "UPDATE users SET topics = ? WHERE id = ?;")) {
                stmt.setString(1, getTopicsFromDB(getId()) + topic + ", ");
                stmt.setInt(2, getId());
                stmt.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static String generateTopic(String task) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = String.format(
                    "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"УВАГА!!! МОВА ПРОГРАМУВАННЯ - JAVA!!!! БЕЗ ФОРМАТУВАННЯ!!!!! НАВІТЬ КОД ЯК ЗВИЧАЙНИЙ ТЕКСТ ПРОСТО З ТАБАМИ!!!!! Згенеруй для завдання назву теми, яку він вивчає за завданням, тільки назва, без жирних виділень та т.д., просто назва, ось завдання: %s\", \"max_tokens\": 100}",
                    escapeJson(task)
            );

            System.out.println("JSON Body: " + jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());

            System.out.println("Response Body: " + response.body());

            return processResponse(response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Помилка: " + e.getMessage();
        }
    }

    public static String generateLesson(String task, String topic) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = String.format(
                    "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", " +
                            "\"prompt\": \"УВАГА!!! ВСІ ВІДСТУПИ У ВІДПОВІДІ ЛИШЕ ПРОБІЛАМИ, ЖОДНОГО ФОРМАТУВАННЯ!!!! " +
                            "УСЕ ПОВИННО БУТИ ОДНИМ МОНОЛІТНИМ ТЕКСТОМ, НАВІТЬ КОД ПИШИ ЯК ПРОСТИЙ ТЕКСТ!!!!! " +
                            "БЕЗ КРАПКОК, БЕЗ МАРКЕРІВ, БЕЗ СПИСКІВ, БЕЗ РОЗРИВІВ РЯДКІВ!!!!! " +
                            "НАВІТЬ ЯКЩО ЦЕ КОД, ПРОСТО ПИШИ ЙОГО ЯК ЗВИЧАЙНИЙ ТЕКСТ, ЗАЛИШАЮЧИ ВІДСТУПИ ПРОБІЛАМИ " +
                            "НА ПОЧАТКУ РЯДКІВ ДЛЯ ВІЗУАЛЬНОЇ ІЄРАРХІЇ!!!!! " +
                            "Створи лекцію за темою: %s, яка пояснює все доступно, без формального сухого тексту, " +
                            "а як цікава розповідь, сповнена живих реальних прикладів та пояснень, " +
                            "як застосовувати знання на практиці. " +
                            "ПОКАЗУЙ ПРАКТИЧНЕ ЗАСТОСУВАННЯ ЧЕРЕЗ КОНКРЕТНІ ПРИКЛАДИ КОДУ!!!!! " +
                            "НІКОЛИ НЕ ВИКОРИСТОВУЙ HTML-ТЕГИ!!!!! НЕ ФОРМАТУЙ НІЧОГО!!!!! " +
                            "ПИШИ ПРОСТО МОНОЛІТНИЙ ТЕКСТ!!!! орієнтуйся також за завданням: %s\", " +
                            "\"max_tokens\": 10000}",
                    escapeJson(topic),
                    escapeJson(task)
            );


            System.out.println("JSON Body: " + jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());

            System.out.println("Response Body: " + response.body());
            System.out.println(processResponse(response));
            return processResponse(response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Помилка: " + e.getMessage();
        }
    }


    public static String motivateAdvice() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"Згенеруй для учня який вчить Java мотиваційну пораду, дуже коротко, і текст без форматування та ін.!\", \"max_tokens\": 100}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return processResponse(response);
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }

    public static String generateCodeTemplate(String task) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format(
                    "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"УВАГА!!! БЕЗ ФОРМАТУВАННЯ, НІЯКИХ ДОДАТКОВИХ КОМПОНЕНТІВ ТА Пояснень! Згенеруй заготовку для завдання, яка містить тільки клас Main, метод main та необхідні імпорти. Ніяких інших класів, методів чи імпортів – тільки те, що потрібно для запуску програми. Текст повинен бути виведений як звичайний текст (без форматування, markdown, ``` тощо), точно так, як я пишу. ось завдання: %s\", \"max_tokens\": 10000}",
                    escapeJson(task)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return processResponse(response);
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }

    private static String processResponse(HttpResponse<String> response) {
        try {
            String responseBody = response.body();
            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(responseBody);

                if (jsonObject.has("choices")) {
                    return jsonObject.getJSONArray("choices").getJSONObject(0).getString("text").trim();
                } else {
                    return "Помилка: Поле 'choices' не знайдено в відповіді.";
                }
            } else {
                return "Статус: " + response.statusCode() + ", Тіло: " + responseBody;
            }
        } catch (Exception e) {
            return "Помилка обробки відповіді: " + e.getMessage();
        }
    }

    private static String escapeJson(String input) {
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }


    public String GradeforI(String code, String ttext) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"Оціни код на його роботу, тобто він взагалі працює та завдання виконується? Тільки число, макс. бал - 5., мін 0 балів. Код: %s, а також тримай завдання за яким треба оцінити код: %s\", \"max_tokens\": 100}",
                    escapeJson(code), escapeJson(ttext));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println(response.body());
                return response.body();
            } else {
                return "Статус: " + response.statusCode() + ", Тіло: " + response.body();
            }
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }



    public String GradeforII(String code, String ttext) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"Оціни код за його оптимізацією (якщо код не працює то став 0 балів), тобто якщо його не можна оптимізувати (макс. оптимізація) - 4 бали, повністю не оптимізований, дуже погано працює - 0 балів. Тільки число. Код: %s, а також тримай завдання за яким треба оцінити код: %s\", \"max_tokens\": 100}",
                    escapeJson(code), escapeJson(ttext));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Статус: " + response.statusCode() + ", Тіло: " + response.body();
            }
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }

    public String GradeforIII(String code, String ttext) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"Оціни код за його антиплагіатністю (якщо код не працює, то став 0 балів), тобто якщо його плагіатність від 0%% до 35%% - 3 бали, від 35%% до 70%% - 2 бали, від 70%% до 87%% - 1 бал, від 87%% до 100%% - 0 балів. КОРОЧШЕ - НЕСПИСАНО ТО ТИ ВІДПРАВЛЯЄШ 3б, ЯКЩО СПИСАНО ТО 0б. Тільки число. Код: %s, а також тримай завдання за яким треба оцінити код: %s\", \"max_tokens\": 100}",
                    escapeJson(code), escapeJson(ttext));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Статус: " + response.statusCode() + ", Тіло: " + response.body();
            }
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }

    public static int transformTexttoJSON(String response) {
        try {
            System.out.println(response);
            JSONObject jsonResponse = new JSONObject(response);

            String text = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");

            return Integer.parseInt(text.trim());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }



    public String Comment(String code, String ttext, int grade1, int grade2, int grade3, int grade4) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format(
                    "{\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\", \"prompt\": \"Проаналізуй код за завданням, та оцінками: Оцінка за роботу кода: %d, (макс. 5) Оцінка за оптимізацію: %d, (макс. 4) Оцінка за плагіат: %d, (макс. 3). Загальний бал: %d, Без форматування та іншого, тупо текст і коротко! Код: %s, а також тримай завдання за яким треба оцінити код: %s\", \"max_tokens\": 100}",
                    grade1, grade2, grade3, grade4, escapeJson(code), escapeJson(ttext)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(responseBody);

                if (jsonObject.has("choices")) {
                    String text = jsonObject.getJSONArray("choices").getJSONObject(0).getString("text");
                    return text.trim();
                } else {
                    return "Помилка: Поле 'choices' не знайдено в відповіді.";
                }
            } else {
                return "Статус: " + response.statusCode() + ", Тіло: " + responseBody;
            }
        } catch (Exception e) {
            return "Помилка: " + e.getMessage();
        }
    }

    public static void saveGradeToDB(String task, String grade, String comments) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "INSERT INTO grades (task_name, user_id, grade, comments) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, task);
                stmt.setInt(2, getId());
                stmt.setString(3, grade);
                stmt.setString(4, comments);
                stmt.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int fetchTaskId(String task) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement("SELECT task_id FROM tasks WHERE description = ?")) {
                stmt.setString(1, task);
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? rs.getInt("task_id") : 1;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void saveDifuseToDB(double difus) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET difus = ? WHERE id = ?")) {

            preparedStatement.setDouble(1, difus);
            preparedStatement.setInt(2, getId());

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated == 0) {
                System.out.println("Ошибка: Пользователь с ID " + getId() + " не найден.");
            } else {
                System.out.println("Данные успешно обновлены.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getDifus(int userId) {
        double difusValue = 0.0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement("SELECT difus FROM users WHERE id = ?")) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    difusValue = rs.getDouble("difus");
                } else {
                    System.out.println("Ошибка: Пользователь с ID " + userId + " не найден.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return difusValue;
    }

    public static ObservableList<TaskDetails> getTaskDetails(int userId) {
        String query = "SELECT task_name, grade, comments FROM grades WHERE user_id = ?";
        ObservableList<TaskDetails> taskDetailsList = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set user_id parameter
            stmt.setInt(1, userId);

            // Execute query and process the results
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String taskName = rs.getString("task_name");
                    int grade = rs.getInt("grade");
                    String comments = rs.getString("comments");

                    taskDetailsList.add(new TaskDetails(taskName, grade, comments));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskDetailsList;
    }

    public static List<String> getTaskByUserId(int userId) {
        List<String> data = new ArrayList<>();
        String query = "SELECT task_name FROM tasks WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                data.add(resultSet.getString("task_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static int getTaskNum() {
        String query = "SELECT num FROM ustask WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("num");
            } else {
                System.out.println("No task found for user ID: " + getId());
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }


    public static int getControlNum() {
        String query = "SELECT controlnum FROM ustask WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("controlnum");
            } else {
                System.out.println("No task found for user ID: " + getId());
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }


}
