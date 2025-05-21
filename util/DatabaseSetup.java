package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private static final String URL_SEM_BANCO = "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "1234567890";

    public static void criarBanco() {
        String sql = "CREATE DATABASE IF NOT EXISTS listaflex";
        try (Connection conn = DriverManager.getConnection(URL_SEM_BANCO, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Banco criado ou já existia.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar banco: " + e.getMessage());
        }
    }

    public static void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS anotacoes (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "titulo VARCHAR(100), " +
                     "descricao TEXT, " +
                     "status VARCHAR(20)" +
                     ")";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'anotacoes' criada ou já existe.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }
}
