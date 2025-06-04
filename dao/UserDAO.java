package dao;

import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        System.out.println("DEBUG: Tentando registrar usuário: " + user.getUsername());
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.executeUpdate();
            System.out.println("DEBUG: Usuário " + user.getUsername() + " registrado com sucesso.");
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("ERRO: Usuário '" + user.getUsername() + "' já existe.");
                return false;
            }
            System.err.println("ERRO SQL ao registrar usuário:");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao registrar usuário:");
            e.printStackTrace();
            return false;
        }
    }

    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        System.out.println("DEBUG: Tentando login para usuário: " + username);
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User loggedInUser = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                    System.out.println("DEBUG: Login bem-sucedido para usuário: " + username + " (ID: " + loggedInUser.getId() + ")");
                    return loggedInUser;
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao tentar login:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao tentar login:");
            e.printStackTrace();
        }
        System.out.println("DEBUG: Login falhou para usuário: " + username);
        return null;
    }
}