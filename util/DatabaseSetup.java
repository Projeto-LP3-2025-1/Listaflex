package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private static final String URL_SEM_BANCO = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "admin"; // ALtere para a senha REAL do seu MySQL! (Ex: "1234567890")

    public static void criarBanco() {
        String sql = "CREATE DATABASE IF NOT EXISTS listaflex";
        try (Connection conn = DriverManager.getConnection(URL_SEM_BANCO, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Banco 'listaflex' criado ou já existia.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar banco: " + e.getMessage());
        }
    }

    // NOVO MÉTODO: Cria a tabela user_lists
    public static void criarTabelaUserLists() {
        String sql = "CREATE TABLE IF NOT EXISTS user_lists (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id INT NOT NULL, " +
                     "list_name VARCHAR(100) NOT NULL, " +
                     "list_type VARCHAR(20) NOT NULL, " + // 'KANBAN' ou 'COMUM'
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'user_lists' criada ou já existe.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela 'user_lists': " + e.getMessage());
        }
    }
    // NOVO MÉTODO: Cria a tabela list_collaborators para compartilhamento
public static void criarTabelaListCollaborators() {
    String sql = "CREATE TABLE IF NOT EXISTS list_collaborators (" +
                 "list_id INT NOT NULL, " +
                 "user_id INT NOT NULL, " +
                 "role VARCHAR(20) NOT NULL, " + // Ex: 'OWNER', 'ADMIN', 'EDITOR', 'VIEWER'
                 "PRIMARY KEY (list_id, user_id), " + // Chave primária composta
                 "FOREIGN KEY (list_id) REFERENCES user_lists(id) ON DELETE CASCADE, " + // Refere a lista
                 "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" + // Refere o usuário
                 ")";
    try (Connection conn = DatabaseConnection.connect();
         Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
        System.out.println("Tabela 'list_collaborators' criada ou já existe.");
    } catch (SQLException e) {
        System.err.println("Erro ao criar tabela 'list_collaborators': " + e.getMessage());
    }
}
    // MÉTODO MODIFICADO: Cria a tabela anotacoes para referenciar user_lists
    // Em util/DatabaseSetup.java
public static void criarTabela() {
    String sql = "CREATE TABLE IF NOT EXISTS anotacoes (" +
                 "id INT AUTO_INCREMENT PRIMARY KEY, " +
                 "titulo VARCHAR(100), " +
                 "descricao TEXT, " +
                 "is_concluida_visual BOOLEAN, " +
                 "status VARCHAR(20), " +
                 "list_id INT NOT NULL, " +
                 "prioridade VARCHAR(20) DEFAULT 'POUCO_IMPORTANTE', " + // <-- NOVA COLUNA para prioridade
                 "FOREIGN KEY (list_id) REFERENCES user_lists(id) ON DELETE CASCADE" +
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
}