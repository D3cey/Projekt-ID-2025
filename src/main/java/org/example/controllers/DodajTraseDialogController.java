package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Polaczenie;
import org.example.model.Stacja;
import org.example.util.DbUtil;

import java.sql.Array;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.model.NastepnaStacjaWrapper;

public class DodajTraseDialogController {

    @FXML
    private ComboBox<Stacja> comboPierwszaStacja;
    @FXML
    private ComboBox<NastepnaStacjaWrapper> comboNastepnaStacja;
    @FXML
    private Button btnDodajOdcinek;
    @FXML
    private ListView<String> listaOdcinkowTrasy;
    @FXML
    private Label lblStatusTrasy;
    @FXML
    private Button btnZapiszTrase;
    @FXML
    private Button btnAnuluj;

    private Stage dialogStage;
    private boolean trasaDodana = false;

    private ObservableList<Stacja> wszystkieStacjeList;
    private List<Stacja> aktualnieBudowanaTrasaStacje = new ArrayList<>();
    private List<Integer> aktualnieBudowanaTrasaPolaczeniaIds = new ArrayList<>();


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setWszystkieStacje(List<Stacja> stacje) {
        this.wszystkieStacjeList = FXCollections.observableArrayList(stacje);
        comboPierwszaStacja.setItems(this.wszystkieStacjeList);
    }

    public boolean isTrasaDodana() {
        return trasaDodana;
    }

    @FXML
    private void initialize() {
        comboPierwszaStacja.setOnAction(event -> {
            Stacja wybranaStacja = comboPierwszaStacja.getValue();
            if (wybranaStacja != null && aktualnieBudowanaTrasaStacje.isEmpty()) {
                aktualnieBudowanaTrasaStacje.add(wybranaStacja);
                aktualnieBudowanaTrasaPolaczeniaIds.clear();
                listaOdcinkowTrasy.getItems().clear();
                listaOdcinkowTrasy.getItems().add(wybranaStacja.getNazwa());
                lblStatusTrasy.setText("Wybrano stację początkową: " + wybranaStacja.getNazwa());
                aktualizujDostepneNastepneStacje(wybranaStacja);
                comboPierwszaStacja.setDisable(true);
                btnDodajOdcinek.setDisable(comboNastepnaStacja.getItems().isEmpty());
                btnZapiszTrase.setDisable(false);
            }
        });
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
                        dostepneNastepneWrappedStacje.add(
                                new NastepnaStacjaWrapper(potencjalnaNastepna, p.getId(), p.getOdleglosc())
                        );
                    }
                }
            }
        }

        if (aktualnieBudowanaTrasaStacje.size() > 1) {
            Stacja przedostatniaStacja = aktualnieBudowanaTrasaStacje.get(aktualnieBudowanaTrasaStacje.size() - 2);
            dostepneNastepneWrappedStacje = dostepneNastepneWrappedStacje.stream()
                    .filter(wrapper -> wrapper.getStacja().getId() != przedostatniaStacja.getId())
                    .collect(Collectors.toList());
        }

        comboNastepnaStacja.setItems(FXCollections.observableArrayList(dostepneNastepneWrappedStacje));
        boolean brakNastepnych = dostepneNastepneWrappedStacje.isEmpty();
        comboNastepnaStacja.setDisable(brakNastepnych);
        btnDodajOdcinek.setDisable(brakNastepnych);

        if (brakNastepnych && !aktualnieBudowanaTrasaStacje.isEmpty()) {
            lblStatusTrasy.setText("Brak kierunkowych połączeń wychodzących z: " + ostatniaStacjaNaTrasie.getNazwa());
        } else if (!brakNastepnych) {
            lblStatusTrasy.setText("Wybierz następną stację z listy (połączenia kierunkowe).");
        } else if (aktualnieBudowanaTrasaStacje.isEmpty()) {
            lblStatusTrasy.setText("Wybierz stację początkową.");
        }
    }

    private Stacja znajdzStacjePoId(int id) {
        if (wszystkieStacjeList == null) return null;
        return wszystkieStacjeList.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    @FXML
    private void handleDodajOdcinek() {
        Stacja ostatniaStacja = aktualnieBudowanaTrasaStacje.isEmpty() ? null : aktualnieBudowanaTrasaStacje.get(aktualnieBudowanaTrasaStacje.size() - 1);
        NastepnaStacjaWrapper wybranyWrapper = comboNastepnaStacja.getValue();

        if (ostatniaStacja == null || wybranyWrapper == null) {
            lblStatusTrasy.setText("Najpierw wybierz stację początkową, a potem następną stację.");
            lblStatusTrasy.setTextFill(Color.RED);
            return;
        }

        Stacja nastepnaStacja = wybranyWrapper.getStacja();
        int polaczenieId = wybranyWrapper.getPolaczenieId();
        double odlegloscOdcinka = wybranyWrapper.getOdlegloscOdcinka();

        aktualnieBudowanaTrasaPolaczeniaIds.add(polaczenieId);
        aktualnieBudowanaTrasaStacje.add(nastepnaStacja);

        listaOdcinkowTrasy.getItems().add(
                String.format("-> %s (Połączenie ID: %d, Dystans: %.2f km)",
                        nastepnaStacja.getNazwa(), polaczenieId, odlegloscOdcinka)
        );
        comboNastepnaStacja.setValue(null);
        aktualizujDostepneNastepneStacje(nastepnaStacja);
        btnZapiszTrase.setDisable(false);
        lblStatusTrasy.setText("Dodano odcinek: " + ostatniaStacja.getNazwa() + " -> " + nastepnaStacja.getNazwa());
        lblStatusTrasy.setTextFill(Color.BLACK);
    }

    @FXML
    private void handleZapiszTrase() {
        if (aktualnieBudowanaTrasaStacje.isEmpty()) {
            lblStatusTrasy.setText("Trasa jest pusta. Wybierz przynajmniej stację początkową.");
            lblStatusTrasy.setTextFill(Color.RED);
            return;
        }

        if (aktualnieBudowanaTrasaPolaczeniaIds.isEmpty()) {
            lblStatusTrasy.setText("Trasa musi zawierać co najmniej jeden odcinek (połączenie).");
            lblStatusTrasy.setTextFill(Color.RED);
            return;
        }

        Stacja poczatkowaStacja = aktualnieBudowanaTrasaStacje.get(0);
        Integer[] polaczeniaIdsArray = aktualnieBudowanaTrasaPolaczeniaIds.toArray(new Integer[0]);

        String nazwaFunkcji = "dodaj_trase";
        int idPoczatkowejStacji = poczatkowaStacja.getId();
        String polaczeniaArrayForLog;

        if (polaczeniaIdsArray.length == 0) {
            polaczeniaArrayForLog = "ARRAY[]::INTEGER[]";
        } else {
            StringBuilder sb = new StringBuilder("ARRAY[");
            for (int i = 0; i < polaczeniaIdsArray.length; i++) {
                sb.append(polaczeniaIdsArray[i]);
                if (i < polaczeniaIdsArray.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            polaczeniaArrayForLog = sb.toString();
        }

        String sqlWykonanyPrzezJdbc = String.format("{? = call %s(?, ?)}", nazwaFunkcji);
        String sqlDoWyswietlenia = String.format(
                "SELECT %s(stacja_poczatkowa_id_param := %d, polaczenia_ids_param := %s);\n",
                nazwaFunkcji,
                idPoczatkowejStacji,
                polaczeniaArrayForLog
        );

        System.out.println(sqlDoWyswietlenia);


        try (Connection conn = DbUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sqlWykonanyPrzezJdbc)) {

            cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
            cstmt.setInt(2, idPoczatkowejStacji);

            Array sqlArrayPolaczenia = conn.createArrayOf("INTEGER", polaczeniaIdsArray);
            cstmt.setArray(3, sqlArrayPolaczenia);

            cstmt.execute();

            int nowaTrasaId = cstmt.getInt(1);

            if (nowaTrasaId > 0) {
                trasaDodana = true;
                lblStatusTrasy.setText("Trasa została pomyślnie zapisana! ID Trasy: " + nowaTrasaId);
                lblStatusTrasy.setTextFill(Color.GREEN);
                btnDodajOdcinek.setDisable(true);
                btnZapiszTrase.setDisable(true);
                comboNastepnaStacja.setDisable(true);
                // dialogStage.close();
            } else {
                lblStatusTrasy.setText("Nie udało się zapisać trasy (funkcja nie zwróciła poprawnego ID).");
                lblStatusTrasy.setTextFill(Color.RED);
            }
            sqlArrayPolaczenia.free();

        } catch (SQLException e) {
            lblStatusTrasy.setText("Błąd zapisu trasy do bazy danych: " + e.getMessage());
            lblStatusTrasy.setTextFill(Color.RED);
            System.err.println("Szczegółowy błąd SQL przy zapisie trasy:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnuluj() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}