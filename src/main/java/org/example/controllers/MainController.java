package org.example.controllers;

import com.sothawo.mapjfx.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Polaczenie;
import org.example.model.PolaczeniePociagowe;
import org.example.model.Stacja;
import org.example.model.Trasa;
import org.example.util.CurrentUserSession;

import java.util.stream.Collectors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private Pane mapContainer;

    @FXML
    private Button btnShowAddStationDialog;
    @FXML
    private Button btnShowAddConnectionDialog;
    @FXML
    private Button btnShowFindRouteDialog;
    @FXML
    private Button btnShowDeleteStationDialog;
    @FXML
    private Button btnShowAddTrasaDialog;
    @FXML
    private Button btnShowEditTrasaDialog;

    @FXML
    private VBox adminControlsPane;

    @FXML
    private Label lblStatus;

    private MapView mapView;
    private final List<Marker> allMarkers = new ArrayList<>();
    private final List<CoordinateLine> allLines = new ArrayList<>();
    private final List<CoordinateLine> redLines = new ArrayList<>();

    private List<Stacja> wszystkieStacjeZBazy;
    private List<Polaczenie> wszystkiePolaczeniaZBazy;

    @FXML
    public void initialize() {
        mapView = new MapView();
        mapView.initialize(Configuration.builder()
                .projection(Projection.WEB_MERCATOR)
                .showZoomControls(true)
                .build());
        mapView.prefWidthProperty().bind(mapContainer.widthProperty());
        mapView.prefHeightProperty().bind(mapContainer.heightProperty());
        mapContainer.getChildren().add(mapView);

        this.wszystkieStacjeZBazy = Stacja.pobierzWszystkie();
        this.wszystkiePolaczeniaZBazy = Polaczenie.pobierzWszystkie();

        mapView.initializedProperty().addListener((obs, oldReady, ready) -> {
            if (ready) {
                mapView.setMapType(MapType.OSM);
                mapView.setCenter(new Coordinate(52.2297, 21.0118));
                mapView.setZoom(7);


                aktualizujWyswietlanieMapy();


                mapView.zoomProperty().addListener((observable, oldZoom, newZoom) -> {
                    aktualizujWyswietlanieMapy();
                });
            }
        });

        if (btnShowAddStationDialog != null) btnShowAddStationDialog.setOnAction(e -> showAddStationDialog());
        if (btnShowAddConnectionDialog != null) btnShowAddConnectionDialog.setOnAction(e -> showAddConnectionDialog());
        if (btnShowFindRouteDialog != null) btnShowFindRouteDialog.setOnAction(e -> showFindRouteDialog());
        if (btnShowDeleteStationDialog != null) btnShowDeleteStationDialog.setOnAction(e -> showDeleteStationDialog());
        if (btnShowAddTrasaDialog != null) btnShowAddTrasaDialog.setOnAction(e -> showDodajTraseDialog());
        if (btnShowEditTrasaDialog != null) btnShowEditTrasaDialog.setOnAction(e -> showEdytujTraseDialog());

        if (CurrentUserSession.isLoggedIn()) {
            boolean isAdmin = CurrentUserSession.isAdmin();

            if (adminControlsPane != null) {
                adminControlsPane.setVisible(isAdmin);
                adminControlsPane.setManaged(isAdmin);

                if (isAdmin) {
                    adminControlsPane.requestLayout();
                    // if (adminControlsPane.getParent() != null) {
                    //     adminControlsPane.getParent().requestLayout();
                    // }
                }

                lblStatus.setText("Zalogowano jako: " + CurrentUserSession.getLoggedInUser().getLogin() + (isAdmin ? " (Admin)" : ""));
                lblStatus.setTextFill(Color.DARKGREEN);
            } else {
                lblStatus.setText("Nie zalogowano. Funkcjonalność ograniczona.");
                lblStatus.setTextFill(Color.ORANGE);

                if (adminControlsPane != null) {
                    adminControlsPane.setVisible(false);
                    adminControlsPane.setManaged(false);
                }
            }
        }
    }

    private void aktualizujWyswietlanieMapy() {
        double aktualnyZoom = mapView.getZoom();
        double progPowierzchni = obliczProgPowierzchni(aktualnyZoom);


        List<Stacja> stacjeDoWyswietlenia = wszystkieStacjeZBazy.stream()
                .filter(stacja -> stacja.getPowierzchniaMiasta() >= progPowierzchni)
                .collect(Collectors.toList());


        allMarkers.forEach(mapView::removeMarker);
        allMarkers.clear();
        for (Stacja s : stacjeDoWyswietlenia) {
            Marker m = Marker.createProvided(Marker.Provided.BLUE)
                    .setPosition(coord(s))
                    .setVisible(true)
                    .attachLabel(new MapLabel(s.getNazwa(), 10, -10).setCssClass("map-label-station"));
            mapView.addMarker(m);
            allMarkers.add(m);
        }


        allLines.forEach(mapView::removeCoordinateLine);
        allLines.clear();

        rysujPolaczenia(stacjeDoWyswietlenia);
    }

    /**
     * Oblicza próg powierzchni miasta, powyżej którego stacje będą widoczne.
     *
     * @param zoom aktualny poziom przybliżenia mapy.
     * @return Minimalna powierzchnia miasta, aby stacja była widoczna.
     */
    private double obliczProgPowierzchni(double zoom) {

        if (zoom < 8) {
            return 300.0;
        } else if (zoom < 9) {
            return 100.0;
        } else if (zoom < 10) {
            return 50.0;
        } else if (zoom < 12) {
            return 10.0;
        } else {
            return 0;
        }
    }

    /**
     * Rysuje szare linie połączeń tylko między stacjami, które są aktualnie widoczne.
     *
     * @param widoczneStacje Lista stacji, które spełniają kryteria widoczności.
     */
    private void rysujPolaczenia(List<Stacja> widoczneStacje) {
        List<Integer> widoczneStacjeIds = widoczneStacje.stream()
                .map(Stacja::getId)
                .collect(Collectors.toList());

        for (Polaczenie p : wszystkiePolaczeniaZBazy) {
            if (widoczneStacjeIds.contains(p.getStacja1Id()) && widoczneStacjeIds.contains(p.getStacja2Id())) {
                Stacja s1 = byId(widoczneStacje, p.getStacja1Id());
                Stacja s2 = byId(widoczneStacje, p.getStacja2Id());
                if (s1 != null && s2 != null) {
                    CoordinateLine cl = new CoordinateLine(coord(s1), coord(s2))
                            .setColor(Color.web("#444444")).setWidth(3).setVisible(true);
                    mapView.addCoordinateLine(cl);
                    allLines.add(cl);
                }
            }
        }
    }

    public void refreshStationData() {
        this.wszystkieStacjeZBazy = Stacja.pobierzWszystkie();
        this.wszystkiePolaczeniaZBazy = Polaczenie.pobierzWszystkie();

        aktualizujWyswietlanieMapy();
    }

    private void showEdytujTraseDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EdytujTraseDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edycja Postojów na Trasie");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            EdytujTraseDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTrasy(Trasa.pobierzWszystkie());

            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Błąd otwierania dialogu edycji trasy: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        }
    }

    private void showDodajTraseDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DodajTraseDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Dodaj Nową Trasę");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            DodajTraseDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setWszystkieStacje(Stacja.pobierzWszystkie());
            dialogStage.showAndWait();
            if (controller.isTrasaDodana()) {
                lblStatus.setText("Nowa trasa została pomyślnie dodana.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Błąd otwierania dialogu dodawania trasy: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        }
    }

    private void showDeleteStationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteStationDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Usuń Stację");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            DeleteStationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setStations(Stacja.pobierzWszystkie());
            dialogStage.showAndWait();
            if (controller.isStationDeleted()) {
                refreshStationData();
                loadConnections();
                lblStatus.setText("Stacja usunięta pomyślnie.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Błąd otwierania dialogu usuwania stacji: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        }
    }

    private void showAddStationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddStationDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Dodaj Nową Stację");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            AddStationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.isStationAdded()) {
                refreshStationData();
                lblStatus.setText("Nowa stacja dodana pomyślnie.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Błąd otwierania dialogu dodawania stacji: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        }
    }

    private void showAddConnectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddConnectionDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Dodaj Nowe Połączenie");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            AddConnectionDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setStations(Stacja.pobierzWszystkie());
            dialogStage.showAndWait();
            if (controller.isConnectionAdded()) {
                loadConnections();
                lblStatus.setText("Nowe połączenie dodane pomyślnie.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Błąd otwierania dialogu dodawania połączenia: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        }
    }

    private void showFindRouteDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FindRouteDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Znajdź Trasę");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            FindRouteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setStations(Stacja.pobierzWszystkie());
            dialogStage.showAndWait();
            Stacja fromStation = controller.getSelectedStacjaA();
            Stacja toStation = controller.getSelectedStacjaB();
            if (fromStation != null && toStation != null) {
                findAndDisplayRoute(fromStation, toStation);
            } else {
                lblStatus.setText("Wyszukiwanie trasy anulowane lub stacje nie zostały wybrane.");
                lblStatus.setTextFill(Color.ORANGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Błąd otwierania dialogu znajdowania trasy: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        }
    }

    private void findAndDisplayRoute(Stacja stacjaA, Stacja stacjaB) {
        if (stacjaA == null || stacjaB == null) {
            lblStatus.setText("Stacje dla wyszukiwania trasy nie są poprawnie wybrane.");
            lblStatus.setTextFill(Color.RED);
            return;
        }
        redLines.forEach(mapView::removeCoordinateLine);
        redLines.clear();
        lblStatus.setText("");
        List<PolaczeniePociagowe> pociagoweList = PolaczeniePociagowe.pobierzWszystkie();
        List<Stacja> allStacjeList = Stacja.pobierzWszystkie();
        boolean routeFoundOnMap = false;
        for (PolaczeniePociagowe pp : pociagoweList) {
            if (pp.getOdcinki().isEmpty()) continue;
            List<Integer> idsOnThisRoute = new ArrayList<>();
            idsOnThisRoute.add(pp.getOdcinki().get(0).getStacja1Id());
            for (Polaczenie segment : pp.getOdcinki()) {
                idsOnThisRoute.add(segment.getStacja2Id());
            }
            int startIndex = idsOnThisRoute.indexOf(stacjaA.getId());
            int endIndex = idsOnThisRoute.indexOf(stacjaB.getId());
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                for (int i = startIndex; i < endIndex; i++) {
                    Stacja s1 = byId(allStacjeList, idsOnThisRoute.get(i));
                    Stacja s2 = byId(allStacjeList, idsOnThisRoute.get(i + 1));
                    if (s1 != null && s2 != null) {
                        CoordinateLine r = new CoordinateLine(coord(s1), coord(s2))
                                .setColor(Color.RED).setWidth(7).setVisible(true);
                        mapView.addCoordinateLine(r);
                        redLines.add(r);
                    }
                }
                mapView.setCenter(new Coordinate(
                        (stacjaA.getSzerokoscGeograficzna() + stacjaB.getSzerokoscGeograficzna()) / 2.0,
                        (stacjaA.getDlugoscGeograficzna() + stacjaB.getDlugoscGeograficzna()) / 2.0));
                routeFoundOnMap = true;
                lblStatus.setText("Wyświetlono trasę: " + stacjaA.getNazwa() + " -> " + stacjaB.getNazwa());
                lblStatus.setTextFill(Color.GREEN);
                break;
            }
        }
        if (!routeFoundOnMap) {
            lblStatus.setText("Brak bezpośredniego odcinka trasy kolejowej: " + stacjaA.getNazwa() + " -> " + stacjaB.getNazwa());
            lblStatus.setTextFill(Color.ORANGE);
        }
    }

    void loadConnections() {
        if (mapView != null && mapView.getInitialized()) {
            allLines.forEach(mapView::removeCoordinateLine);
            allLines.clear();
            List<Stacja> stacje = Stacja.pobierzWszystkie();
            List<Polaczenie> polaczenia = Polaczenie.pobierzWszystkie();
            for (Polaczenie p : polaczenia) {
                Stacja s1 = byId(stacje, p.getStacja1Id());
                Stacja s2 = byId(stacje, p.getStacja2Id());
                if (s1 == null || s2 == null) continue;
                CoordinateLine cl = new CoordinateLine(coord(s1), coord(s2))
                        .setColor(Color.web("#444444")).setWidth(3).setVisible(true);
                mapView.addCoordinateLine(cl);
                allLines.add(cl);
            }
        }
    }

    private static Coordinate coord(Stacja s) {
        return new Coordinate(s.getSzerokoscGeograficzna(), s.getDlugoscGeograficzna());
    }

    private static Stacja byId(List<Stacja> list, int id) {
        return list.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }
}