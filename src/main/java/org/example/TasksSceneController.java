package org.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String DB_URL = "jdbc:mysql://109.94.209.168:3306/man?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "nikitosruban007";
    private static final String DB_PASSWORD = "Nikitos121109";
    private static String currentTaskText = null;

    private static final String KEYWORD_PATTERN = "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b";
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

    public void populateListView(ListView<String> listView, int userId, TextArea descriptionArea, CodeArea codeArea, TextArea commentsArea) {
        List<String> data = StudyCod.getTaskByUserId(userId);
        ObservableList<String> observableData = FXCollections.observableArrayList(data);
        listView.setItems(observableData);
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastMatchEnd = 0;
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                            matcher.group("BRACE") != null ? "brace" :
                                    matcher.group("BRACKET") != null ? "bracket" :
                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                    matcher.group("STRING") != null ? "string" :
                                                            matcher.group("COMMENT") != null ? "comment" : "default";
            spansBuilder.add(Collections.singleton("default"), matcher.start() - lastMatchEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastMatchEnd = matcher.end();
        }
        spansBuilder.add(Collections.singleton("default"), text.length() - lastMatchEnd);
        return spansBuilder.create();
    }

    @FXML
    public void initialize() {
        codeEditor.getStylesheets().add(getClass().getResource("/styles/syntax-highlighting.css").toExternalForm());
        AutoComplete.setupAutoCompletion(codeEditor);
        codeEditor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                int caretPos = codeEditor.getCaretPosition();
                codeEditor.insertText(caretPos, "    ");
                event.consume();
            }
        });
        codeEditor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                int caretPosition = codeEditor.getCaretPosition();
                int currentParagraph = codeEditor.getCurrentParagraph();
                if (currentParagraph > 0) {
                    String prevText = codeEditor.getParagraph(currentParagraph - 1).getText();
                    String indent = getIndent(prevText);
                    Platform.runLater(() -> codeEditor.insertText(caretPosition, indent));
                }
            }
        });
        populateListView(taskListView, StudyCod.getId(), taskDescription, codeEditor, consoleOutput);
        launchcode.setDisable(true);
        checkTask.setDisable(true);
        lessonRead.setDisable(true);
        if (codeEditor != null) {
            codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
            codeEditor.textProperty().addListener((obs, oldText, newText) ->
                    codeEditor.setStyleSpans(0, computeHighlighting(newText)));
        }
    }

    private String getIndent(String text) {
        StringBuilder indent = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indent.append(c);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    @FXML
    public void goBack() {
        if (primaryStage != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeScene.fxml"));
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

    public String getCurrentTopic() {
        return currentTopic;
    }

    public void setCurrentTopic(String currentTopic) {
        this.currentTopic = currentTopic;
    }

    public String getCurrentLesson() {
        return currentLesson;
    }

    public void setCurrentLesson(String currentLesson) {
        this.currentLesson = currentLesson;
    }

    @FXML
    private void generateTask(ActionEvent event) {
        lessonContent.setVisible(true);
        lessonContent.setText("");
        launchcode.setDisable(true);
        checkTask.setDisable(true);
        lessonRead.setVisible(true);
        new Thread(() -> {
            String task;
            String template = "import java.util.*;\n" +
                    "\n" +
                    "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        \n" +
                    "    }\n" +
                    "}";
            if ((StudyCod.getTaskNum() / StudyCod.getControlNum()) % 26 != 1) {
                task = StudyCod.generateUniqueTask(StudyCod.getDifus(StudyCod.getId()), StudyCod.getTopicsFromDB(StudyCod.getId()));
                String topic = StudyCod.generateTopic(task);
                setCurrentTopic(topic);
                StudyCod.saveTopicToDB(topic);
                setCurrentLesson(StudyCod.generateLesson(task, currentTopic));
                currentTaskText = task;
                Platform.runLater(() -> {
                    taskListView.getItems().add("Завдання №" + StudyCod.getTaskNum());
                    taskDescription.setText(task);
                    codeEditor.replaceText(template);
                    StudyCod.saveTaskToDB(task, template);
                });
                loadLesson();
            } else {
                task = StudyCod.generateControlTask(StudyCod.getDifus(), StudyCod.getTopicsFromDB(StudyCod.getId()));
                currentTaskText = task;
                Platform.runLater(() -> {
                    taskListView.getItems().add("Контроль знань №" + StudyCod.getControlNum());
                    taskDescription.setText(task);
                    codeEditor.replaceText(template);
                    StudyCod.saveTaskToDB(task, template);
                });
                loadLesson();
            }
            Platform.runLater(() -> {
                lessonRead.setDisable(false);
                generateTask.setDisable(true);
                launchcode.setDisable(false);
                checkTask.setDisable(false);
            });
        }).start();
    }

    @FXML
    private void loadLesson() {
        if ((StudyCod.getTaskNum() / StudyCod.getControlNum()) % 26 != 1) {
            lessonContent.setText(getCurrentTopic() + "\n" + getCurrentLesson());
            lessonContent.setEditable(false);
            launchcode.setDisable(true);
            checkTask.setDisable(true);
            lessonRead.setVisible(true);
        } else {
            lessonContent.setText("Контроль знань №" + StudyCod.getControlNum());
            lessonContent.setEditable(false);
            launchcode.setDisable(true);
            checkTask.setDisable(true);
            lessonRead.setVisible(true);
        }
    }

    @FXML
    private void markLessonAsRead() {
        lessonContent.setVisible(false);
        launchcode.setDisable(false);
        checkTask.setDisable(false);
        lessonRead.setVisible(false);
    }

    @FXML
    private void runCode(ActionEvent event) throws IOException, InterruptedException {
        String code = codeEditor.getText();
        String codeWithoutPackage = removePackageDeclaration(code);
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "codeRunner");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File mainFile = new File(tempDir, "Main.java");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mainFile), StandardCharsets.UTF_8))) {
            writer.write(codeWithoutPackage);
        }
        Process compileProcess = new ProcessBuilder("javac", "-encoding", "UTF-8", mainFile.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
        compileProcess.waitFor();
        String compileOutput = readStream(compileProcess.getInputStream());
        if (compileProcess.exitValue() == 0) {
            Process runProcess = new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-cp", tempDir.getAbsolutePath(), "Main")
                    .redirectErrorStream(true)
                    .start();
            runProcess.waitFor();
            String runOutput = readStream(runProcess.getInputStream());
            consoleOutput.setText(runOutput);
        } else {
            consoleOutput.setText("Compilation failed:\n" + compileOutput);
        }
    }

    private String removePackageDeclaration(String code) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(code))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("package "))
                    sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    @FXML
    private void checkCode(ActionEvent event) {
        try {
            generateTask.setDisable(false);
            launchcode.setDisable(true);
            checkTask.setDisable(true);
            StudyCod studyCod = new StudyCod();
            String codeText = codeEditor.getText();
            String taskText = currentTaskText;
            int grade1 = StudyCod.transformTexttoJSON(studyCod.GradeforI(codeText, taskText).trim());
            int grade2 = StudyCod.transformTexttoJSON(studyCod.GradeforII(codeText, taskText).trim());
            int grade3 = StudyCod.transformTexttoJSON(studyCod.GradeforIII(codeText, taskText).trim());
            int grade = grade1 + grade2 + grade3;
            String task = "Завдання №" + StudyCod.getTaskNum();
            String comment = studyCod.Comment(codeText, taskText, grade1, grade2, grade3, grade);
            String result = "Оцінка: " + grade + "\n" + "Коментар: " + comment;
            consoleOutput.setText(result);
            StudyCod.updateTask(taskText, codeText, comment);
            if (grade >= 8) {
                StudyCod.setDifus(StudyCod.getDifus() + 0.03);
            } else if (grade == 7) {
                StudyCod.setDifus(StudyCod.getDifus() + 0.01);
            } else if (grade <= 4 && grade > 0) {
                StudyCod.setDifus(StudyCod.getDifus() - 0.045);
            } else if (grade == 0) {
                StudyCod.setDifus(StudyCod.getDifus() - 0.055);
            }
            if (StudyCod.getDifus() < 0)
                StudyCod.setDifus(0);
            else if (StudyCod.getDifus() > 1)
                StudyCod.setDifus(1);
            StudyCod.saveDifuseToDB(StudyCod.getDifus());
            StudyCod.saveGradeToDB(task, String.valueOf(grade), comment);
            String updateQuery = "UPDATE ustask SET num = num + 1 WHERE user_id = ?";
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setInt(1, StudyCod.getId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error executing database query: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("An error occurred in the checkCode method: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
