package org.example.fx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.StudyCod;
import org.example.services.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegController {

    @FXML
    public Label regtext;

    @FXML
    public TextField login;

    @FXML
    public PasswordField password;

    @FXML
    public ComboBox<String> languageComboBox;

    @FXML
    private ImageView imageView;

    @FXML
    public Label correctly;

    private final UserManager userManager;

    @Autowired
    public RegController(UserManager userManager) {
        this.userManager = userManager;
    }

    @FXML
    public void initialize() {
        correctly.setVisible(false);
        languageComboBox.getItems().addAll("Java", "Python");
        languageComboBox.setValue("Java");
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
    public void RegisterUser(ActionEvent event) {
        String username = login.getText().trim();
        String pass = password.getText().trim();
        String language = languageComboBox.getValue();

        if (username.isEmpty() || pass.isEmpty()) {
            correctly.setText("Будь ласка, заповніть всі поля");
            correctly.setVisible(true);
            return;
        }

        boolean success = userManager.registerUser(username, pass, language);
        if (success) {
            closeWindow(); // Закрываем окно после успешной регистрации
        } else {
            correctly.setText("Помилка реєстрації (ім'я зайняте)");
            correctly.setVisible(true);
        }
    }

    @FXML
    public void openLoginDialog(MouseEvent event) {
        try {
            closeWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            loader.setControllerFactory(StudyCod.getSpringContext()::getBean);
            Parent root = loader.load();

            Stage secondDialogStage = new Stage();
            secondDialogStage.setTitle("Авторизація");
            secondDialogStage.setScene(new Scene(root));
            secondDialogStage.initModality(Modality.APPLICATION_MODAL);
            secondDialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}