// START OF FILE: ListCollaboratorDAO.java
package dao;

import model.ListCollaborator;
import model.UserList; // Importar UserList
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListCollaboratorDAO {

    public boolean addCollaborator(ListCollaborator collaborator) {
        String sql = "INSERT INTO list_collaborators (list_id, user_id, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, collaborator.getListId());
            stmt.setInt(2, collaborator.getUserId());
            stmt.setString(3, collaborator.getRole());
            stmt.executeUpdate();
            System.out.println("DEBUG: Colaborador " + collaborator.getUserId() + " adicionado à lista " + collaborator.getListId() + " com papel " + collaborator.getRole());
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Erro de chave duplicada (colaborador já existe)
                System.err.println("Colaborador já existe para esta lista.");
            } else {
                System.err.println("Erro ao adicionar colaborador: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    public List<ListCollaborator> getCollaboratorsByList(int listId) {
        List<ListCollaborator> collaborators = new ArrayList<>();
        String sql = "SELECT list_id, user_id, role FROM list_collaborators WHERE list_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    collaborators.add(new ListCollaborator(
                        rs.getInt("list_id"),
                        rs.getInt("user_id"),
                        rs.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar colaboradores da lista: " + e.getMessage());
            e.printStackTrace();
        }
        return collaborators;
    }

    public boolean updateCollaboratorRole(int listId, int userId, String newRole) {
        String sql = "UPDATE list_collaborators SET role = ? WHERE list_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setInt(2, listId);
            stmt.setInt(3, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar papel do colaborador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeCollaborator(int listId, int userId) {
        String sql = "DELETE FROM list_collaborators WHERE list_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao remover colaborador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getUserRoleInList(int listId, int userId) {
        String role = null;
        String sql = "SELECT role FROM list_collaborators WHERE list_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar papel do usuário na lista: " + e.getMessage());
            e.printStackTrace();
        }
        return role;
    }

    // Busca todas as listas que um usuário está associado (como owner ou colaborador)
    public List<UserList> getListsForUser(int userId) {
        List<UserList> userLists = new ArrayList<>();
        // Junta user_lists com list_collaborators para obter todas as listas
        // onde o usuário é OWNER ou está listado como colaborador
        String sql = "SELECT DISTINCT ul.id, ul.user_id, ul.list_name, ul.list_type " +
                     "FROM user_lists ul " +
                     "JOIN list_collaborators lc ON ul.id = lc.list_id " +
                     "WHERE lc.user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userLists.add(new UserList(
                        rs.getInt("id"),
                        rs.getInt("user_id"), // Este user_id é o owner_id da lista, não o do colaborador
                        rs.getString("list_name"),
                        rs.getString("list_type")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar listas para o usuário: " + e.getMessage());
            e.printStackTrace();
        }
        return userLists;
    }
}
// END OF FILE: ListCollaboratorDAO.java