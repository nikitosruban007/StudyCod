package org.example.fx.controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.StudyCod;
import org.example.services.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML
    public Label regtext;

    @FXML
    public TextField login;

    @FXML
    public PasswordField password;

    @FXML
    private ImageView imageView;

    @FXML
    public Label correctly;

    @Autowired
    private UserManager userManager;

    @FXML
    public void initialize() {
        correctly.setVisible(false);
    }

    @FXML
    public void closeDialog(MouseEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    public void loginUser(ActionEvent event) {
        if (userManager == null) {
            correctly.setText("Помилка системи. Спробуйте пізніше.");
            correctly.setVisible(true);
            return;
        }

        String username = login.getText().trim();
        String pass = password.getText().trim();

        if (username.isEmpty() || pass.isEmpty()) {
            correctly.setText("Будь ласка, заповніть всі поля");
            correctly.setVisible(true);
            return;
        }

        boolean success = userManager.authenticateUser(username, pass);
        if (success) {
            closeWindow(); // Закрываем окно после успешной авторизации
        } else {
            correctly.setText("Невірний логін або пароль");
            correctly.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> correctly.setVisible(false));
            pause.play();
        }
    }

    @FXML
    public void openRegisterDialog(MouseEvent event) {
        try {
            closeWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
            Parent root = loader.load();

            Stage secondDialogStage = new Stage();
            secondDialogStage.setTitle("Реєстрація");
            secondDialogStage.setScene(new Scene(root));
            secondDialogStage.initModality(Modality.APPLICATION_MODAL);
            secondDialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}