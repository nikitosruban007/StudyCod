package org.example.fx.controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import org.example.StudyCod;
import org.example.User;
import org.example.UserSession;
import org.example.services.LanguageManager;
import org.springframework.stereotype.Component;

@Component
public class HomeSceneController {

    @FXML
    private javafx.scene.control.Button tasksButton;

    @FXML
    private javafx.scene.control.Button gradesButton;

    @FXML
    private javafx.scene.control.ComboBox<String> langChoice;

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
        // Language toggle on double-click of the welcome label (alternative)
        welcome.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                LanguageManager.toggle();
                applyTranslations();
                updateUserData();
                if (langChoice != null) {
                    langChoice.setValue(LanguageManager.get() == LanguageManager.Lang.UK ? "Українська" : "English");
                }
            }
        });
        if (langChoice != null) {
            langChoice.getItems().setAll("Українська", "English");
            langChoice.setValue(LanguageManager.get() == LanguageManager.Lang.UK ? "Українська" : "English");
        }
        applyTranslations();
        updateUserData();
    }

    private void applyTranslations() {
        // Buttons
        if (tasksButton != null) tasksButton.setText(LanguageManager.tr("nav.tasks"));
        if (gradesButton != null) gradesButton.setText(LanguageManager.tr("nav.grades"));
        // Auth warning default text
        if (authorizate != null) authorizate.setText(LanguageManager.tr("auth.required"));
    }

    private void updateUserData() {
        UserSession.loadUserData(user);

        if (!user.isAuthorized()) {
            welcome.setText(LanguageManager.tr("welcome.guest"));
            advice.setText(LanguageManager.tr("advice.prefix") + LanguageManager.tr("advice.askAuth"));
            authorizate.setVisible(false);
        } else {
            welcome.setText(String.format(LanguageManager.tr("welcome.user"), user.getUsername() != null ? user.getUsername() : ""));
            advice.setText(LanguageManager.tr("advice.prefix") + LanguageManager.tr("advice.loading"));
            loadAdviceAsync();
        }
    }

    private void loadAdviceAsync() {
        new Thread(() -> {
            String adv = StudyCod.motivateAdvice();
            javafx.application.Platform.runLater(() -> {
                advice.setText(LanguageManager.tr("advice.prefix") + adv);
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
        authStage.setTitle(LanguageManager.tr("auth.title"));
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
        profileStage.setTitle(LanguageManager.tr("profile.title"));
        profileStage.setScene(new Scene(root));
        profileStage.initModality(Modality.APPLICATION_MODAL);
        profileStage.show();
    }

    @FXML
    public void goToTasks() {
        try {
            openTasksScene();

        } catch (Exception e) {
            System.err.println("Error loading TasksScene: " + e.getMessage());
            e.printStackTrace();
            // Fallback - показать простое сообщение об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка");
            alert.setHeaderText("Не вдалося завантажити сцену завдань");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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
        authorizate.setText(LanguageManager.tr("auth.required"));
        authorizate.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> authorizate.setVisible(false));
        pause.play();
    }

    @FXML
    private void changeUiLanguage(ActionEvent event) {
        if (langChoice != null) {
            String selected = langChoice.getValue();
            if (selected != null && selected.startsWith("Укр")) {
                LanguageManager.set(LanguageManager.Lang.UK);
            } else {
                LanguageManager.set(LanguageManager.Lang.EN);
            }
            applyTranslations();
            updateUserData();
        }
    }
}