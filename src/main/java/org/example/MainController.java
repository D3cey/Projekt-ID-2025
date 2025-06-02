package org.example;

import com.sothawo.mapjfx.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
// Removed Alert and ButtonType, as the confirmation is now inside DeleteStationDialogController
// However, Alert and ButtonType are still used by DeleteStationDialogController itself.
import javafx.scene.control.Button;
// Removed ComboBox import if no other ComboBox is directly managed by MainController for general selection
// import javafx.scene.control.ComboBox; // Keep if other ComboBoxes like comboDeleteStation existed here.
// Actually, we still need it for the method signature of byId if it's generic, but not for FXML fields.
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Polaczenie;
import org.example.model.PolaczeniePociagowe;
import org.example.model.Stacja;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// Removed Optional, as MainController no longer directly handles the Alert result for delete.


public class MainController {
    @FXML private Pane mapContainer;

    @FXML private Button btnShowAddStationDialog;
    @FXML private Button btnShowAddConnectionDialog;
    @FXML private Button btnShowFindRouteDialog;
    @FXML private Button btnShowDeleteStationDialog; // New FXML field for the dialog trigger

    // Removed: @FXML private ComboBox<Stacja> comboDeleteStation;
    // Removed: @FXML private Button           btnRemoveStation;

    @FXML private Label lblStatus;

    private MapView mapView;
    private final List<Marker> allMarkers = new ArrayList<>();
    private final List<CoordinateLine> allLines = new ArrayList<>();
    private final List<CoordinateLine> redLines = new ArrayList<>();

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

        // No need to populate comboDeleteStation here anymore

        mapView.initializedProperty().addListener((obs, oldReady, ready) -> {
            if (ready) {
                mapView.setMapType(MapType.OSM);
                mapView.setCenter(new Coordinate(52.2297, 21.0118));
                mapView.setZoom(6);
                refreshStationData(); // Now only updates map markers
                loadConnections();
            }
        });

        btnShowAddStationDialog.setOnAction(e -> showAddStationDialog());
        btnShowAddConnectionDialog.setOnAction(e -> showAddConnectionDialog());
        btnShowFindRouteDialog.setOnAction(e -> showFindRouteDialog());
        btnShowDeleteStationDialog.setOnAction(e -> showDeleteStationDialog()); // Set action
    }

    private void showDeleteStationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteStationDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Remove Station");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Stage ownerStage = (Stage) mapContainer.getScene().getWindow();
            dialogStage.initOwner(ownerStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            DeleteStationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setStations(Stacja.pobierzWszystkie()); // Pass current stations

            dialogStage.showAndWait(); // Show dialog and wait for it to close

            if (controller.isStationDeleted()) {
                refreshStationData(); // Update map markers
                loadConnections();    // Update connection lines
                lblStatus.setText("Station removed successfully.");
                lblStatus.setTextFill(Color.GREEN);
            } else {
                // Optional: display a message if cancelled, or just do nothing
                // lblStatus.setText("Station removal cancelled or failed within dialog.");
                // lblStatus.setTextFill(Color.ORANGE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText("Error opening remove station dialog: " + e.getMessage());
            lblStatus.setTextFill(Color.RED);
        } catch (NullPointerException e) {
            e.printStackTrace(); // Catch if mapContainer.getScene() is null early on
            lblStatus.setText("Error setting up remove station dialog. Main window not ready?");
            lblStatus.setTextFill(Color.RED);
        }
    }

    // Removed handleRemoveStation() method, its logic is now within DeleteStationDialogController

    public void refreshStationData() {
        List<Stacja> stacje = Stacja.pobierzWszystkie();

        // No longer need to update comboDeleteStation here

        // Update map markers
        if (mapView != null && mapView.getInitialized()) {
            allMarkers.forEach(mapView::removeMarker);
            allMarkers.clear();

            for (Stacja s : stacje) {
                Marker m = Marker.createProvided(Marker.Provided.BLUE)
                        .setPosition(coord(s))
                        .setVisible(true);
                m.attachLabel(new MapLabel(s.getNazwa(), 10, -10)
                        .setCssClass("map-label-station")
                        .setVisible(true));
                mapView.addMarker(m);
                allMarkers.add(m);
            }
        }
    }

    // showAddStationDialog, showAddConnectionDialog, showFindRouteDialog,
    // findAndDisplayRoute, loadConnections, coord, byId methods remain as previously defined.
    // Make sure they are complete from previous steps.

    // --- Ensure these methods are present and complete from previous steps ---
    private void showAddStationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddStationDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Station");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            AddStationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.isStationAdded()) {
                refreshStationData();
                lblStatus.setText("New station added successfully.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) { e.printStackTrace(); lblStatus.setText("Error: " + e.getMessage());}
    }

    private void showAddConnectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddConnectionDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) mapContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            AddConnectionDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setStations(Stacja.pobierzWszystkie());
            dialogStage.showAndWait();
            if (controller.isConnectionAdded()) {
                loadConnections();
                lblStatus.setText("New connection added successfully.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) { e.printStackTrace(); lblStatus.setText("Error: " + e.getMessage());}
    }

    private void showFindRouteDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FindRouteDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Find Route");
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
                lblStatus.setText("Route finding cancelled or stations not selected.");
                lblStatus.setTextFill(Color.ORANGE);
            }
        } catch (Exception e) { e.printStackTrace(); lblStatus.setText("Error: " + e.getMessage());}
    }

    private void findAndDisplayRoute(Stacja stacjaA, Stacja stacjaB) {
        if (stacjaA == null || stacjaB == null) {
            lblStatus.setText("Stations for route finding are not properly selected.");
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
                        (stacjaA.getLatitude() + stacjaB.getLatitude()) / 2.0,
                        (stacjaA.getLongitude() + stacjaB.getLongitude()) / 2.0));
                routeFoundOnMap = true;
                lblStatus.setText("Route displayed: " + stacjaA.getNazwa() + " -> " + stacjaB.getNazwa());
                lblStatus.setTextFill(Color.GREEN);
                break;
            }
        }
        if (!routeFoundOnMap) {
            lblStatus.setText("No direct train route segment: " + stacjaA.getNazwa() + " -> " + stacjaB.getNazwa());
            lblStatus.setTextFill(Color.ORANGE);
        }
    }

    private void loadConnections() {
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
        return new Coordinate(s.getLatitude(), s.getLongitude());
    }

    private static Stacja byId(List<Stacja> list, int id) {
        return list.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }
}