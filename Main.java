import util.DatabaseSetup;
import view.TelaLogin;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.criarBanco();
        DatabaseSetup.criarTabelaUsuarios(); // Cria users primeiro
        DatabaseSetup.criarTabelaUserLists(); // NOVO: Cria user_lists
        DatabaseSetup.criarTabela(); // Cria anotacoes (que depende de user_lists)
        new TelaLogin();
    }
}