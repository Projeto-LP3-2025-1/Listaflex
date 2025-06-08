package dao;

import java.sql.*;
import java.util.*;
import model.Anotacao;
import util.DatabaseConnection;

public class AnotacaoDAO {

    public void inserir(Anotacao a) {
        // Alterado: Adicionado 'prioridade' na query
        String sql = "INSERT INTO anotacoes (titulo, descricao, status, list_id, prioridade) VALUES (?, ?, ?, ?, ?)";
        System.out.println("DEBUG: Tentando inserir Anotação.");
        System.out.println("DEBUG: Título: " + a.getTitulo() + ", Descrição: " + a.getDescricao() + ", Status: " + a.getStatus() + ", ListID: " + a.getListId() + ", Prioridade: " + a.getPrioridade());
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, a.getTitulo());
            stmt.setString(2, a.getDescricao());
            stmt.setString(3, a.getStatus());
            stmt.setInt(4, a.getListId());
            stmt.setString(5, a.getPrioridade()); // Define a prioridade
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG: Linhas afetadas pela inserção: " + rowsAffected);

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        a.setId(generatedKeys.getInt(1));
                        System.out.println("DEBUG: Anotação inserida com sucesso! ID gerado: " + a.getId());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao inserir Anotação:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao inserir Anotação:");
            e.printStackTrace();
        }
    }

    public List<Anotacao> listar(int listId) {
        List<Anotacao> lista = new ArrayList<>();
        // Alterado: Seleciona também a prioridade
        String sql = "SELECT * FROM anotacoes WHERE list_id = ?";
        System.out.println("DEBUG: Executando listar para ListID: " + listId);
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Anotacao(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("status"),
                        rs.getInt("list_id"),
                        rs.getString("prioridade") // Obtém a prioridade do ResultSet
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao listar Anotações:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao listar Anotações:");
            e.printStackTrace();
        }
        return lista;
    }

    // NOVO MÉTODO: Listar e ordenar por prioridade (para a lista comum)
    public List<Anotacao> listarEOrdenarPorPrioridade(int listId) {
        List<Anotacao> lista = new ArrayList<>();
        // Ordena pela prioridade. Defina a ordem que quiser (ex: MUITO_IMPORTANTE primeiro)
        String sql = "SELECT * FROM anotacoes WHERE list_id = ? ORDER BY CASE prioridade WHEN 'MUITO_IMPORTANTE' THEN 1 WHEN 'IMPORTANTE' THEN 2 WHEN 'POUCO_IMPORTANTE' THEN 3 ELSE 4 END";
        System.out.println("DEBUG: Executando listar e ordenar por prioridade para ListID: " + listId);
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Anotacao(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("status"),
                        rs.getInt("list_id"),
                        rs.getString("prioridade")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao listar e ordenar por prioridade:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao listar e ordenar por prioridade:");
            e.printStackTrace();
        }
        return lista;
    }

    public void atualizar(Anotacao a) {
        // Alterado: Adicionado 'prioridade' na query de atualização
        String sql = "UPDATE anotacoes SET titulo=?, descricao=?, status=?, prioridade=? WHERE id=? AND list_id=?";
        System.out.println("DEBUG: Tentando atualizar Anotação ID: " + a.getId() + ", ListID: " + a.getListId() + ", Prioridade: " + a.getPrioridade());
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getTitulo());
            stmt.setString(2, a.getDescricao());
            stmt.setString(3, a.getStatus());
            stmt.setString(4, a.getPrioridade()); // Atualiza a prioridade
            stmt.setInt(5, a.getId());
            stmt.setInt(6, a.getListId());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG: Linhas afetadas pela atualização: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao atualizar Anotação:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao atualizar Anotação:");
            e.printStackTrace();
        }
    }

    public void excluir(int id, int listId) {
        String sql = "DELETE FROM anotacoes WHERE id=? AND list_id=?";
        System.out.println("DEBUG: Tentando excluir Anotação ID: " + id + ", ListID: " + listId);
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, listId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG: Linhas afetadas pela exclusão: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao excluir Anotação:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO GERAL ao excluir Anotação:");
            e.printStackTrace();
        }
    }
}