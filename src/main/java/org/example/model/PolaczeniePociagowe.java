package org.example.model;

import org.example.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolaczeniePociagowe {
    private final int id;
    private final int stacjaPoczatkowaId;
    private final int stacjaKoncowaId;
    private final List<Polaczenie> odcinki = new ArrayList<>();

    public PolaczeniePociagowe(int id, int stacjaPoczatkowaId, int stacjaKoncowaId, List<Polaczenie> odcinki) {
        this.id = id;
        this.stacjaPoczatkowaId = stacjaPoczatkowaId;
        this.stacjaKoncowaId = stacjaKoncowaId;
        if (odcinki != null) {
            this.odcinki.addAll(odcinki);
        }
    }

    public int getId() {
        return id;
    }

    public int getStacjaPoczatkowaId() {
        return stacjaPoczatkowaId;
    }

    public int getStacjaKoncowaId() {
        return stacjaKoncowaId;
    }

    public List<Polaczenie> getOdcinki() {
        return odcinki;
    }

    public static List<PolaczeniePociagowe> pobierzWszystkie() {
        List<PolaczeniePociagowe> wszystkieTrasy = new ArrayList<>();
        String sqlTrasy = "SELECT id, poczatkowa_stacja_id FROM trasa";

        String sqlSegment = "SELECT snt.stacja1_id, snt.stacja2_id, snt.polaczenia_miedzy_stacjami_id, pms.odleglosc " +
                "FROM stacje_na_trasie snt " +
                "JOIN polaczenia_miedzy_stacjami pms ON snt.polaczenia_miedzy_stacjami_id = pms.id " +
                "WHERE snt.trasa_id = ? AND snt.stacja1_id = ?";

        try (Connection conn = DbUtil.getConnection();
             Statement stmtTrasy = conn.createStatement();
             ResultSet rsTrasy = stmtTrasy.executeQuery(sqlTrasy);
             PreparedStatement pstmtSegment = conn.prepareStatement(sqlSegment)) {

            while (rsTrasy.next()) {
                int trasaId = rsTrasy.getInt("id");
                int poczatkowaStacjaIdTrasy = rsTrasy.getInt("poczatkowa_stacja_id");

                List<Polaczenie> odcinkiCurrentTrasa = new ArrayList<>();
                int currentStationIdInPath = poczatkowaStacjaIdTrasy;
                int finalDestinationStationId = poczatkowaStacjaIdTrasy;

                while (true) {
                    pstmtSegment.setInt(1, trasaId);
                    pstmtSegment.setInt(2, currentStationIdInPath);

                    try (ResultSet rsSegment = pstmtSegment.executeQuery()) {
                        if (rsSegment.next()) {
                            int segmentStacja1 = rsSegment.getInt("stacja1_id");
                            int segmentStacja2 = rsSegment.getInt("stacja2_id");
                            int pmsId = rsSegment.getInt("polaczenia_miedzy_stacjami_id");
                            double odlegloscSegmentu = rsSegment.getDouble("odleglosc");

                            Polaczenie odcinek = new Polaczenie(pmsId, segmentStacja1, segmentStacja2, odlegloscSegmentu);
                            odcinkiCurrentTrasa.add(odcinek);

                            currentStationIdInPath = segmentStacja2;
                            finalDestinationStationId = segmentStacja2;
                        } else {
                            break;
                        }
                    }
                }

                if (!odcinkiCurrentTrasa.isEmpty() || poczatkowaStacjaIdTrasy == finalDestinationStationId) {
                    if (!odcinkiCurrentTrasa.isEmpty()) {
                        wszystkieTrasy.add(new PolaczeniePociagowe(trasaId, poczatkowaStacjaIdTrasy, finalDestinationStationId, odcinkiCurrentTrasa));
                    } else if (poczatkowaStacjaIdTrasy != 0) {
                        System.out.println("Trasa ID " + trasaId + " nie ma segmentów lub nie mogła zostać w pełni przetworzona.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania Połączeń Pociągowych: " + e.getMessage());
            e.printStackTrace();
        }
        return wszystkieTrasy;
    }

    public static PolaczeniePociagowe znajdzTrase(int a, int b) {
        return pobierzWszystkie().stream()
                .filter(t -> (t.stacjaPoczatkowaId == a && t.stacjaKoncowaId == b) ||
                        (t.stacjaPoczatkowaId == b && t.stacjaKoncowaId == a))
                .findFirst().orElse(null);
    }
}