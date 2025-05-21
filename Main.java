import util.DatabaseSetup;
import view.TelaKanban;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.criarBanco();
        DatabaseSetup.criarTabela();
        new TelaKanban();
    }
}