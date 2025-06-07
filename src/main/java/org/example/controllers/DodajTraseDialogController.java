package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.util.DbUtil;
import org.example.model.NastepnaStacjaWrapper;
import org.example.model.Polaczenie;
import org.example.model.Stacja;
import org.example.model.StacjaNaTrasieWrapper;
import org.example.model.Trasa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DodajTraseDialogController {

    @FXML
    private ComboBox<Stacja> comboPierwszaStacja;
    @FXML
    private ComboBox<NastepnaStacjaWrapper> comboNastepnaStacja;
    @FXML
    private Button btnDodajOdcinek;
    @FXML
    private TableView<StacjaNaTrasieWrapper> tabelaStacjiNaTrasie;
    @FXML
    private TableColumn<StacjaNaTrasieWrapper, String> kolumnaNazwaStacji;
    @FXML
    private TableColumn<StacjaNaTrasieWrapper, Boolean> kolumnaZatrzymujeSie;
    @FXML
    private Label lblStatusTrasy;
    @FXML
    private Button btnZapiszTrase;
    @FXML
    private Button btnAnuluj;

    private Stage dialogStage;
    private boolean trasaDodana = false;

    private List<Stacja> wszystkieStacjeList;
    private ObservableList<StacjaNaTrasieWrapper> stacjeNaTrasieDoTabeli = FXCollections.observableArrayList();
    private List<Segment> segmentyDoZapisu = new ArrayList<>();

    private static class Segment {
        int stacja1Id;
        int stacja2Id;
        int polaczenieId;

        Segment(int s1, int s2, int pId) {
            stacja1Id = s1;
            stacja2Id = s2;
            polaczenieId = pId;
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isTrasaDodana() {
        return trasaDodana;
    }

    public void setWszystkieStacje(List<Stacja> stacje) {
        this.wszystkieStacjeList = stacje;
        comboPierwszaStacja.setItems(FXCollections.observableArrayList(stacje));
    }

    @FXML
    private void initialize() {
        tabelaStacjiNaTrasie.setItems(stacjeNaTrasieDoTabeli);
        kolumnaNazwaStacji.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStacja().getNazwa())
        );
        kolumnaZatrzymujeSie.setCellValueFactory(cellData -> cellData.getValue().zatrzymujeSieProperty());
        kolumnaZatrzymujeSie.setCellFactory(CheckBoxTableCell.forTableColumn(kolumnaZatrzymujeSie));
        kolumnaZatrzymujeSie.setEditable(true);
        tabelaStacjiNaTrasie.setEditable(true);

        comboPierwszaStacja.setOnAction(event -> {
            Stacja wybranaStacja = comboPierwszaStacja.getValue();
            if (wybranaStacja != null && stacjeNaTrasieDoTabeli.isEmpty()) {
                stacjeNaTrasieDoTabeli.add(new StacjaNaTrasieWrapper(wybranaStacja, 0, true));
                lblStatusTrasy.setText("Wybrano stację początkową: " + wybranaStacja.getNazwa());
                aktualizujDostepneNastepneStacje(wybranaStacja);
                comboPierwszaStacja.setDisable(true);
                btnDodajOdcinek.setDisable(comboNastepnaStacja.getItems().isEmpty());
                btnZapiszTrase.setDisable(true);
            }
        });
    }

    @FXML
    private void handleDodajOdcinek() {
        if (stacjeNaTrasieDoTabeli.isEmpty()) return;
        Stacja ostatniaStacja = stacjeNaTrasieDoTabeli.get(stacjeNaTrasieDoTabeli.size() - 1).getStacja();
        NastepnaStacjaWrapper wybranyWrapper = comboNastepnaStacja.getValue();

        if (wybranyWrapper == null) {
            lblStatusTrasy.setText("Wybierz następną stację.");
            lblStatusTrasy.setTextFill(Color.RED);
            return;
        }

        Stacja nastepnaStacja = wybranyWrapper.getStacja();
        int polaczenieId = wybranyWrapper.getPolaczenieId();


        segmentyDoZapisu.add(new Segment(ostatniaStacja.getId(), nastepnaStacja.getId(), polaczenieId));

        stacjeNaTrasieDoTabeli.add(new StacjaNaTrasieWrapper(nastepnaStacja, 0, true));

        comboNastepnaStacja.setValue(null);
        aktualizujDostepneNastepneStacje(nastepnaStacja);
        btnZapiszTrase.setDisable(false);
        lblStatusTrasy.setText("Dodano odcinek do: " + nastepnaStacja.getNazwa());
        lblStatusTrasy.setTextFill(Color.BLACK);
    }

    @FXML
    private void handleZapiszTrase() {
        if (segmentyDoZapisu.isEmpty()) {
            lblStatusTrasy.setText("Trasa musi składać się z przynajmniej jednego odcinka (dwóch stacji).");
            lblStatusTrasy.setTextFill(Color.RED);
            return;
        }

        int poczatkowaStacjaId = stacjeNaTrasieDoTabeli.get(0).getStacja().getId();
        Connection conn = null;
        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);


            int nowaTrasaId = Trasa.dodajTrase(conn, poczatkowaStacjaId);


            for (Segment segment : segmentyDoZapisu) {
                boolean zatrzymujeSie = stacjeNaTrasieDoTabeli.stream()
                        .filter(s -> s.getStacja().getId() == segment.stacja1Id)
                        .findFirst()
                        .map(StacjaNaTrasieWrapper::czySieZatrzymuje)
                        .orElse(true);

                boolean segmentDodany = Stacja.dodajSegmentDoTrasy(
                        conn, nowaTrasaId, segment.stacja1Id, segment.stacja2Id, segment.polaczenieId, zatrzymujeSie
                );
                if (!segmentDodany) {
                    throw new SQLException("Nie udało się dodać segmentu trasy: " + segment.stacja1Id + " -> " + segment.stacja2Id);
                }
            }

            conn.commit();
            trasaDodana = true;
            lblStatusTrasy.setText("Trasa została pomyślnie zapisana! ID Trasy: " + nowaTrasaId);
            lblStatusTrasy.setTextFill(Color.GREEN);
            btnDodajOdcinek.setDisable(true);
            btnZapiszTrase.setDisable(true);
            comboNastepnaStacja.setDisable(true);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            lblStatusTrasy.setText("Błąd zapisu trasy: " + e.getMessage());
            lblStatusTrasy.setTextFill(Color.RED);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void aktualizujDostepneNastepneStacje(Stacja ostatniaStacjaNaTrasie) {
        if (ostatniaStacjaNaTrasie == null) {
            comboNastepnaStacja.setItems(FXCollections.emptyObservableList());
            comboNastepnaStacja.setDisable(true);
            return;
        }

        List<Polaczenie> wszystkiePolaczenia = Polaczenie.pobierzWszystkie();
        List<NastepnaStacjaWrapper> dostepneNastepneWrappedStacje = new ArrayList<>();

        for (Polaczenie p : wszystkiePolaczenia) {
            if (p.getStacja1Id() == ostatniaStacjaNaTrasie.getId()) {
                Stacja potencjalnaNastepna = znajdzStacjePoId(p.getStacja2Id());
                if (potencjalnaNastepna != null) {
                    if (potencjalnaNastepna.getId() != ostatniaStacjaNaTrasie.getId()) {
                        dostepneNastepneWrappedStacje.add(new NastepnaStacjaWrapper(potencjalnaNastepna, p.getId(), p.getOdleglosc()));
                    }
                }
            }
        }

        if (stacjeNaTrasieDoTabeli.size() > 1) {
            Stacja przedostatniaStacja = stacjeNaTrasieDoTabeli.get(stacjeNaTrasieDoTabeli.size() - 2).getStacja();
            dostepneNastepneWrappedStacje = dostepneNastepneWrappedStacje.stream()
                    .filter(wrapper -> wrapper.getStacja().getId() != przedostatniaStacja.getId())
                    .collect(Collectors.toList());
        }

        comboNastepnaStacja.setItems(FXCollections.observableArrayList(dostepneNastepneWrappedStacje));
        boolean brakNastepnych = dostepneNastepneWrappedStacje.isEmpty();
        comboNastepnaStacja.setDisable(brakNastepnych);
        btnDodajOdcinek.setDisable(brakNastepnych);

        if (brakNastepnych && !stacjeNaTrasieDoTabeli.isEmpty()) {
            lblStatusTrasy.setText("Brak kierunkowych połączeń wychodzących z: " + ostatniaStacjaNaTrasie.getNazwa());
        } else if (!brakNastepnych) {
            lblStatusTrasy.setText("Wybierz następną stację z listy.");
        }
    }

    private Stacja znajdzStacjePoId(int id) {
        if (wszystkieStacjeList == null) return null;
        return wszystkieStacjeList.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    @FXML
    private void handleAnuluj() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}