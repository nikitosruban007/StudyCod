package org.example.fx.controllers;

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
import lombok.Setter;
import org.example.StudyCod;
import org.example.TaskDetails;
import org.example.services.LanguageManager;
import org.example.User;
import org.example.services.GradeManager;
import org.example.services.database.UserDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class GradesSceneController {

    @Setter
    private Stage primaryStage;

    @FXML
    private TableView<TaskDetails> gradesTable;

    @FXML
    private TableColumn<TaskDetails, String> taskNameColumn, commentsColumn;

    @FXML
    private TableColumn<TaskDetails, Integer> gradeColumn;

    private final UserDB udb = new UserDB();

    @Autowired
    private GradeManager manager;

    @FXML
    private void goBackToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeScene.fxml"));
            loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
            Parent root = loader.load();

            HomeSceneController homeController = loader.getController();
            homeController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exportTopdf(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(LanguageManager.tr("grades.export.title"));

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = today.format(formatter);

            String username = User.user().getUsername() != null ? User.user().getUsername() : "unknown";
            fileChooser.setInitialFileName(String.format(LanguageManager.tr("grades.export.filename"), username, formattedDate));
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
        try {
            long uid = User.user().getId() != null ? User.user().getId() : 0L;
            ObservableList<TaskDetails> taskDetailsList = manager.getTaskDetailsList(Math.toIntExact(uid));
            gradesTable.setItems(taskDetailsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
