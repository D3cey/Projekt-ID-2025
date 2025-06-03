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

    public Stacja(int id, String nazwa, double szerokoscGeograficzna, double dlugoscGeograficzna) {
        this.id = id;
        this.nazwa = nazwa;
        this.szerokoscGeograficzna = szerokoscGeograficzna;
        this.dlugoscGeograficzna = dlugoscGeograficzna;
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
     * Pobiera wszystkie stacje z bazy danych.
     *
     * @return Lista obiektów Stacja.
     */
    public static List<Stacja> pobierzWszystkie() {
        List<Stacja> listaStacji = new ArrayList<>(); // Zmieniono nazwę zmiennej
        String sql = "SELECT id, nazwa, szerokosc AS szerokoscGeograficzna, dlugosc AS dlugoscGeograficzna FROM stacje ORDER BY nazwa";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaStacji.add(new Stacja(
                        rs.getInt("id"),
                        rs.getString("nazwa"),
                        rs.getDouble("szerokoscGeograficzna"),
                        rs.getDouble("dlugoscGeograficzna")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Wystąpił błąd podczas pobierania stacji: " + e.getMessage());
            e.printStackTrace();
        }
        return listaStacji;
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
}