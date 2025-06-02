package org.example.model;

import org.example.DbUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Stacja {
    private int id;
    private String nazwa;
    private double latitude;
    private double longitude;

    public Stacja(int id, String nazwa, double latitude, double longitude) {
        this.id = id;
        this.nazwa = nazwa;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public String getNazwa() { return nazwa; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() { return nazwa; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stacja stacja = (Stacja) o;
        return id == stacja.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    public static List<Stacja> pobierzWszystkie() {
        List<Stacja> lista = new ArrayList<>();
        String sql = "SELECT id, nazwa, szerokosc AS latitude, dlugosc AS longitude FROM stacje ORDER BY nazwa";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Stacja(
                        rs.getInt("id"),
                        rs.getString("nazwa"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static boolean dodajStacje(String nazwa, double latitude, double longitude) {
        String sql = "INSERT INTO stacje (nazwa, szerokosc, dlugosc) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nazwa);
            pstmt.setDouble(2, latitude);
            pstmt.setDouble(3, longitude);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding station: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                System.err.println("A station with these coordinates already exists or other unique constraint violated.");
            }
            e.printStackTrace();
            return false;
        }
    }

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
                        "): Stacja wykorzystywana jest w innych miejscach.");
            }
            e.printStackTrace();
            return false;
        }
    }
}