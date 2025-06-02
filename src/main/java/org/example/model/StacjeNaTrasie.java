package org.example;

import org.example.DbUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StacjeNaTrasie {
    // Klasa pomocnicza do przekazywania danych segmentu
    public static class SegmentTrasyInput {
        public int stacja1Id;
        public int stacja2Id;
        public int polaczeniaMiedzyStacjamiId;

        public SegmentTrasyInput(int stacja1Id, int stacja2Id, int polaczeniaMiedzyStacjamiId) {
            this.stacja1Id = stacja1Id;
            this.stacja2Id = stacja2Id;
            this.polaczeniaMiedzyStacjamiId = polaczeniaMiedzyStacjamiId;
        }
    }

    /**
     * Dodaje segment (odcinek) do określonej trasy w bazie danych.
     * @param conn Połączenie bazodanowe (dla transakcji)
     * @param trasaId ID trasy, do której należy segment.
     * @param stacja1Id ID pierwszej stacji segmentu.
     * @param stacja2Id ID drugiej stacji segmentu.
     * @param polaczeniaMiedzyStacjamiId ID połączenia między stacjami reprezentującego ten segment.
     * @return true jeśli segment został dodany pomyślnie, false w przeciwnym razie.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public static boolean dodajSegmentDoTrasy(Connection conn, int trasaId, int stacja1Id, int stacja2Id, int polaczeniaMiedzyStacjamiId) throws SQLException {
        // Tabela: stacje_na_trasie
        // Kolumny: trasa_id, stacja1_id, stacja2_id, polaczenia_miedzy_stacjami_id, tor1, tor2
        // tor1 i tor2 są ignorowane (domyślnie NULL, jeśli tak pozwala schemat)
        String sql = "INSERT INTO stacje_na_trasie (trasa_id, stacja1_id, stacja2_id, polaczenia_miedzy_stacjami_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trasaId);
            pstmt.setInt(2, stacja1Id);
            pstmt.setInt(3, stacja2Id);
            pstmt.setInt(4, polaczeniaMiedzyStacjamiId);

            return pstmt.executeUpdate() > 0;
        }
    }
}