package org.example.fx.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.AutoComplete;
import org.example.ParameterHints;
import org.example.StudyCod;
import org.example.User;
import org.example.services.CoursePlan;
import org.example.services.GradeManager;
import org.example.services.TaskManager;
import org.example.services.ai.AiRequest;
import org.example.services.database.UsTaskDB;
import org.example.services.database.UserDB;
import org.example.services.repo.UsTaskI;
import org.example.services.repo.UserI;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TasksSceneController {

    @FXML private Button launchcode;
    @FXML private Button checkTask;
    @FXML private ListView<String> taskListView;
    @FXML private TextArea taskDescription;
    @FXML private CodeArea codeEditor;
    @FXML private TextArea consoleOutput;
    @FXML private Button generateTask;
    @FXML private Button lessonRead;
    @FXML private TextArea lessonContent;

    private Stage primaryStage;
    private String currentLesson = null;
    private String currentTopic = null;
    private String currentTaskText = null;

    User user = User.user();

    @Autowired
    private UserI userI;

    @Autowired
    private UsTaskI usTaskI;

    @Autowired
    private GradeManager gradeManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private CoursePlan coursePlan;

    private static final String KEYWORD_PATTERN = "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|" +
            "continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|" +
            "int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|" +
            "switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void populateListView(int userId) {
        if (User.user().isAuthorized()) {
            String lang = userI.findById(User.user().getId()).map(UserDB::getLang).orElse("Java");
            List<String> data = taskManager.getTaskByUserId(userId, lang);
            ObservableList<String> observableData = FXCollections.observableArrayList(data);
            taskListView.setItems(observableData);
        }
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        int lastMatchEnd = 0;
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            "default";

            spansBuilder.add(Collections.singleton("default"), matcher.start() - lastMatchEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastMatchEnd = matcher.end();
        }
        spansBuilder.add(Collections.singleton("default"), text.length() - lastMatchEnd);

        return spansBuilder.create();
    }

    @FXML
    public void initialize() {
        if (User.user().isAuthorized()) {
            codeEditor.getStylesheets().add(getClass().getResource("/styles/syntax-highlighting.css").toExternalForm());
            AutoComplete.setupAutoCompletion(codeEditor);
            ParameterHints.attach(codeEditor);

            // Ensure lesson and task text are black on white regardless of external CSS
            taskDescription.setStyle("-fx-text-fill: black; -fx-control-inner-background: white;");
            lessonContent.setStyle("-fx-text-fill: black; -fx-control-inner-background: white;");
            consoleOutput.setStyle("-fx-text-fill: black; -fx-control-inner-background: white;");

            populateListView(Math.toIntExact(User.user().getId()));
            launchcode.setDisable(true);
            checkTask.setDisable(true);
            if (codeEditor != null) {
                codeEditor.getStyleClass().add("code-area");
                codeEditor.getStyleClass().add("code-editor");
                codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
                codeEditor.textProperty().addListener((obs, oldText, newText) -> {
                    codeEditor.setStyleSpans(0, computeHighlighting(newText));
                });
            }
        }
    }

    @FXML
    public void goBack() {
        if (primaryStage != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeScene.fxml"));
                loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
                Parent root = loader.load();
                HomeSceneController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);

                primaryStage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("primaryStage is null");
        }
    }

    @FXML
    private void generateTask(ActionEvent event) {
        Optional<UserDB> opt = userI.findById(User.user().getId());
        if (opt.isEmpty()) return;
        UserDB userDb = opt.get();

        // Получаем или создаем запись UsTaskDB
        UsTaskDB usTask = usTaskI.getByUserId(userDb.getId());
        if (usTask == null) {
            usTask = new UsTaskDB();
            usTask.setUserId(Math.toIntExact(userDb.getId()));
            usTask.setNum(1);
            usTask.setControlNum(1);
            usTaskI.save(usTask);
        }

        final UsTaskDB finalUsTask = usTask;

        new Thread(() -> {
            try {
                String lang = userDb.getLang() != null ? userDb.getLang() : "Java";
                int nextLessonIndex = finalUsTask.getNum();
                int nextCheckIndex = finalUsTask.getControlNum();
                CoursePlan.PlanResult plan = coursePlan.nextFor(lang, nextLessonIndex, nextCheckIndex, userDb.getDifus());

                currentTopic = plan.topic;
                currentLesson = CoursePlan.lessonText(lang, currentTopic);
                currentTaskText = plan.task;

                if (!plan.knowledgeCheck && currentTopic != null) {
                    String topics = userDb.getTopics() == null ? "" : userDb.getTopics();
                    userDb.setTopics(topics + currentTopic + ", ");
                    userI.save(userDb);
                }

                loadLesson(currentLesson, plan.knowledgeCheck ? ("Контроль знань №" + nextCheckIndex) : currentTopic);

                String listItemTitle = plan.knowledgeCheck ? ("Контроль знань №" + nextCheckIndex) : ("Завдання №" + nextLessonIndex);

                Platform.runLater(() -> {
                    taskListView.getItems().add(listItemTitle);
                    taskDescription.setText("Завдання:\n" + plan.task);
                    if(lang.equals("Python"))
                        codeEditor.replaceText(CoursePlan.pythonTemplate());
                    else
                        codeEditor.replaceText(CoursePlan.javaTemplate());
                    taskManager.saveTaskToDB(plan.task, lang);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        generateTask.setDisable(true);
        launchcode.setDisable(false);
        checkTask.setDisable(false);
    }

    @FXML
    private void loadLesson(String currentLesson, String currentTopic) {
        UsTaskDB usTask = usTaskI.getByUserId(user.getId());
        if (usTask == null) {
            // Создаем запись если не существует
            usTask = new UsTaskDB();
            usTask.setUserId(Math.toIntExact(user.getId()));
            usTask.setNum(1);
            usTask.setControlNum(1);
            usTaskI.save(usTask);
        }

        // Show lesson and block editing until "I've read" is pressed
        taskDescription.setVisible(false);
        codeEditor.setVisible(false);
        consoleOutput.setVisible(false);
        lessonContent.setVisible(true);
        lessonRead.setVisible(true);
        lessonContent.setEditable(false);
        launchcode.setDisable(true);
        checkTask.setDisable(true);

        if ((usTask.getNum() / usTask.getControlNum()) % 26 != 1) {
            lessonContent.setText(currentTopic + "\n" + currentLesson);
        } else {
            lessonContent.setText("Контроль знань №" + usTask.getControlNum() + "\n" + currentLesson);
        }
    }

    @FXML
    private void markLessonAsRead() {
        lessonContent.setVisible(false);
        lessonRead.setVisible(false);

        taskDescription.setVisible(true);
        codeEditor.setVisible(true);
        consoleOutput.setVisible(true);

        launchcode.setDisable(false);
        checkTask.setDisable(false);
    }

    private String readStream(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        }
    }

    private String sanitizeTemplate(String input) {
        if (input == null) return "";
        String noFences = input
                .replaceAll("```[a-zA-Z]*\\s*", "")
                .replace("```", "");
        StringBuilder sb = new StringBuilder();
        for (String line : noFences.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) continue;
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    @FXML
    private void runCode(ActionEvent event) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("studycod");
        File javaFile = tempDir.resolve("Main.java").toFile();
        javaFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(javaFile))) {
            writer.write(codeEditor.getText());
        }

        Process compileProcess = new ProcessBuilder("javac", javaFile.getAbsolutePath())
                .directory(tempDir.toFile())
                .start();
        compileProcess.waitFor();

        if (compileProcess.exitValue() == 0) {
            String className = "Main";
            Process runProcess = new ProcessBuilder("java", "-cp", tempDir.toString(), className)
                    .directory(tempDir.toFile())
                    .start();

            String output = readStream(runProcess.getInputStream());
            String errors = readStream(runProcess.getErrorStream());

            consoleOutput.setText(output + (errors.isEmpty() ? "" : "\nErrors:\n" + errors));
        } else {
            String errors = readStream(compileProcess.getErrorStream());
            consoleOutput.setText("Compilation failed:\n" + errors);
        }
    }

    @FXML
    private void checkCode(ActionEvent event) {
        UsTaskDB usTask = usTaskI.getByUserId(user.getId());
        if (usTask == null) {
            // Создаем запись если не существует
            usTask = new UsTaskDB();
            usTask.setUserId(Math.toIntExact(user.getId()));
            usTask.setNum(1);
            usTask.setControlNum(1);
            usTaskI.save(usTask);
        }

        generateTask.setDisable(false);
        launchcode.setDisable(true);
        checkTask.setDisable(true);

        String codeText = codeEditor.getText();
        String taskText = currentTaskText;

        int grade1 = StudyCod.transformTexttoJSON(gradeManager.GradeforI(codeText, taskText).trim());
        int grade2 = StudyCod.transformTexttoJSON(gradeManager.GradeforII(codeText, taskText).trim());
        int grade3 = StudyCod.transformTexttoJSON(gradeManager.GradeforIII(codeText, taskText).trim());

        int grade = grade1 + grade2 + grade3;

        String task = "Завдання №" + usTask.getNum();

        String comment = StudyCod.Comment(codeText, taskText, grade1, grade2, grade3, grade);
        String result = "Оцінка: " + grade + "\n" + "Коментар: " + comment;
        consoleOutput.setText(result);

        String lang = userI.findById(user.getId()).map(UserDB::getLang).orElse("Java");
        taskManager.updateTask(taskText, codeText, comment, lang);

        Optional<UserDB> us = userI.findById(user.getId());
        if (us.isPresent()) {
            UserDB udb = us.get();

            if (grade >= 8) {
                udb.setDifus(udb.getDifus() + 0.03);
            } else if (grade == 7) {
                udb.setDifus(udb.getDifus() + 0.01);
            } else if (grade <= 4 && grade > 0) {
                udb.setDifus(udb.getDifus() - 0.02);
            } else if (grade == 0) {
                udb.setDifus(udb.getDifus() - 0.04);
            }

            if (udb.getDifus() < 0) udb.setDifus(0);
            if (udb.getDifus() > 1) udb.setDifus(1);
            userI.save(udb); // сохраняем изменения
            gradeManager.saveGradeToDB(task, String.valueOf(grade), comment);
        }

        usTask.setNum(usTask.getNum() + 1);
        usTaskI.save(usTask);
    }
}