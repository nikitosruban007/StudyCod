package org.example.fx.controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import org.example.StudyCod;
import org.example.User;
import org.example.UserSession;
import org.springframework.stereotype.Component;

@Component
public class HomeSceneController {

    @Setter
    private Stage primaryStage;

    @FXML
    public Label welcome;
    @FXML
    public Label advice;
    @FXML
    public Label authorizate;

    User user = User.user();

    @FXML
    public void initialize() {
        updateUserData();
    }

    private void updateUserData() {
        UserSession.loadUserData(user);

        if (!user.isAuthorized()) {
            welcome.setText("Вітаю в StudyCod!");
            advice.setText("Мотиваційна порада перед навчанням😉: Увійти або зареєструвати акаунт");
            authorizate.setVisible(false);
        } else {
            welcome.setText("Вітаю, " + user.getUsername() + "!");
            loadAdviceAsync();
        }
    }

    private void loadAdviceAsync() {
        new Thread(() -> {
            String adv = StudyCod.motivateAdvice();
            javafx.application.Platform.runLater(() -> {
                advice.setText("Мотиваційна порада перед навчанням😉: " + adv);
            });
        }).start();
    }

    @FXML
    private void handleAuth(ActionEvent event) {
        try {
            if (!user.isAuthorized()) {
                openLoginWindow();
            } else {
                openProfileWindow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLoginWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
        Parent root = loader.load();

        Stage authStage = new Stage();
        authStage.setTitle("Авторизація");
        authStage.setScene(new Scene(root));
        authStage.initModality(Modality.APPLICATION_MODAL);

        authStage.setOnHidden(e -> {
            updateUserData();
        });

        authStage.show();
    }

    private void openProfileWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
        loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
        Parent root = loader.load();

        Stage profileStage = new Stage();
        profileStage.setTitle("Профіль");
        profileStage.setScene(new Scene(root));
        profileStage.initModality(Modality.APPLICATION_MODAL);
        profileStage.show();
    }

    @FXML
    private void goToTasks(ActionEvent event) {
        if (user.isAuthorized()) {
            openTasksScene();
        } else {
            showAuthorizationWarning();
        }
    }

    private void openTasksScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TasksScene.fxml"));
            loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
            Parent root = loader.load();
            TasksSceneController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToGrades(ActionEvent event) {
        if (user.isAuthorized()) {
            openGradesScene();
        } else {
            showAuthorizationWarning();
        }
    }

    private void openGradesScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GradesScene.fxml"));
            loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
            Parent root = loader.load();

            GradesSceneController gradesController = loader.getController();
            gradesController.setPrimaryStage(primaryStage);

            primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAuthorizationWarning() {
        authorizate.setText("Будь ласка, увійдіть в систему");
        authorizate.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> authorizate.setVisible(false));
        pause.play();
    }
}