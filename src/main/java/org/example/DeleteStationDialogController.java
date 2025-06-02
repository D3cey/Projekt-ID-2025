package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Stacja;

import java.util.List;
import java.util.Optional;

public class DeleteStationDialogController {

    @FXML private ComboBox<Stacja> comboSelectStationToDelete;
    @FXML private Label lblDeleteStatus;
    @FXML private Button btnDelete;
    @FXML private Button btnCancel;

    private Stage dialogStage;
    private boolean stationDeleted = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setStations(List<Stacja> stacje) {
        comboSelectStationToDelete.setItems(FXCollections.observableArrayList(stacje));
    }

    public boolean isStationDeleted() {
        return stationDeleted;
    }

    @FXML
    private void handleDeleteConfirm() {
        Stacja stationToRemove = comboSelectStationToDelete.getValue();
        lblDeleteStatus.setText(""); // Clear previous status

        if (stationToRemove == null) {
            lblDeleteStatus.setText("Please select a station to remove.");
            lblDeleteStatus.setTextFill(Color.RED);
            return;
        }

        // Confirmation Alert within the dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.initOwner(dialogStage); // Make it modal to this dialog
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Delete Station: " + stationToRemove.getNazwa());
        confirmationAlert.setContentText("Are you sure you want to permanently delete this station?\n" +
                "This might fail if the station is part of existing connections or routes.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed deletion in the alert
            boolean success = Stacja.usunStacje(stationToRemove.getId());

            if (success) {
                stationDeleted = true;
                lblDeleteStatus.setText("Station '" + stationToRemove.getNazwa() + "' removed successfully.");
                lblDeleteStatus.setTextFill(Color.GREEN);
                // Close the dialog after successful deletion and confirmation
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                lblDeleteStatus.setText("Failed to remove station. It might be in use. Check console logs.");
                lblDeleteStatus.setTextFill(Color.RED);
            }
        } else {
            // User cancelled in the alert
            lblDeleteStatus.setText("Station removal cancelled.");
            lblDeleteStatus.setTextFill(Color.ORANGE);
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}