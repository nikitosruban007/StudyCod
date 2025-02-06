package org.example;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GradesSceneController {
    private Stage primaryStage;
    private static final String DB_URL = "jdbc:mysql://109.94.209.168:3306/man?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "nikitosruban007";
    private static final String DB_PASSWORD = "Nikitos121109";
    @FXML
    private TableView<TaskDetails> gradesTable;

    @FXML
    private TableColumn<TaskDetails, String> taskNameColumn;

    @FXML
    private TableColumn<TaskDetails, Integer> gradeColumn;

    @FXML
    private TableColumn<TaskDetails, String> commentsColumn;


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void goBackToHome(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeScene.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HomeSceneController homeController = loader.getController();
        homeController.setPrimaryStage(primaryStage);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    @FXML
    public void exportTopdf(ActionEvent event) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Збереження Журналу Оцінювання в PDF");

                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedDate = today.format(formatter);
                fileChooser.setInitialFileName("Журнал Оцінювання " + StudyCod.getId() + " " + formattedDate + ".pdf");

                // Ограничиваем типы файлов
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

                File file = fileChooser.showSaveDialog(primaryStage);

                if (file != null) {
                    StudyCod.exportTableViewToPDF(gradesTable, primaryStage, file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @FXML
    public void refresh(ActionEvent event) {
        loadTaskDetails();
    }


    @FXML
    public void initialize() {

        taskNameColumn.setResizable(false);
        gradeColumn.setResizable(false);
        commentsColumn.setResizable(false);
        taskNameColumn.setReorderable(false);
        gradeColumn.setReorderable(false);
        commentsColumn.setReorderable(false);
        taskNameColumn.setCellValueFactory(new PropertyValueFactory<>("taskName"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

        loadTaskDetails();
    }

    private void loadTaskDetails() {
        ObservableList<TaskDetails> taskDetailsList = StudyCod.getTaskDetails(StudyCod.getId());
        gradesTable.setItems(taskDetailsList);
    }


}
