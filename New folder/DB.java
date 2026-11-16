package com.percentagecalculator;

import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/percentage_calculator?useSSL=false&serverTimezone=Asia/Kolkata";
    private static final String USER = "root";
    private static final String PASSWORD = "0000"; // Change this

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String hash(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format("%02x", b));
        return out.toString();
    }

    public static void initialize() throws Exception {
        try (Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=Asia/Kolkata", USER, PASSWORD);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS percentage_calculator");
        }
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  username VARCHAR(60) UNIQUE NOT NULL,
                  password_hash CHAR(64) NOT NULL
                )
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS calculation_history (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  user_id INT NOT NULL,
                  module_name VARCHAR(100) NOT NULL,
                  input_values TEXT NOT NULL,
                  result TEXT NOT NULL,
                  calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT IGNORE INTO users(username,password_hash) VALUES(?,?)")) {
                p.setString(1, "admin");
                p.setString(2, hash("admin123"));
                p.executeUpdate();
            }
        }
    }

    public static int authenticate(String username, String password) throws Exception {
        String sql = "SELECT id FROM users WHERE username=? AND password_hash=?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, hash(password));
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    public static void saveHistory(int userId, String module, String inputs, String result) throws SQLException {
        String sql = "INSERT INTO calculation_history(user_id,module_name,input_values,result) VALUES(?,?,?,?)";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId); p.setString(2, module); p.setString(3, inputs); p.setString(4, result);
            p.executeUpdate();
        }
    }

    public static ResultSet history(int userId, Connection c) throws SQLException {
        PreparedStatement p = c.prepareStatement(
                "SELECT id,module_name,input_values,result,calculated_at FROM calculation_history WHERE user_id=? ORDER BY id DESC");
        p.setInt(1, userId);
        return p.executeQuery();
    }

    public static void clearHistory(int userId) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(
                "DELETE FROM calculation_history WHERE user_id=?")) {
            p.setInt(1, userId); p.executeUpdate();
        }
    }
}
