// START OF FILE: DatabaseConnection.java
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/listaflex?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "admin"; // ALTERAR AQUI SE A SENHA FOR DIFERENTE

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Erro na conexão com o banco: " + e.getMessage());
            return null;
        }
    }
}
// END OF FILE: DatabaseConnection.java