package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.util.CurrentUserSession;

public class LoginDialogController {

    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnCancelLogin;
    @FXML
    private Label lblLoginStatus;

    private Stage dialogStage;
    private User loggedInUser = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    @FXML
    private void handleLoginButtonAction() {
        String login = txtLogin.getText();
        String password = txtPassword.getText();

        if (login.isEmpty() || password.isEmpty()) {
            lblLoginStatus.setText("Login i hasło są wymagane.");
            lblLoginStatus.setTextFill(Color.RED);
            return;
        }

        User user = User.authenticate(login, password);

        if (user != null) {
            this.loggedInUser = user;
            CurrentUserSession.loginUser(user);
            lblLoginStatus.setText("Logowanie pomyślne!");
            lblLoginStatus.setTextFill(Color.GREEN);
            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            lblLoginStatus.setText("Nieprawidłowy login lub hasło.");
            lblLoginStatus.setTextFill(Color.RED);
            txtPassword.clear();
        }
    }

    @FXML
    private void handleCancelLoginButtonAction() {
        this.loggedInUser = null;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}