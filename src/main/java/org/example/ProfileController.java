package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ProfileController {
    @FXML
    public Label username;

    @FXML
    public Label id;

    @FXML
    public Label kcz;
    @FXML
    private ImageView imageView;

    @FXML
    public void closeDialog(MouseEvent event) {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }

    public void initialize() {
        if (StudyCod.isIsAuthorized()){
            username.setText(StudyCod.getCurrentUsername());
            id.setText(String.valueOf(StudyCod.getId()));
            kcz.setText(String.valueOf(StudyCod.getDifus()));
        }
    }

    @FXML
    public void exit(ActionEvent event) {
        StudyCod.logout();
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        currentStage.close();
    }
}
