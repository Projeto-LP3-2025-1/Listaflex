package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup { // <-- Início da classe DatabaseSetup
    private static final String URL_SEM_BANCO = "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "admin"; // ALtere para a senha REAL do seu MySQL! (Ex: "1234567890")

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

    // Este método criarTabela() deve estar AQUI DENTRO da classe DatabaseSetup
    public static void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS anotacoes (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "titulo VARCHAR(100), " +
                     "descricao TEXT, " +
                     "status VARCHAR(20), " +
                     "user_id INT, " +
                     "tipo_lista VARCHAR(20), " + // <-- NOVA COLUNA
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'anotacoes' criada ou já existe.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela 'anotacoes': " + e.getMessage());
        }
    }

    public static void criarTabelaUsuarios() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "username VARCHAR(50) NOT NULL UNIQUE, " +
                     "password VARCHAR(255) NOT NULL" +
                     ")";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'users' criada ou já existe.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela de usuários: " + e.getMessage());
        }
    }
} // <-- Fim da classe DatabaseSetup