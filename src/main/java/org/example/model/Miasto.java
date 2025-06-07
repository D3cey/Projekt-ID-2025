package org.example.model;

import org.example.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Miasto {
    private int id;
    private String nazwa;
    private double szerokosc;
    private double dlugosc;
    private double powierzchnia;

    public Miasto(int id, String nazwa, double szerokosc, double dlugosc, double powierzchnia) {
        this.id = id;
        this.nazwa = nazwa;
        this.szerokosc = szerokosc;
        this.dlugosc = dlugosc;
        this.powierzchnia = powierzchnia;
    }

    public int getId() {
        return id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public double getSzerokosc() {
        return szerokosc;
    }

    public double getDlugosc() {
        return dlugosc;
    }

    public double getPowierzchnia() {
        return powierzchnia;
    }

    /**
     * Pobiera wszystkie miasta z bazy danych.
     *
     * @return Lista obiektów Miasto.
     */
    public static List<Miasto> pobierzWszystkie() {
        List<Miasto> miasta = new ArrayList<>();
        String sql = "SELECT id, nazwa, szerokosc, dlugosc, powierzchnia FROM miasta ORDER BY powierzchnia DESC";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                miasta.add(new Miasto(
                        rs.getInt("id"),
                        rs.getString("nazwa"),
                        rs.getDouble("szerokosc"),
                        rs.getDouble("dlugosc"),
                        rs.getDouble("powierzchnia")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania Miast: " + e.getMessage());
            e.printStackTrace();
        }
        return miasta;
    }
}