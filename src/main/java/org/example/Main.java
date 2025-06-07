package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.controllers.LoginDialogController;
import org.example.model.User;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        boolean loggedIn = showLoginDialog();

        if (loggedIn) {
            showMainWindow();
        } else {
            System.out.println("Logowanie anulowane lub nieudane. Aplikacja zostanie zamknięta.");
            Platform.exit();
        }
    }

    private boolean showLoginDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginDialog.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Logowanie");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        dialogStage.setOnCloseRequest(event -> {
            System.out.println("Dialog logowania zamknięty przez użytkownika.");
        });


        LoginDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);

        dialogStage.showAndWait();

        return controller.getLoggedInUser() != null;
    }

    private void showMainWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        primaryStage.setTitle("Pociągi – Mapa"); // Tytuł głównego okna
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}