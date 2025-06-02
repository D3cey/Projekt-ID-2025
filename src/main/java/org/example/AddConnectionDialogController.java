package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Polaczenie;
import org.example.model.Stacja;

import java.util.List;

public class AddConnectionDialogController {

    @FXML private ComboBox<Stacja> comboStacja1;
    @FXML private ComboBox<Stacja> comboStacja2;
    @FXML private TextField txtDistance;
    @FXML private Label lblStatus;
    @FXML private Button btnAdd;
    @FXML private Button btnCancel;

    private Stage dialogStage;
    private boolean connectionAdded = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setStations(List<Stacja> stacje) {
        comboStacja1.setItems(FXCollections.observableArrayList(stacje));
        comboStacja2.setItems(FXCollections.observableArrayList(stacje));
    }

    public boolean isConnectionAdded() {
        return connectionAdded;
    }

    @FXML
    private void handleAdd() {
        Stacja stacja1 = comboStacja1.getValue();
        Stacja stacja2 = comboStacja2.getValue();
        String distStr = txtDistance.getText().trim();

        lblStatus.setText(""); // Clear previous status

        if (stacja1 == null || stacja2 == null) {
            lblStatus.setText("Please select both stations.");
            lblStatus.setTextFill(Color.RED);
            return;
        }
        if (stacja1.getId() == stacja2.getId()) {
            lblStatus.setText("Stations must be different.");
            lblStatus.setTextFill(Color.RED);
            return;
        }
        if (distStr.isEmpty()) {
            lblStatus.setText("Please enter the distance.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        double distance;
        try {
            distance = Double.parseDouble(distStr);
            if (distance <= 0) {
                lblStatus.setText("Distance must be a positive value.");
                lblStatus.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException ex) {
            lblStatus.setText("Distance must be a valid number.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        boolean success = Polaczenie.dodajPolaczenie(stacja1.getId(), stacja2.getId(), distance);

        if (success) {
            connectionAdded = true;
            lblStatus.setText("Connection added successfully!");
            lblStatus.setTextFill(Color.GREEN);
            // dialogStage.close(); // uncomment to close immediately
        } else {
            lblStatus.setText("Failed to add connection. Check logs.");
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