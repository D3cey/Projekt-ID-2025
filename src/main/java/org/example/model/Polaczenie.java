package org.example.model;

import org.example.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Polaczenie {
    private final int id;
    private final int stacja1Id;
    private final int stacja2Id;
    private final double odleglosc;

    public Polaczenie(int id, int stacja1Id, int stacja2Id, double odleglosc) {
        this.id = id;
        this.stacja1Id = stacja1Id;
        this.stacja2Id = stacja2Id;
        this.odleglosc = odleglosc;
    }

    public int getId() {
        return id;
    }

    public int getStacja1Id() {
        return stacja1Id;
    }

    public int getStacja2Id() {
        return stacja2Id;
    }

    public double getOdleglosc() {
        return odleglosc;
    }

    public static List<Polaczenie> pobierzWszystkie() {
        List<Polaczenie> lista = new ArrayList<>();

        String sql = "SELECT id, stacja1_id, stacja2_id, odleglosc FROM polaczenia_miedzy_stacjami";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Polaczenie(
                        rs.getInt("id"),
                        rs.getInt("stacja1_id"),
                        rs.getInt("stacja2_id"),
                        rs.getDouble("odleglosc")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania Połączeń: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public static Polaczenie znajdzBezposrednie(int stacjaAId, int stacjaBId) {
        List<Polaczenie> wszystkie = pobierzWszystkie();
        for (Polaczenie p : wszystkie) {
            if ((p.getStacja1Id() == stacjaAId && p.getStacja2Id() == stacjaBId) ||
                    (p.getStacja1Id() == stacjaBId && p.getStacja2Id() == stacjaAId)) {
                return p;
            }
        }
        return null;
    }

    public static boolean dodajPolaczenie(int stacja1Id, int stacja2Id, double odleglosc) {
        String sql = "INSERT INTO polaczenia_miedzy_stacjami (stacja1_id, stacja2_id, odleglosc) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stacja1Id);
            pstmt.setInt(2, stacja2Id);
            pstmt.setDouble(3, odleglosc);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Błąd dodawania połączenia: " + e.getMessage());
            if ("23503".equals(e.getSQLState())) {
                System.err.println("DB Błąd: Jeden lub oba ID stacji dla połączenia nie istnieją w tabeli 'stacje'.");
            }
            e.printStackTrace();
            return false;
        }
    }
}