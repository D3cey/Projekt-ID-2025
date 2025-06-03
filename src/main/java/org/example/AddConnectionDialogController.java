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

    @FXML
    private ComboBox<Stacja> comboStacja1;
    @FXML
    private ComboBox<Stacja> comboStacja2;
    @FXML
    private TextField txtDistance;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

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
        String strOdleglosc = txtDistance.getText().trim();

        lblStatus.setText("");

        if (stacja1 == null || stacja2 == null) {
            lblStatus.setText("Proszę wybrać obie stacje.");
            lblStatus.setTextFill(Color.RED);
            return;
        }
        if (stacja1.getId() == stacja2.getId()) {
            lblStatus.setText("Stacje muszą być różne.");
            lblStatus.setTextFill(Color.RED);
            return;
        }
        if (strOdleglosc.isEmpty()) {
            lblStatus.setText("Proszę wprowadzić odległość.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        double odleglosc;
        try {
            odleglosc = Double.parseDouble(strOdleglosc);
            if (odleglosc <= 0) {
                lblStatus.setText("Odległość musi być wartością dodatnią.");
                lblStatus.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException ex) {
            lblStatus.setText("Odległość musi być poprawną liczbą.");
            lblStatus.setTextFill(Color.RED);
            return;
        }

        boolean sukces = Polaczenie.dodajPolaczenie(stacja1.getId(), stacja2.getId(), odleglosc);

        if (sukces) {
            connectionAdded = true;
            lblStatus.setText("Połączenie dodane pomyślnie!");
            lblStatus.setTextFill(Color.GREEN);
            // scenaDialogowa.close();
        } else {
            lblStatus.setText("Nie udało się dodać połączenia. Sprawdź logi.");
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