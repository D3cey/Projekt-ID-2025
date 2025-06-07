package org.example.model;

import org.example.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Stacja {
    private int id;
    private String nazwa;
    private double szerokoscGeograficzna;
    private double dlugoscGeograficzna;
    private double powierzchniaMiasta;

    public Stacja(int id, String nazwa, double szerokoscGeograficzna, double dlugoscGeograficzna, double powierzchniaMiasta) {
        this.id = id;
        this.nazwa = nazwa;
        this.szerokoscGeograficzna = szerokoscGeograficzna;
        this.dlugoscGeograficzna = dlugoscGeograficzna;
        this.powierzchniaMiasta = powierzchniaMiasta;
    }

    public int getId() {
        return id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public double getSzerokoscGeograficzna() {
        return szerokoscGeograficzna;
    }

    public double getDlugoscGeograficzna() {
        return dlugoscGeograficzna;
    }

    public double getPowierzchniaMiasta() {
        return powierzchniaMiasta;
    }

    @Override
    public String toString() {
        return nazwa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stacja stacja = (Stacja) o;
        return id == stacja.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
     *
     * @param nazwa                 Nazwa stacji.
     * @param szerokoscGeograficzna Szerokość geograficzna stacji.
     * @param dlugoscGeograficzna   Długość geograficzna stacji.
     * @return true jeśli stacja została dodana pomyślnie, false w przeciwnym razie.
     */
    public static boolean dodajStacje(String nazwa, double szerokoscGeograficzna, double dlugoscGeograficzna) {
        String sql = "INSERT INTO stacje (nazwa, szerokosc, dlugosc) VALUES (?, ?, ?)";
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
     *
     * @param stacjaId ID stacji do usunięcia.
     * @return true jeśli stacja została usunięta pomyślnie, false w przeciwnym razie.
     */
    public static boolean usunStacje(int stacjaId) {
        String sql = "DELETE FROM stacje WHERE id = ?";
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
     * Pobiera listę stacji dla określonej trasy, włącznie ze statusem postoju.
     *
     * @param trasaId ID trasy do sprawdzenia.
     * @return Lista obiektów StacjaNaTrasieWrapper.
     */
    public static List<StacjaNaTrasieWrapper> pobierzStacjeDlaTrasy(int trasaId) {
        List<StacjaNaTrasieWrapper> stacjeNaTrasie = new ArrayList<>();

        String sql = "SELECT s.id, s.nazwa, s.szerokosc, s.dlugosc, s.miasto, snt.zatrumujesia " +
                "FROM stacje_na_trasie snt " +
                "JOIN stacje s ON snt.stacja1_id = s.id " +
                "WHERE snt.trasa_id = ? " +
                "ORDER BY snt.stacja1_id";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trasaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Stacja stacja = new Stacja(
                        rs.getInt("id"),
                        rs.getString("nazwa"),
                        rs.getDouble("szerokosc"),
                        rs.getDouble("dlugosc"),
                        rs.getInt("miasto")
                );
                stacjeNaTrasie.add(new StacjaNaTrasieWrapper(
                        stacja,
                        trasaId,
                        rs.getBoolean("zatrumujesia")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania stacji dla trasy " + trasaId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return stacjeNaTrasie;
    }

    /**
     * Aktualizuje status postoju dla danej stacji na danej trasie.
     *
     * @param trasaId       ID trasy.
     * @param stacjaId      ID stacji (w tabeli stacje_na_trasie jest to stacja1_id).
     * @param zatrzymujeSie Nowy status postoju (true/false).
     * @return true jeśli aktualizacja się powiodła, false w przeciwnym razie.
     */
    public static boolean zaktualizujStatusZatrzymania(int trasaId, int stacjaId, boolean zatrzymujeSie) {
        String sql = "UPDATE stacje_na_trasie SET zatrumujesia = ? WHERE trasa_id = ? AND stacja1_id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, zatrzymujeSie);
            pstmt.setInt(2, trasaId);
            pstmt.setInt(3, stacjaId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji statusu postoju: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dodaje pojedynczy segment do trasy w ramach istniejącej transakcji.
     *
     * @param conn                       Połączenie bazodanowe zarządzane przez kontroler.
     * @param trasaId                    ID trasy.
     * @param stacja1Id                  ID stacji początkowej segmentu.
     * @param stacja2Id                  ID stacji końcowej segmentu.
     * @param polaczeniaMiedzyStacjamiId ID połączenia między stacjami.
     * @param zatrzymujeSie              Czy pociąg zatrzymuje się na stacji początkowej tego segmentu.
     * @return true jeśli operacja się powiodła.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public static boolean dodajSegmentDoTrasy(Connection conn, int trasaId, int stacja1Id, int stacja2Id, int polaczeniaMiedzyStacjamiId, boolean zatrzymujeSie) throws SQLException {
        String sql = "INSERT INTO stacje_na_trasie (trasa_id, stacja1_id, stacja2_id, polaczenia_miedzy_stacjami_id, zatrumujesia) VALUES (?, ?, ?, ?, ?)";
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