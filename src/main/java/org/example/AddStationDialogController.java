package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Stacja;

public class AddStationDialogController {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLat;
    @FXML
    private TextField txtLon;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    private Stage dialogStage;
    private boolean stationAdded = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isStationAdded() {
        return stationAdded;
    }

    @FXML
    private void handleAdd() {
        String name = txtName.getText().trim();
        String latStr = txtLat.getText().trim();
        String lonStr = txtLon.getText().trim();

        lblStatus.setText("");

        if (name.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
            lblStatus.setText("Wszystkie pola są wymagane.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lonStr);
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                lblStatus.setText("Nieprawidłowe wartości szerokości/długości geograficznej.");
                lblStatus.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException ex) {
            lblStatus.setText("Szerokość i długość geograficzna muszą być liczbami.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        boolean success = Stacja.dodajStacje(name, latitude, longitude);

        if (success) {
            stationAdded = true;
            lblStatus.setText("Stacja dodana pomyślnie!");
            lblStatus.setTextFill(Color.GREEN);
            // dialogStage.close();
        } else {
            lblStatus.setText("Nie udało się dodać stacji. Współrzędne mogą już istnieć lub wystąpił błąd bazy danych.");
            lblStatus.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}