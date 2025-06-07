package org.example.model;

import org.example.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Trasa {
    private int id;
    private int poczatkowaStacjaId;
    private String nazwaStacjiPoczatkowej;

    public Trasa(int id, int poczatkowaStacjaId, String nazwaStacjiPoczatkowej) {
        this.id = id;
        this.poczatkowaStacjaId = poczatkowaStacjaId;
        this.nazwaStacjiPoczatkowej = nazwaStacjiPoczatkowej;
    }

    public int getId() {
        return id;
    }

    public int getPoczatkowaStacjaId() {
        return poczatkowaStacjaId;
    }

    @Override
    public String toString() {
        return "Trasa ID: " + id + " (z: " + nazwaStacjiPoczatkowej + ")";
    }

    /**
     * Pobiera wszystkie trasy z bazy danych.
     *
     * @return Lista obiektów Trasa.
     */
    public static List<Trasa> pobierzWszystkie() {
        List<Trasa> trasy = new ArrayList<>();
        String sql = "SELECT t.id, t.poczatkowa_stacja_id, s.nazwa AS nazwa_stacji " +
                "FROM trasa t JOIN stacje s ON t.poczatkowa_stacja_id = s.id " +
                "ORDER BY t.id";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                trasy.add(new Trasa(
                        rs.getInt("id"),
                        rs.getInt("poczatkowa_stacja_id"),
                        rs.getString("nazwa_stacji")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania tras: " + e.getMessage());
            e.printStackTrace();
        }
        return trasy;
    }

    /**
     * Dodaje nową trasę (bez segmentów) do bazy danych w ramach istniejącej transakcji.
     *
     * @param conn               Połączenie bazodanowe zarządzane przez kontroler.
     * @param poczatkowaStacjaId ID stacji początkowej.
     * @return Wygenerowane ID nowej trasy.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public static int dodajTrase(Connection conn, int poczatkowaStacjaId) throws SQLException {
        String sql = "INSERT INTO trasa (poczatkowa_stacja_id) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, poczatkowaStacjaId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Nie udało się dodać trasy, żaden wiersz nie został zmodyfikowany.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Nie udało się dodać trasy, brak zwróconego ID.");
                }
            }
        }
    }
}