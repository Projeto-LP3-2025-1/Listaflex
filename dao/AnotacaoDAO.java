package dao;

import java.sql.*;
import java.util.*;
import model.Anotacao;
import util.DatabaseConnection;

public class AnotacaoDAO {

    public void inserir(Anotacao a) {
        // Alterado: Adicionado 'tipo_lista' na query
        String sql = "INSERT INTO anotacoes (titulo, descricao, status, user_id, tipo_lista) VALUES (?, ?, ?, ?, ?)";
        System.out.println("DEBUG: Tentando inserir Anotação.");
        System.out.println("DEBUG: Título: " + a.getTitulo() + ", Descrição: " + a.getDescricao() + ", Status: " + a.getStatus() + ", UserID: " + a.getUserId() + ", Tipo: " + a.getTipoLista());
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, a.getTitulo());
            stmt.setString(2, a.getDescricao());
            stmt.setString(3, a.getStatus());
            stmt.setInt(4, a.getUserId());
            stmt.setString(5, a.getTipoLista()); // Definir o tipo_lista
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

    // Método listar que recebe o tipo de lista
    public List<Anotacao> listar(int userId, String tipoLista) {
        List<Anotacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM anotacoes WHERE user_id = ? AND tipo_lista = ?";
        System.out.println("DEBUG: Executando listar para UserID: " + userId + ", Tipo: " + tipoLista);
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, tipoLista);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Anotacao(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("status"),
                        rs.getInt("user_id"),
                        rs.getString("tipo_lista")
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

    // Os métodos atualizar e excluir devem começar AQUI, DENTRO da classe AnotacaoDAO.
    // NÃO DEVE HAVER UMA CHAVE } ANTES DESTE MÉTODO.
    public void atualizar(Anotacao a) {
        String sql = "UPDATE anotacoes SET titulo=?, descricao=?, status=?, tipo_lista=? WHERE id=? AND user_id=?";
        System.out.println("DEBUG: Tentando atualizar Anotação ID: " + a.getId() + ", UserID: " + a.getUserId() + ", Tipo: " + a.getTipoLista());
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getTitulo());
            stmt.setString(2, a.getDescricao());
            stmt.setString(3, a.getStatus());
            stmt.setString(4, a.getTipoLista());
            stmt.setInt(5, a.getId());
            stmt.setInt(6, a.getUserId());
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

    public void excluir(int id, int userId) {
        String sql = "DELETE FROM anotacoes WHERE id=? AND user_id=?";
        System.out.println("DEBUG: Tentando excluir Anotação ID: " + id + ", UserID: " + userId);
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
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
} // Esta é a ÚNICA chave que fecha a classe AnotacaoDAO