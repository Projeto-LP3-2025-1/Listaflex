package dao;

import java.sql.*;
import java.util.*;
import model.Anotacao;
import util.DatabaseConnection;

public class AnotacaoDAO {

    public void inserir(Anotacao a) {
        String sql = "INSERT INTO anotacoes (titulo, descricao, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getTitulo());
            stmt.setString(2, a.getDescricao());
            stmt.setString(3, a.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Anotacao> listar() {
        List<Anotacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM anotacoes";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Anotacao(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void atualizar(Anotacao a) {
        String sql = "UPDATE anotacoes SET titulo=?, descricao=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getTitulo());
            stmt.setString(2, a.getDescricao());
            stmt.setString(3, a.getStatus());
            stmt.setInt(4, a.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM anotacoes WHERE id=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}