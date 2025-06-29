// START OF FILE: AnotacaoDAO.java
package dao;

import model.Anotacao;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnotacaoDAO {

    public void inserir(Anotacao anotacao) {
        String sql = "INSERT INTO anotacoes (titulo, descricao, status, list_id, prioridade, is_concluida_visual) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("DEBUG: Tentando inserir Anotação.");
        System.out.println("DEBUG: Título: " + anotacao.getTitulo() + ", Descrição: " + anotacao.getDescricao() + ", Status: " + anotacao.getStatus() + ", ListID: " + anotacao.getListId() + ", Prioridade: " + anotacao.getPrioridade() + ", ConcluidaVisual: " + anotacao.isConcluidaVisual());
        try (Connection con = DatabaseConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, anotacao.getTitulo());
            ps.setString(2, anotacao.getDescricao());
            ps.setString(3, anotacao.getStatus());
            ps.setInt(4, anotacao.getListId());
            ps.setString(5, anotacao.getPrioridade());
            ps.setBoolean(6, anotacao.isConcluidaVisual());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    anotacao.setId(rs.getInt(1));
                }
            }
            System.out.println("DEBUG: Anotação inserida no BD. ID: " + anotacao.getId() + ", Título: " + anotacao.getTitulo() + ", ListID: " + anotacao.getListId() + ", ConcluidaVisual: " + anotacao.isConcluidaVisual());
        } catch (SQLException e) {
            System.err.println("Erro ao inserir anotação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void atualizar(Anotacao anotacao) {
        String sql = "UPDATE anotacoes SET titulo = ?, descricao = ?, status = ?, prioridade = ?, is_concluida_visual = ? WHERE id = ? AND list_id = ?";
        System.out.println("DEBUG: Tentando atualizar Anotação ID: " + anotacao.getId() + ", ListID: " + anotacao.getListId() + ", Prioridade: " + anotacao.getPrioridade() + ", ConcluidaVisual: " + anotacao.isConcluidaVisual());
        try (Connection con = DatabaseConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anotacao.getTitulo());
            ps.setString(2, anotacao.getDescricao());
            ps.setString(3, anotacao.getStatus());
            ps.setString(4, anotacao.getPrioridade());
            ps.setBoolean(5, anotacao.isConcluidaVisual());
            ps.setInt(6, anotacao.getId());
            ps.setInt(7, anotacao.getListId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DEBUG: Anotação atualizada no BD. ID: " + anotacao.getId() + ", Título: " + anotacao.getTitulo() + ", ConcluidaVisual: " + anotacao.isConcluidaVisual());
            } else {
                System.out.println("DEBUG: Nenhuma anotação encontrada para atualização com ID: " + anotacao.getId() + " e ListID: " + anotacao.getListId());
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar anotação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void excluir(int id, int listId) {
        String sql = "DELETE FROM anotacoes WHERE id = ? AND list_id = ?";
        System.out.println("DEBUG: Tentando excluir Anotação ID: " + id + ", ListID: " + listId);
        try (Connection con = DatabaseConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, listId);
            ps.executeUpdate();
            System.out.println("DEBUG: Anotação excluída do BD. ID: " + id + ", ListID: " + listId);
        } catch (SQLException e) {
            System.err.println("Erro ao excluir anotação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Anotacao> listar(int listId) {
        List<Anotacao> anotacoes = new ArrayList<>();
        String sql = "SELECT id, titulo, descricao, status, list_id, prioridade, is_concluida_visual FROM anotacoes WHERE list_id = ?";
        System.out.println("DEBUG: Executando listar para ListID: " + listId);
        try (Connection con = DatabaseConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Anotacao a = new Anotacao(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("status"),
                        rs.getInt("list_id"),
                        rs.getString("prioridade"),
                        rs.getBoolean("is_concluida_visual")
                    );
                    anotacoes.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar anotações: " + e.getMessage());
            e.printStackTrace();
        }
        return anotacoes;
    }

    public List<Anotacao> listarEOrdenarPorPrioridade(int listId) {
        List<Anotacao> anotacoes = new ArrayList<>();
        // ORDEM: Muito importante > Importante > Pouco importante
        String sql = "SELECT id, titulo, descricao, status, list_id, prioridade, is_concluida_visual FROM anotacoes WHERE list_id = ? ORDER BY FIELD(prioridade, 'Muito importante', 'Importante', 'Pouco importante')";
        try (Connection con = DatabaseConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Anotacao a = new Anotacao(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("status"),
                        rs.getInt("list_id"),
                        rs.getString("prioridade"),
                        rs.getBoolean("is_concluida_visual")
                    );
                    anotacoes.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar e ordenar anotações por prioridade: " + e.getMessage());
            e.printStackTrace();
        }
        return anotacoes;
    }
}
// END OF FILE: AnotacaoDAO.java