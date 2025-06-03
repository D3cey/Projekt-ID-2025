package org.example.model;

import org.example.util.DbUtil;
import org.example.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private String login;
    private boolean isAdmin;
    private Integer klientId;

    public User(String login, boolean isAdmin, Integer klientId) {
        this.login = login;
        this.isAdmin = isAdmin;
        this.klientId = klientId;
    }

    public String getLogin() {
        return login;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public Integer getKlientId() {
        return klientId;
    }

    public static User authenticate(String login, String plainPassword) {
        if (login == null || login.trim().isEmpty() || plainPassword == null) {
            return null;
        }

        String sql = "SELECT login, haslo, admin, klient_id FROM users WHERE login = ?";
        String hashedPasswordFromInput = PasswordUtil.hashPassword(plainPassword);

        if (hashedPasswordFromInput == null) {
            System.err.println("Nie udało się zahashować podanego hasła.");
            return null;
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbHashedPassword = rs.getString("haslo");
                    boolean isAdmin = rs.getBoolean("admin");
                    Integer klientId = rs.getObject("klient_id") != null ? rs.getInt("klient_id") : null;

                    if (hashedPasswordFromInput.equals(dbHashedPassword)) {
                        // Hasła się zgadzają
                        return new User(login, isAdmin, klientId);
                    } else {
                        // Hasło nie pasuje
                        System.out.println("Logowanie nieudane: Nieprawidłowe hasło dla użytkownika " + login);
                        return null;
                    }
                } else {
                    // Użytkownik nie istnieje
                    System.out.println("Logowanie nieudane: Użytkownik " + login + " nie istnieje.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd SQL podczas próby uwierzytelnienia użytkownika " + login + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", isAdmin=" + isAdmin +
                ", klientId=" + klientId +
                '}';
    }
}