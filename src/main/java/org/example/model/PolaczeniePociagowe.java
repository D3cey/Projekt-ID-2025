package org.example.model;

import org.example.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolaczeniePociagowe {
    private final int id; // Corresponds to trasa.id
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

    // Getters
    public int getId() { return id; }
    public int getStacjaPoczatkowaId() { return stacjaPoczatkowaId; }
    public int getStacjaKoncowaId() { return stacjaKoncowaId; }
    public List<Polaczenie> getOdcinki() { return odcinki; }

    public static List<PolaczeniePociagowe> pobierzWszystkie() {
        List<PolaczeniePociagowe> wszystkieTrasy = new ArrayList<>();
        String sqlTrasy = "SELECT id, poczatkowa_stacja_id FROM trasa";
        // Segments are ordered by finding the next station in the path iteratively
        String sqlSegment = "SELECT stacja1_id, stacja2_id, polaczenia_miedzy_stacjami_id " +
                "FROM stacje_na_trasie WHERE trasa_id = ? AND stacja1_id = ?";

        try (Connection conn = DbUtil.getConnection();
             Statement stmtTrasy = conn.createStatement();
             ResultSet rsTrasy = stmtTrasy.executeQuery(sqlTrasy);
             PreparedStatement pstmtSegment = conn.prepareStatement(sqlSegment)) {

            while (rsTrasy.next()) {
                int trasaId = rsTrasy.getInt("id");
                int poczatkowaStacjaIdTrasy = rsTrasy.getInt("poczatkowa_stacja_id");

                List<Polaczenie> odcinkiCurrentTrasa = new ArrayList<>();
                int currentStationIdInPath = poczatkowaStacjaIdTrasy;
                int finalDestinationStationId = poczatkowaStacjaIdTrasy; // if no segments, it's just the start

                while (true) {
                    pstmtSegment.setInt(1, trasaId);
                    pstmtSegment.setInt(2, currentStationIdInPath);

                    try (ResultSet rsSegment = pstmtSegment.executeQuery()) {
                        if (rsSegment.next()) {
                            int segmentStacja1 = rsSegment.getInt("stacja1_id");
                            int segmentStacja2 = rsSegment.getInt("stacja2_id");
                            int pmsId = rsSegment.getInt("polaczenia_miedzy_stacjami_id");

                            // Create a Polaczenie object for this segment of the route
                            // The ID is pmsId, and stations define the directed segment
                            Polaczenie odcinek = new Polaczenie(pmsId, segmentStacja1, segmentStacja2);
                            odcinkiCurrentTrasa.add(odcinek);

                            currentStationIdInPath = segmentStacja2; // Move to the next station for the next iteration
                            finalDestinationStationId = segmentStacja2; // Update the final destination
                        } else {
                            // No more segments found for this trasa starting from currentStationIdInPath
                            break;
                        }
                    }
                }

                if (!odcinkiCurrentTrasa.isEmpty()) {
                    wszystkieTrasy.add(new PolaczeniePociagowe(trasaId, poczatkowaStacjaIdTrasy, finalDestinationStationId, odcinkiCurrentTrasa));
                } else {
                    // Handle trasy with no segments if necessary, or log a warning.
                    // For now, we only add if there are segments.
                    // A trasa must have at least one segment starting with poczatkowa_stacja_id due to the FK constraint:
                    // ALTER TABLE trasa ADD FOREIGN KEY (id, poczatkowa_stacja_id) REFERENCES stacje_na_trasie (trasa_id, stacja1_id);
                    // So, an empty odcinkiCurrentTrasa here might indicate a data issue or that the first segment itself is missing,
                    // which shouldn't happen with the FK.
                    // If a trasa only has one station (start and end are the same, no actual "polaczenie"),
                    // it might not be added here. The logic assumes a path of connections.
                    // Consider if a PolaczeniePociagowe can exist without any odcinki in your model.
                    // Based on the current model, a route with at least one segment makes sense.
                    System.out.println("Trasa ID " + trasaId + " has no segments or could not be fully processed.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Polaczenia Pociagowe: " + e.getMessage());
            e.printStackTrace();
        }
        return wszystkieTrasy;
    }

    public static PolaczeniePociagowe znajdzTrase(int a, int b) {
        // This method filters the list returned by the database-backed pobierzWszystkie()
        return pobierzWszystkie().stream()
                .filter(t -> (t.stacjaPoczatkowaId == a && t.stacjaKoncowaId == b) ||
                        (t.stacjaPoczatkowaId == b && t.stacjaKoncowaId == a)) // Considers reverse direction for the whole route
                .findFirst().orElse(null);
    }
}