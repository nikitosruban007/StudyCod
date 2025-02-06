package org.example;

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

public class RegController {

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

    @FXML
    public void closeDialog(MouseEvent event) {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    public void RegisterUser(ActionEvent event) {
        StudyCod.registerUser(login.getText(), password.getText(), correctly, imageView);
    }

    @FXML
    public void openLoginDialog(MouseEvent event) {
        try {
            Stage currentStage = (Stage) regtext.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
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
