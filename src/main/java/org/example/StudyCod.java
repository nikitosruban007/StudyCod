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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Objects;

@SpringBootApplication(scanBasePackages = "org.example")
public class StudyCod {

    @Getter
    private static ConfigurableApplicationContext springContext;

    private static final List<String> STUDENT_TIPS = List.of(
            "Навчайся не заради оцінки, а заради знань.",
            "Кожна помилка — це крок до майстерності.",
            "Не відкладайте важливе на завтра — почніть сьогодні.",
            "Успіх приходить до тих, хто не здається.",
            "Зосередься на процесі, а не лише на результаті.",
            "Краще зробити хоч щось, ніж ідеально нічого.",
            "Маленькі щоденні кроки ведуть до великих звершень.",
            "Не порівнюй себе з іншими — порівнюй із собою вчорашнім.",
            "Знання — це найкраща інвестиція у себе.",
            "Відпочинок — частина продуктивності, а не її ворог.",
            "Став чіткі цілі й розділяй їх на маленькі завдання.",
            "Найскладніше — почати. Почни зараз.",
            "Твоя дисципліна — найкращий союзник у навчанні.",
            "Не бійся питати — це шлях до розуміння.",
            "Твої сьогоднішні зусилля — завтрашня впевненість.",
            "Помилки не визначають тебе — вони навчають тебе.",
            "Постійне навчання — ключ до розвитку.",
            "Навіть коротке навчання щодня дає великі результати.",
            "Кожен день — шанс стати трохи кращим.",
            "Зосереджуйся на якості, а не на кількості.",
            "Не шукай натхнення — створюй його діями.",
            "Твої мрії варті того, щоб за них боротися.",
            "Навчання — це не тягар, а привілей.",
            "Думай як дослідник, а не як виконавець.",
            "Коли важко — значить, ти ростеш.",
            "Великі результати починаються з маленьких зусиль.",
            "Кожна сторінка, яку ти читаєш, формує твоє майбутнє.",
            "Не здавайся, навіть якщо здається, що прогресу немає.",
            "Будь послідовним — це найпотужніша суперсила.",
            "Навчання відкриває двері, які іншим здаються зачиненими."
    );


    public static void main(String[] args) {
        Application.launch(FxStarter.class, args);
    }

    public static <T> void exportTableViewToPDF(TableView<T> tableView, Stage stage, String filePath) {
        try {
            // Ініціалізація PDF-документа
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            DeviceRgb backgroundColor = new DeviceRgb(28, 28, 28);
            pdfDocument.addNewPage();
            PdfCanvas canvas = new PdfCanvas(pdfDocument.getLastPage());
            canvas.setFillColor(backgroundColor)
                    .rectangle(0, 0, pdfDocument.getDefaultPageSize().getWidth(), pdfDocument.getDefaultPageSize().getHeight())
                    .fill();

            PdfFont arialFont = PdfFontFactory.createFont(
                    StudyCod.class.getResource("/ArialMT.ttf").toString(),
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            PdfFont arialBoldFont = PdfFontFactory.createFont(
                    StudyCod.class.getResource("/Arial-BoldMT.ttf").toString(),
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            Paragraph title = new Paragraph("Журнал оцінювання")
                    .setFontSize(18)
                    .setFont(arialBoldFont)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(102, 205, 170))
                    .setMarginBottom(20);
            document.add(title);

            User session = User.user();
            String nickname = "Юзернейм: " + (session.getUsername() != null ? session.getUsername() : "");
            String id = "ID: " + (session.getId() != null ? session.getId() : "");
            String kzs = "КСЗ: " + session.getDifus();
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

                if (i % 30 == 0 && i != 0) {
                    pdfDocument.addNewPage();
                    canvas = new PdfCanvas(pdfDocument.getLastPage());
                    canvas.setFillColor(backgroundColor)
                            .rectangle(0, 0, pdfDocument.getDefaultPageSize().getWidth(), pdfDocument.getDefaultPageSize().getHeight())
                            .fill();
                }
            }

            document.add(pdfTable);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String motivateAdvice() {
        int index = (int) (Math.random() * STUDENT_TIPS.size());
        return STUDENT_TIPS.get(index);
    }

    public static String Comment(String code, String ttext, int grade) {
        return AiRequest.requestToAI(
                "Прокоментуй код коротко та зрозуміло. Вкажи, що добре, а що можна покращити. " +
                        "Не використовуй форматування, списки або HTML.\n\n" +
                        "Код:\n" + code + "\n\n" +
                        "Текст завдання:\n" + ttext + "\n\n" +
                        "Оцінка: " + grade
        );
    }



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
