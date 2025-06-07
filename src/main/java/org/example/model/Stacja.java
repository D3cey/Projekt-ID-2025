package org.example.model;

import org.example.util.DbUtil;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Stacja {
    private int id;
    private String nazwa;
    private double szerokoscGeograficzna;
    private double dlugoscGeograficzna;
    private double powierzchniaMiasta;

    private static class SegmentData {
        final int stacja2Id;
        final boolean zatrzymujeSie;

        SegmentData(int stacja2Id, boolean zatrzymujeSie) {
            this.stacja2Id = stacja2Id;
            this.zatrzymujeSie = zatrzymujeSie;
        }
    }

    public Stacja(int id, String nazwa, double szerokoscGeograficzna, double dlugoscGeograficzna, double powierzchniaMiasta) {
        this.id = id;
        this.nazwa = nazwa;
        this.szerokoscGeograficzna = szerokoscGeograficzna;
        this.dlugoscGeograficzna = dlugoscGeograficzna;
        this.powierzchniaMiasta = powierzchniaMiasta;
    }

    public int getId() { return id; }
    public String getNazwa() { return nazwa; }
    public double getSzerokoscGeograficzna() { return szerokoscGeograficzna; }
    public double getDlugoscGeograficzna() { return dlugoscGeograficzna; }
    public double getPowierzchniaMiasta() { return powierzchniaMiasta; }
    @Override public String toString() { return nazwa; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Stacja stacja = (Stacja) o; return id == stacja.id; }
    @Override public int hashCode() { return Objects.hash(id); }

    /**
     * Pobiera wszystkie stacje z bazy danych wraz z powierzchnią miasta, w którym się znajdują.
     *
     * @return Lista wszystkich obiektów Stacja.
     */
    public static List<Stacja> pobierzWszystkie() {
        List<Stacja> lista = new ArrayList<>();

        String sql = "SELECT s.id, s.nazwa, s.szerokosc, s.dlugosc, COALESCE(m.powierzchnia, 0) AS powierzchnia_miasta " +
                "FROM stacje s " +
                "LEFT JOIN miasta m ON s.miasto = m.id " +
                "ORDER BY s.nazwa";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Stacja(
                        rs.getInt("id"),
                        rs.getString("nazwa"),
                        rs.getDouble("szerokosc"),
                        rs.getDouble("dlugosc"),
                        rs.getDouble("powierzchnia_miasta")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania Stacji: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Dodaje nową stację do bazy danych.
     */
    public static boolean dodajStacje(String nazwa, double szerokoscGeograficzna, double dlugoscGeograficzna) {
        String sql = "INSERT INTO stacje (nazwa, szerokosc, dlugosc) VALUES (?, ?, ?)";

        String executableSql = sql
                .replaceFirst("\\?", "'" + nazwa.replace("'", "''") + "'")
                .replaceFirst("\\?", String.valueOf(szerokoscGeograficzna))
                .replaceFirst("\\?", String.valueOf(dlugoscGeograficzna));
        System.out.println(executableSql + ";\n");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nazwa);
            pstmt.setDouble(2, szerokoscGeograficzna);
            pstmt.setDouble(3, dlugoscGeograficzna);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania stacji: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Stacja o podanych współrzędnych już istnieje lub naruszono inny unikalny indeks.");
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Usuwa stację z bazy danych na podstawie jej ID.
     */
    public static boolean usunStacje(int stacjaId) {
        String sql = "DELETE FROM stacje WHERE id = ?";

        String executableSql = sql.replaceFirst("\\?", String.valueOf(stacjaId));
        System.out.println(executableSql + ";\n");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stacjaId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Błąd podczas usuwania stacji (ID: " + stacjaId + "): " + e.getMessage());
            if ("23503".equals(e.getSQLState())) {
                System.err.println("Nie można usunąć stacji (ID: " + stacjaId +
                        "): Stacja wykorzystywana jest w innych miejscach (np. w innych tabelach).");
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pobiera listę stacji dla określonej trasy w poprawnej kolejności.
     */
    public static List<StacjaNaTrasieWrapper> pobierzStacjeDlaTrasy(int trasaId) {
        List<StacjaNaTrasieWrapper> uporzadkowaneStacje = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection()) {
            int poczatkowaStacjaId = -1;
            String sqlTrasa = "SELECT poczatkowa_stacja_id FROM trasa WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlTrasa)) {
                pstmt.setInt(1, trasaId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    poczatkowaStacjaId = rs.getInt("poczatkowa_stacja_id");
                } else {
                    System.err.println("Nie znaleziono trasy o ID: " + trasaId);
                    return uporzadkowaneStacje;
                }
            }

            Map<Integer, SegmentData> segmentyMap = new HashMap<>();
            String sqlSegmenty = "SELECT stacja1_id, stacja2_id, zatrumujesia FROM stacje_na_trasie WHERE trasa_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlSegmenty)) {
                pstmt.setInt(1, trasaId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    segmentyMap.put(rs.getInt("stacja1_id"), new SegmentData(rs.getInt("stacja2_id"), rs.getBoolean("zatrumujesia")));
                }
            }

            // Krok 3: Zbuduj uporządkowaną listę ID stacji (logika bez zmian)
            List<Integer> uporzadkowaneIdStacji = new ArrayList<>();
            Map<Integer, Boolean> statusyZatrzyman = new HashMap<>();
            if (poczatkowaStacjaId != -1) {
                int aktualneIdStacji = poczatkowaStacjaId;
                while (true) {
                    uporzadkowaneIdStacji.add(aktualneIdStacji);
                    SegmentData nastepnySegment = segmentyMap.get(aktualneIdStacji);
                    if (nastepnySegment != null) {
                        statusyZatrzyman.put(aktualneIdStacji, nastepnySegment.zatrzymujeSie);
                        aktualneIdStacji = nastepnySegment.stacja2Id;
                    } else {
                        statusyZatrzyman.put(aktualneIdStacji, true);
                        break;
                    }
                }
            }

            if (uporzadkowaneIdStacji.isEmpty()) {
                return uporzadkowaneStacje;
            }

            // Krok 4: Pobierz wszystkie potrzebne obiekty Stacja
            Map<Integer, Stacja> stacjeMap = new HashMap<>();
            String inClause = uporzadkowaneIdStacji.stream().map(String::valueOf).collect(Collectors.joining(","));
            String sqlStacje = "SELECT id, nazwa, szerokosc, dlugosc, miasto FROM stacje WHERE id IN (" + inClause + ")";

            System.out.println(sqlStacje + ";\n");

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlStacje)) {
                while (rs.next()) {
                    stacjeMap.put(rs.getInt("id"), new Stacja(
                            rs.getInt("id"), rs.getString("nazwa"),
                            rs.getDouble("szerokosc"), rs.getDouble("dlugosc"), 0.0
                    ));
                }
            }

            for (Integer idStacji : uporzadkowaneIdStacji) {
                Stacja stacja = stacjeMap.get(idStacji);
                boolean zatrzymujeSie = statusyZatrzyman.getOrDefault(idStacji, true);
                if (stacja != null) {
                    uporzadkowaneStacje.add(new StacjaNaTrasieWrapper(stacja, trasaId, zatrzymujeSie));
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania stacji dla trasy " + trasaId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return uporzadkowaneStacje;
    }

    /**
     * Aktualizuje status postoju dla danej stacji na danej trasie.
     */
    public static boolean zaktualizujStatusZatrzymania(int trasaId, int stacjaId, boolean zatrzymujeSie) {
        String sql = "UPDATE stacje_na_trasie SET zatrumujesia = ? WHERE trasa_id = ? AND stacja1_id = ?";

        String executableSql = sql
                .replaceFirst("\\?", String.valueOf(zatrzymujeSie).toUpperCase())
                .replaceFirst("\\?", String.valueOf(trasaId))
                .replaceFirst("\\?", String.valueOf(stacjaId));
        System.out.println(executableSql + ";\n");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, zatrzymujeSie);
            pstmt.setInt(2, trasaId);
            pstmt.setInt(3, stacjaId);
//            return pstmt.executeUpdate() > 0;
            return true;
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji statusu postoju: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dodaje pojedynczy segment do trasy w ramach istniejącej transakcji.
     */
    public static boolean dodajSegmentDoTrasy(Connection conn, int trasaId, int stacja1Id, int stacja2Id, int polaczeniaMiedzyStacjamiId, boolean zatrzymujeSie) throws SQLException {
        String sql = "INSERT INTO stacje_na_trasie (trasa_id, stacja1_id, stacja2_id, polaczenia_miedzy_stacjami_id, zatrumujesia) VALUES (?, ?, ?, ?, ?)";

        String executableSql = sql
                .replaceFirst("\\?", String.valueOf(trasaId))
                .replaceFirst("\\?", String.valueOf(stacja1Id))
                .replaceFirst("\\?", String.valueOf(stacja2Id))
                .replaceFirst("\\?", String.valueOf(polaczeniaMiedzyStacjamiId))
                .replaceFirst("\\?", String.valueOf(zatrzymujeSie).toUpperCase());
        System.out.println(executableSql + ";\n");

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trasaId);
            pstmt.setInt(2, stacja1Id);
            pstmt.setInt(3, stacja2Id);
            pstmt.setInt(4, polaczeniaMiedzyStacjamiId);
            pstmt.setBoolean(5, zatrzymujeSie);
            return pstmt.executeUpdate() > 0;
        }
    }
}