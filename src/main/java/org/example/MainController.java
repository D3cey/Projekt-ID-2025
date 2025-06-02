package org.example;

import com.sothawo.mapjfx.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.example.model.Polaczenie;
import org.example.model.PolaczeniePociagowe;
import org.example.model.Stacja;

import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML private Pane              mapContainer;
    @FXML private ComboBox<Stacja>  comboStacjaA;
    @FXML private ComboBox<Stacja>  comboStacjaB;
    @FXML private Button            btnZnajdz;

    private MapView mapView;

    private final List<Marker>         allMarkers = new ArrayList<>();
    private final List<CoordinateLine> allLines   = new ArrayList<>();
    private final List<CoordinateLine> redLines = new ArrayList<>();
    private CoordinateLine highlight;

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
        List<Stacja>     stacje     = Stacja.pobierzWszystkie();
        List<Polaczenie> polaczenia = Polaczenie.pobierzWszystkie();
        List<PolaczeniePociagowe> pociagowe= PolaczeniePociagowe.pobierzWszystkie();
        comboStacjaA.getItems().addAll(stacje);
        comboStacjaB.getItems().addAll(stacje);

        mapView.initializedProperty().addListener((obs, oldReady, ready) -> {
            if (!ready) return;

            mapView.setMapType(MapType.OSM);
            mapView.setCenter(new Coordinate(52.2297, 21.0118));
            mapView.setZoom(6);

            for (Polaczenie p : polaczenia) {
                Stacja s1 = byId(stacje, p.getStacja1Id());
                Stacja s2 = byId(stacje, p.getStacja2Id());
                if (s1 == null || s2 == null) continue;

                CoordinateLine cl = new CoordinateLine(
                        coord(s1), coord(s2))
                        .setColor(Color.web("#444444"))
                        .setWidth(5)
                        .setVisible(true);

                mapView.addCoordinateLine(cl);
                allLines.add(cl);
            }

            for (Stacja s : stacje) {
                Marker m = Marker.createProvided(Marker.Provided.BLUE)
                        .setPosition(coord(s))
                        .setVisible(true);
                mapView.addMarker(m);
                allMarkers.add(m);
            }
        });

        btnZnajdz.setOnAction(e -> {
            Stacja a = comboStacjaA.getValue();
            Stacja b = comboStacjaB.getValue();
            if (a == null || b == null) return;

            redLines.forEach(mapView::removeCoordinateLine);
            redLines.clear();

            for (PolaczeniePociagowe pp : pociagowe) {

                List<Integer> ids = new ArrayList<>();
                ids.add(pp.getOdcinki().get(0).getStacja1Id());
                for(Polaczenie p :  pp.getOdcinki()) {
                    ids.add(p.getStacja2Id());
                }

                int start = ids.indexOf(a.getId());
                int end = ids.indexOf(b.getId());
                if (start == -1 || end== -1 || start >= end) continue;



                for (int i = start; i < end; i++) {
                    Polaczenie seg = pp.getOdcinki().get(i);
                    Stacja s1 = byId(stacje, seg.getStacja1Id());
                    Stacja s2 = byId(stacje, seg.getStacja2Id());

                    CoordinateLine r = new CoordinateLine(coord(s1), coord(s2))
                            .setColor(Color.RED)
                            .setWidth(7)
                            .setVisible(true);

                    mapView.addCoordinateLine(r);
                    redLines.add(r);
                }

                mapView.setCenter(new Coordinate(
                        (a.getLatitude() + b.getLatitude()) / 2.0,
                        (a.getLongitude() + b.getLongitude()) / 2.0));
                break;
            }
        });
    }


    private static Coordinate coord(Stacja s) {
        return new Coordinate(s.getLatitude(), s.getLongitude());
    }
    private static Stacja byId(List<Stacja> list, int id) {
        return list.stream().filter(x -> x.getId()==id).findFirst().orElse(null);
    }
}
