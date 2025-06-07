package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Stacja;
import org.example.model.StacjaNaTrasieWrapper;
import org.example.model.Trasa;

import java.util.List;

public class EdytujTraseDialogController {

    @FXML
    private ComboBox<Trasa> comboWybierzTrase;
    @FXML
    private TableView<StacjaNaTrasieWrapper> tabelaStacji;
    @FXML
    private TableColumn<StacjaNaTrasieWrapper, String> kolumnaNazwaStacji;
    @FXML
    private TableColumn<StacjaNaTrasieWrapper, Boolean> kolumnaZatrzymujeSie;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnZapiszZmiany;
    @FXML
    private Button btnAnuluj;

    private Stage dialogStage;
    private ObservableList<StacjaNaTrasieWrapper> stacjeNaWybranejTrasie = FXCollections.observableArrayList();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTrasy(List<Trasa> trasy) {
        comboWybierzTrase.setItems(FXCollections.observableArrayList(trasy));
    }

    @FXML
    private void initialize() {
        kolumnaNazwaStacji.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStacja().getNazwa())
        );

        kolumnaZatrzymujeSie.setCellValueFactory(cellData -> cellData.getValue().zatrzymujeSieProperty());
        kolumnaZatrzymujeSie.setCellFactory(CheckBoxTableCell.forTableColumn(kolumnaZatrzymujeSie));
        kolumnaZatrzymujeSie.setEditable(true);
        tabelaStacji.setEditable(true);

        comboWybierzTrase.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        zaladujStacjeDlaTrasy(newValue.getId());
                        btnZapiszZmiany.setDisable(false);
                    }
                }
        );
        tabelaStacji.setItems(stacjeNaWybranejTrasie);
    }

    private void zaladujStacjeDlaTrasy(int trasaId) {
        List<StacjaNaTrasieWrapper> stacje = Stacja.pobierzStacjeDlaTrasy(trasaId);
        stacjeNaWybranejTrasie.clear();
        stacjeNaWybranejTrasie.addAll(stacje);
    }

    @FXML
    private void handleZapiszZmiany() {
        boolean allSuccess = true;
        for (StacjaNaTrasieWrapper wrapper : stacjeNaWybranejTrasie) {
            boolean success = Stacja.zaktualizujStatusZatrzymania(
                    wrapper.getTrasaId(),
                    wrapper.getStacja().getId(),
                    wrapper.czySieZatrzymuje()
            );
            if (!success) {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            lblStatus.setText("Zmiany zostały pomyślnie zapisane!");
            lblStatus.setTextFill(Color.GREEN);
            // dialogStage.close();
        } else {
            lblStatus.setText("Wystąpił błąd podczas zapisywania zmian. Sprawdź logi.");
            lblStatus.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleAnuluj() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}