import util.DatabaseSetup;
import view.TelaLogin;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.criarBanco();
        DatabaseSetup.criarTabelaUsuarios();
         DatabaseSetup.criarTabela();
        new TelaLogin();
    }
}