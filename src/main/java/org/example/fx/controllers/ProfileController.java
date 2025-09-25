package org.example.fx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.User;
import org.example.services.UserManager;
import org.example.services.database.UserDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileController {

    @FXML
    public Label username;

    @FXML
    public Label id;

    @FXML
    public Label kcz;

    @FXML
    public ComboBox<String> languageComboBox;

    @FXML
    private ImageView imageView;

    @Autowired
    private UserManager userManager;

    User user = User.user();

    private UserDB userDB;

    @FXML
    public void closeDialog(MouseEvent event) {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }

    public void initialize() {
        if (user.isAuthorized()) {
            userDB = userManager.getUserById(user.getId());
            if (userDB != null) {
                username.setText(user.getUsername());
                id.setText(String.valueOf(user.getId()));
                kcz.setText(String.valueOf(user.getDifus()));

                languageComboBox.getItems().addAll("Java", "Python");
                languageComboBox.setValue(userDB.getLang());
            }
        }
    }

    @FXML
    public void saveLanguage(ActionEvent event) {
        if (userDB != null) {
            String selectedLanguage = languageComboBox.getValue();
            userDB.setLang(selectedLanguage);
            userManager.updateUser(userDB);
        }
    }

    @FXML
    public void exit(ActionEvent event) {
        UserManager.logout(user);
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }
}