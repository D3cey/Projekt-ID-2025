package org.example;

import org.example.DbUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Trasa {

    /**
     * Dodaje nową trasę do bazy danych.
     * @param conn Połączenie bazodanowe (dla transakcji)
     * @param poczatkowaStacjaId ID stacji początkowej.
     * @return Wygenerowane ID nowej trasy.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public static int dodajTrase(Connection conn, int poczatkowaStacjaId) throws SQLException {
        String sql = "INSERT INTO trasa (poczatkowa_stacja_id) VALUES (?)";
        // Używamy Statement.RETURN_GENERATED_KEYS aby pobrać auto-wygenerowane ID
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, poczatkowaStacjaId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Nie udało się dodać trasy, żaden wiersz nie został zmodyfikowany.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Zwróć pierwsze auto-wygenerowane ID (kolumna "id")
                } else {
                    throw new SQLException("Nie udało się dodać trasy, brak zwróconego ID.");
                }
            }
        }
    }
}