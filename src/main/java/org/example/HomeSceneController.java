package org.example;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;


public class HomeSceneController {
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public Label welcome;
    @FXML
    public Label advice;
    @FXML
    public Label authorizate;


    @FXML
    public void initialize() {
        if (!StudyCod.isIsAuthorized()) {
            setWelcome(null);
            setAdvice(null);
        } else {
            setWelcome(StudyCod.getCurrentUsername());
            setAdvice(StudyCod.motivateAdvice());
        }
    }

    public void setWelcome(String username) {
        if (username != null) {
            welcome.setText("Вітаю, " + username + "!");

        } else {
            welcome.setText("Вітаю, невідомий користувач!");
        }
    }

    public void setAdvice(String AIadvice) {
        if (AIadvice != null && StudyCod.isIsAuthorized()) {
            advice.setText("Мотиваційна порада перед навчанням😉: " + AIadvice);
        } else {
            advice.setText("Мотиваційна порада перед навчанням😉: Увійти або зареєструвати акаунт");
        }
    }


    @FXML
    private void handleAuth(ActionEvent event) {
        if (!StudyCod.isIsAuthorized()) {
            try {
                // Загружаем FXML для второго окна
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Parent root = loader.load();

                // Создаем второе окно (Stage)
                Stage secondDialogStage = new Stage();
                secondDialogStage.setTitle("Авторизація");
                secondDialogStage.setScene(new Scene(root));

                // Настроим окно как модальное
                secondDialogStage.initModality(Modality.APPLICATION_MODAL);
                secondDialogStage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
                Parent root = loader.load();

                Stage secondDialogStage = new Stage();
                secondDialogStage.setTitle("Профіль");
                secondDialogStage.setScene(new Scene(root));

                secondDialogStage.initModality(Modality.APPLICATION_MODAL);
                secondDialogStage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goToTasks(ActionEvent event) throws IOException {
        if (StudyCod.isIsAuthorized()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/TasksScene.fxml"));
                Parent root = loader.load();
                TasksSceneController controller = loader.getController();
                controller.setPrimaryStage(primaryStage); // Передаємо primaryStage

                primaryStage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            authorizate.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(5));

            pause.setOnFinished(e -> authorizate.setVisible(false));

            pause.play();
        }
    }

    @FXML
    private void goToGrades(ActionEvent event) {
        if (StudyCod.isIsAuthorized()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GradesScene.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            GradesSceneController gradesController = loader.getController();
            gradesController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            authorizate.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(5));

            pause.setOnFinished(e -> authorizate.setVisible(false));

            pause.play();
        }
    }
}
