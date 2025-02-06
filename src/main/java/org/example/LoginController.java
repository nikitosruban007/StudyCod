package org.example;

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

import javax.swing.text.Style;


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

    @FXML
    public void closeDialog(MouseEvent event) {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    public void loginUser(ActionEvent event) {
        if (StudyCod.authenticateUser(login.getText(), password.getText())) {

            Stage currentStage = (Stage) imageView.getScene().getWindow();
            currentStage.close();
        } else {
            correctly.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(5));

            pause.setOnFinished(e -> correctly.setVisible(false));

            pause.play();
        }
    }

    @FXML
    public void openRegisterDialog(MouseEvent event) {
        try {
            Stage currentStage = (Stage) regtext.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reg.fxml"));
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
