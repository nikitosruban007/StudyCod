package org.example;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import org.example.fx.controllers.GradesSceneController;
import org.example.fx.controllers.HomeSceneController;
import org.example.fx.controllers.TasksSceneController;
import org.example.services.ai.AiRequest;
import org.example.services.database.UserDB;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@SpringBootApplication(scanBasePackages = "org.example")
public class StudyCod {

    // ------------------ API ------------------
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "sk-or-v1-..."; // ⚠️ краще в application.properties

    public static UserDB u = new UserDB();

    @Getter
    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        Application.launch(FxStarter.class, args);
    }

    // ------------------ PDF Export ------------------
    public static <T> void exportTableViewToPDF(TableView<T> tableView, Stage stage, String filePath) {
        try {
            // Ініціалізація PDF-документа
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // Фон сторінки
            DeviceRgb backgroundColor = new DeviceRgb(28, 28, 28);
            pdfDocument.addNewPage();
            PdfCanvas canvas = new PdfCanvas(pdfDocument.getLastPage());
            canvas.setFillColor(backgroundColor)
                    .rectangle(0, 0, pdfDocument.getDefaultPageSize().getWidth(), pdfDocument.getDefaultPageSize().getHeight())
                    .fill();

            // Завантаження шрифтів
            PdfFont arialFont = PdfFontFactory.createFont(
                    StudyCod.class.getResource("/ArialMT.ttf").toString(),
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            PdfFont arialBoldFont = PdfFontFactory.createFont(
                    StudyCod.class.getResource("/Arial-BoldMT.ttf").toString(),
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            // Заголовок
            Paragraph title = new Paragraph("Журнал оцінювання")
                    .setFontSize(18)
                    .setFont(arialBoldFont)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(102, 205, 170))
                    .setMarginBottom(20);
            document.add(title);

            // Інформація про користувача
            String nickname = "Юзернейм: " + u.getUsername();
            String id = "ID: " + u.getId();
            String kzs = "КСЗ: " + u.getDifus();
            String[] labels = {nickname, id, kzs};

            for (String label : labels) {
                Paragraph labelParagraph = new Paragraph(label)
                        .setFontSize(12)
                        .setFont(arialFont)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontColor(ColorConstants.WHITE)
                        .setMarginBottom(5);
                document.add(labelParagraph);
            }

            // Таблиця
            ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
            ObservableList<T> items = tableView.getItems();
            Table pdfTable = new Table(UnitValue.createPercentArray(columns.size()));
            pdfTable.setWidth(UnitValue.createPercentValue(100));

            // Заголовки таблиці
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

            // Дані таблиці
            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                DeviceRgb rowColor = (i % 2 == 0) ? new DeviceRgb(51, 51, 51) : new DeviceRgb(34, 34, 34);

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

                // Нова сторінка кожні 30 рядків
                if (i % 30 == 0 && i != 0) {
                    pdfDocument.addNewPage();
                    canvas = new PdfCanvas(pdfDocument.getLastPage());
                    canvas.setFillColor(backgroundColor)
                            .rectangle(0, 0, pdfDocument.getDefaultPageSize().getWidth(), pdfDocument.getDefaultPageSize().getHeight())
                            .fill();
                }
            }

            // Додавання таблиці до документа
            document.add(pdfTable);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------ JSON Parsing ------------------
// В класс StudyCod добавьте этот метод
    public static int transformTexttoJSON(String response) {
        try {
            // Проверяем, является ли response валидным JSON
            if (response == null || !response.trim().startsWith("{")) {
                return -1;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");

            if (choices.length() > 0) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                String content = message.getString("content").trim();
                return Integer.parseInt(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ------------------ API Calls ------------------
    public static String generateTopic(String task) {
        return AiRequest.requestToAI("Згенеруй назву для навчальної теми: " + task);
    }

    public static String generateLesson(String task, String topic) {
        return AiRequest.requestToAI("Створи урок на тему: " + topic + ". Завдання: " + task);
    }

    public static String motivateAdvice() {
        return AiRequest.requestToAI("Дай коротку мотиваційну пораду для студента.");
    }

    public static String generateCodeTemplate(String task) {
        return AiRequest.requestToAI("Згенеруй код на Java для завдання: " + task);
    }

    public static String Comment(String code, String ttext, int grade1, int grade2, int grade3, int grade4) {
        return AiRequest.requestToAI("Оціни та прокоментуй код:\n" + code +
                "\nТекст: " + ttext +
                "\nОцінки: " + grade1 + ", " + grade2 + ", " + grade3 + ", " + grade4);
    }


    // ------------------ JavaFX + Spring ------------------
    public static class FxStarter extends Application {
        @Override
        public void init() {
            springContext = new SpringApplicationBuilder(StudyCod.class).run();
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            primaryStage.setTitle("StudyCod");
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));

            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/HomeScene.fxml"));
            homeLoader.setControllerFactory(springContext::getBean);
            Parent homeRoot = homeLoader.load();
            HomeSceneController homeController = homeLoader.getController();
            homeController.setPrimaryStage(primaryStage);

            FXMLLoader gradesLoader = new FXMLLoader(getClass().getResource("/GradesScene.fxml"));
            gradesLoader.setControllerFactory(springContext::getBean);
            Parent gradesRoot = gradesLoader.load();
            GradesSceneController gradesController = gradesLoader.getController();
            gradesController.setPrimaryStage(primaryStage);

            FXMLLoader tasksLoader = new FXMLLoader(getClass().getResource("/TasksScene.fxml"));
            tasksLoader.setControllerFactory(springContext::getBean);
            Parent tasksRoot = tasksLoader.load();
            TasksSceneController tasksController = tasksLoader.getController();
            tasksController.setPrimaryStage(primaryStage);

            primaryStage.setScene(new Scene(homeRoot));
            primaryStage.show();
        }

        @Override
        public void stop() {
            springContext.close();
            Platform.exit();
        }
    }
}
