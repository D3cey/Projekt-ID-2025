package org.example.model;

import org.example.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public int getId() {
        return id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return nazwa;
    }

    public static List<Stacja> pobierzWszystkie() {
        List<Stacja> lista = new ArrayList<>();
        /*try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nazwa, latitude, longitude FROM stacje")) {
            while (rs.next()) {
                lista.add(new Stacja(
                        rs.getInt("id"),
                        rs.getString("nazwa"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        lista.add(new Stacja(1, "Warszawa", 52.2297, 21.0118));
        lista.add(new Stacja(2, "Kraków", 50.0647, 19.9372));
        lista.add(new Stacja(3, "Gdańsk", 54.3520, 18.6466));
        lista.add(new Stacja(4, "Wrocław", 51.1079, 17.0385));
        return lista;
    }
}
