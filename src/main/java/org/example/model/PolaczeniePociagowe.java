package org.example.model;

import org.example.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolaczeniePociagowe {
    private final int id; // Odpowiada trasa.id
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

    // Gettery
    public int getId() { return id; }
    public int getStacjaPoczatkowaId() { return stacjaPoczatkowaId; }
    public int getStacjaKoncowaId() { return stacjaKoncowaId; }
    public List<Polaczenie> getOdcinki() { return odcinki; }

    public static List<PolaczeniePociagowe> pobierzWszystkie() {
        List<PolaczeniePociagowe> wszystkieTrasy = new ArrayList<>();
        String sqlTrasy = "SELECT id, poczatkowa_stacja_id FROM trasa";

        // Zmodyfikowane zapytanie SQL, aby dołączyć do polaczenia_miedzy_stacjami i pobrać odleglosc
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
                            double odlegloscSegmentu = rsSegment.getDouble("odleglosc"); // Pobranie odległości

                            // Użycie nowego, czteroargumentowego konstruktora Polaczenie
                            Polaczenie odcinek = new Polaczenie(pmsId, segmentStacja1, segmentStacja2, odlegloscSegmentu);
                            odcinkiCurrentTrasa.add(odcinek);

                            currentStationIdInPath = segmentStacja2;
                            finalDestinationStationId = segmentStacja2;
                        } else {
                            break; // Koniec segmentów dla tej trasy
                        }
                    }
                }

                // Dodaj trasę tylko jeśli ma segmenty (zgodnie z logiką z poprzedniej wersji)
                // LUB jeśli trasa może istnieć bez segmentów (np. tylko stacja początkowa i końcowa są takie same)
                // W obecnej logice, jeśli odcinkiCurrentTrasa jest puste, trasa nie jest dodawana.
                // Jeśli chcesz dodać trasę nawet bez segmentów (co może być rzadkie), musisz zmienić ten warunek.
                if (!odcinkiCurrentTrasa.isEmpty() || poczatkowaStacjaIdTrasy == finalDestinationStationId) { // Możliwa modyfikacja warunku
                    // Jeśli trasa może mieć tylko jedną stację, a stacja końcowa nie została zaktualizowana
                    // (bo nie było segmentów), to stacja końcowa będzie taka sama jak początkowa.
                    // Twój oryginalny komentarz sugerował, że trasa bez segmentów nie jest dodawana.
                    // Dla spójności zostawiam !odcinkiCurrentTrasa.isEmpty()
                    if (!odcinkiCurrentTrasa.isEmpty()) {
                        wszystkieTrasy.add(new PolaczeniePociagowe(trasaId, poczatkowaStacjaIdTrasy, finalDestinationStationId, odcinkiCurrentTrasa));
                    } else if (poczatkowaStacjaIdTrasy != 0) { // Dodaj trasę "punktową" jeśli ma sens w systemie
                        // Jeśli chcesz reprezentować trasy, które są tylko pojedynczym punktem (stacją),
                        // możesz dodać je tutaj z pustą listą odcinków.
                        // Na przykład:
                        // wszystkieTrasy.add(new PolaczeniePociagowe(trasaId, poczatkowaStacjaIdTrasy, poczatkowaStacjaIdTrasy, new ArrayList<>()));
                        // System.out.println("Trasa ID " + trasaId + " nie ma segmentów, ale została przetworzona jako trasa punktowa (jeśli zaimplementowano).");
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