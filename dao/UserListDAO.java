// START OF FILE: UserListDAO.java
package dao;

import model.UserList;
import model.ListCollaborator; // Adicionado import para ListCollaborator
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserListDAO {
    private ListCollaboratorDAO collaboratorDAO = new ListCollaboratorDAO();

    public int inserir(UserList userList) {
        String sql = "INSERT INTO user_lists (user_id, list_name, list_type) VALUES (?, ?, ?)";
        int generatedId = -1;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userList.getUserId());
            stmt.setString(2, userList.getListName());
            stmt.setString(3, userList.getListType());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        userList.setId(generatedId);
                        // CORREÇÃO: Chamar addCollaborator para definir o criador como OWNER
                        collaboratorDAO.addCollaborator(new ListCollaborator(generatedId, userList.getUserId(), "OWNER"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao inserir UserList: " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId;
    }

    // Listar listas que um usuário está associado (como owner ou colaborador)
    public List<UserList> listarPorUsuario(int userId) {
        return collaboratorDAO.getListsForUser(userId);
    }

    public boolean atualizar(UserList userList) {
        String sql = "UPDATE user_lists SET list_name=?, list_type=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userList.getListName());
            stmt.setString(2, userList.getListType());
            stmt.setInt(3, userList.getId());
            stmt.setInt(4, userList.getUserId()); // O owner da lista não muda
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao atualizar UserList: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean excluir(int listId, int userId) {
        String sql = "DELETE FROM user_lists WHERE id=? AND user_id=?"; // Garante que apenas o dono pode excluir
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao excluir UserList: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public UserList getById(int listId) {
        String sql = "SELECT id, user_id, list_name, list_type FROM user_lists WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserList(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("list_name"),
                        rs.getString("list_type")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter UserList por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
// END OF FILE: UserListDAO.java