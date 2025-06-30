// Em Main.java
package project;

import project.util.DatabaseSetup;
import project.view.TelaLogin;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.criarBanco();
        DatabaseSetup.criarTabelaUsuarios(); // Cria users
        DatabaseSetup.criarTabelaUserLists(); // Cria user_lists
        DatabaseSetup.criarTabelaListCollaborators(); // NOVO: Cria list_collaborators
        DatabaseSetup.criarTabela(); // Cria anotacoes
        new TelaLogin();
    }
}