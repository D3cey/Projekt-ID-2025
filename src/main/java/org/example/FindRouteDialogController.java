package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Stacja;

import java.util.List;

public class FindRouteDialogController {

    @FXML private ComboBox<Stacja> comboDialogStacjaA;
    @FXML private ComboBox<Stacja> comboDialogStacjaB;
    @FXML private Label lblDialogStatus;
    @FXML private Button btnDialogFind;
    @FXML private Button btnDialogCancel;

    private Stage dialogStage;
    private Stacja selectedStacjaA = null;
    private Stacja selectedStacjaB = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setStations(List<Stacja> stacje) {
        comboDialogStacjaA.setItems(FXCollections.observableArrayList(stacje));
        comboDialogStacjaB.setItems(FXCollections.observableArrayList(stacje));
    }

    public Stacja getSelectedStacjaA() {
        return selectedStacjaA;
    }

    public Stacja getSelectedStacjaB() {
        return selectedStacjaB;
    }

    @FXML
    private void handleDialogFind() {
        Stacja stacjaA = comboDialogStacjaA.getValue();
        Stacja stacjaB = comboDialogStacjaB.getValue();

        lblDialogStatus.setText(""); // Clear previous status

        if (stacjaA == null || stacjaB == null) {
            lblDialogStatus.setText("Please select both stations.");
            lblDialogStatus.setTextFill(Color.RED);
            return;
        }

        if (stacjaA.getId() == stacjaB.getId()) {
            lblDialogStatus.setText("Start and end stations must be different.");
            lblDialogStatus.setTextFill(Color.RED);
            return;
        }

        this.selectedStacjaA = stacjaA;
        this.selectedStacjaB = stacjaB;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleDialogCancel() {
        this.selectedStacjaA = null;
        this.selectedStacjaB = null;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}