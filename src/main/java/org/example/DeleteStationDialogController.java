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

    @FXML
    private ComboBox<Stacja> comboSelectStationToDelete;
    @FXML
    private Label lblDeleteStatus;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnCancel;

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
        lblDeleteStatus.setText("");

        if (stationToRemove == null) {
            lblDeleteStatus.setText("Proszę wybrać stację do usunięcia.");
            lblDeleteStatus.setTextFill(Color.RED);
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.initOwner(dialogStage);
        confirmationAlert.setTitle("Potwierdź Usunięcie");
        confirmationAlert.setHeaderText("Usunąć stację: " + stationToRemove.getNazwa());
        confirmationAlert.setContentText("Czy na pewno chcesz trwale usunąć tę stację?\n" +
                "Operacja może się nie powieść, jeśli stacja jest częścią istniejących połączeń lub tras.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = Stacja.usunStacje(stationToRemove.getId());

            if (success) {
                stationDeleted = true;
                lblDeleteStatus.setText("Stacja '" + stationToRemove.getNazwa() + "' została pomyślnie usunięta.");
                lblDeleteStatus.setTextFill(Color.GREEN);

                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                lblDeleteStatus.setText("Nie udało się usunąć stacji. Może być w użyciu. Sprawdź logi konsoli.");
                lblDeleteStatus.setTextFill(Color.RED);
            }
        } else {
            lblDeleteStatus.setText("Anulowano usuwanie stacji.");
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