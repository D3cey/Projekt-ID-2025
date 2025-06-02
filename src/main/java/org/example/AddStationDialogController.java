package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Stacja;

public class AddStationDialogController {

    @FXML private TextField txtName;
    @FXML private TextField txtLat;
    @FXML private TextField txtLon;
    @FXML private Label lblStatus;
    @FXML private Button btnAdd;
    @FXML private Button btnCancel;

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

        lblStatus.setText(""); // Clear previous status

        if (name.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
            lblStatus.setText("All fields are required.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lonStr);
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                lblStatus.setText("Invalid latitude/longitude values.");
                lblStatus.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException ex) {
            lblStatus.setText("Latitude and Longitude must be numbers.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        boolean success = Stacja.dodajStacje(name, latitude, longitude);

        if (success) {
            stationAdded = true;
            lblStatus.setText("Station added successfully!");
            lblStatus.setTextFill(Color.GREEN);
            // Optionally disable add button or close dialog immediately
            // For now, user clicks cancel or adds another.
            // dialogStage.close(); // uncomment to close immediately on success
        } else {
            lblStatus.setText("Failed to add station. Coordinates might exist or DB error.");
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