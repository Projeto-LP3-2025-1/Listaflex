package dao;

import model.UserList;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserListDAO {

    public int inserir(UserList userList) {
        String sql = "INSERT INTO user_lists (user_id, list_name, list_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userList.getUserId());
            stmt.setString(2, userList.getListName());
            stmt.setString(3, userList.getListType());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userList.setId(generatedKeys.getInt(1));
                        return userList.getId();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao inserir UserList:");
            e.printStackTrace();
        }
        return -1; // Retorna -1 se a inserção falhar
    }

    public List<UserList> listarPorUsuario(int userId) {
        List<UserList> userLists = new ArrayList<>();
        String sql = "SELECT * FROM user_lists WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userLists.add(new UserList(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("list_name"),
                        rs.getString("list_type")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao listar UserLists:");
            e.printStackTrace();
        }
        return userLists;
    }

    public boolean atualizar(UserList userList) {
        String sql = "UPDATE user_lists SET list_name=?, list_type=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userList.getListName());
            stmt.setString(2, userList.getListType());
            stmt.setInt(3, userList.getId());
            stmt.setInt(4, userList.getUserId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao atualizar UserList:");
            e.printStackTrace();
        }
        return false;
    }

    public boolean excluir(int listId, int userId) {
        String sql = "DELETE FROM user_lists WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao excluir UserList:");
            e.printStackTrace();
        }
        return false;
    }
}