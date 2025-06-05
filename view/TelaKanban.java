// Em view/TelaKanban.java
package view;

import dao.AnotacaoDAO;
import model.Anotacao;
import view.TelaListaComum; // Adicione esta linha

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaKanban extends JFrame { // Mantém o nome TelaKanban, mas agora é a tela principal
    private JPanel mainPanel; // Painel principal que irá trocar as visualizações
    private JPanel kanbanPanel; // Painel da visualização Kanban
    private TelaListaComum listaComumPanel; // Painel da visualização de lista comum

    private JPanel panelAFazer = new JPanel(new GridLayout(0, 1));
    private JPanel panelFazendo = new JPanel(new GridLayout(0, 1));
    private JPanel panelFeito = new JPanel(new GridLayout(0, 1));

    private AnotacaoDAO dao = new AnotacaoDAO();
    private int loggedInUserId;

    public TelaKanban(int userId) {
        this.loggedInUserId = userId;

        setTitle("Listaflex - Gerenciador de Anotações - Usuário ID: " + userId);
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Configuração da Barra de Menus ---
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Opções");

        JMenuItem cadastrar = new JMenuItem("Nova Anotação (Kanban)"); // Opcional: especificar tipo
        cadastrar.addActionListener(e -> abrirCadastroKanban()); // Usará "KANBAN"
        menu.add(cadastrar);

        JMenuItem deslogar = new JMenuItem("Deslogar");
        deslogar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja deslogar?", "Deslogar",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new TelaLogin();
            }
        });
        menu.add(deslogar);

        JMenu viewMenu = new JMenu("Visualizar"); // Novo menu para alternar visualizações
        JMenuItem viewKanban = new JMenuItem("Ver Kanban");
        viewKanban.addActionListener(e -> showKanbanView());
        viewMenu.add(viewKanban);

        JMenuItem viewListaComum = new JMenuItem("Ver Lista Comum");
        viewListaComum.addActionListener(e -> showListaComumView());
        viewMenu.add(viewListaComum);

        menuBar.add(menu);
        menuBar.add(viewMenu); // Adiciona o novo menu à barra de menus
        setJMenuBar(menuBar);

        // --- Configuração dos Painéis de Visualização ---
        mainPanel = new JPanel(new CardLayout()); // Usa CardLayout para alternar painéis

        // Painel da Visualização Kanban
        kanbanPanel = new JPanel(new GridLayout(1, 3));
        kanbanPanel.add(criarColuna(panelAFazer, "A Fazer"));
        kanbanPanel.add(criarColuna(panelFazendo, "Fazendo"));
        kanbanPanel.add(criarColuna(panelFeito, "Feito"));

        // Instancia o Painel da Lista Comum
        listaComumPanel = new TelaListaComum(this.loggedInUserId);

        // Adiciona os painéis ao mainPanel com nomes para o CardLayout
        mainPanel.add(kanbanPanel, "KANBAN_VIEW");
        mainPanel.add(listaComumPanel, "LISTA_COMUM_VIEW");

        add(mainPanel, BorderLayout.CENTER); // Adiciona o mainPanel ao frame

        // Exibe a visualização Kanban por padrão
        showKanbanView();

        System.out.println("DEBUG: TelaKanban iniciada para o usuário ID: " + this.loggedInUserId);
        setVisible(true);
    }

    private JPanel criarColuna(JPanel panel, String titulo) {
        JPanel coluna = new JPanel(new BorderLayout());
        coluna.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        coluna.add(new JScrollPane(panel), BorderLayout.CENTER);
        return coluna;
    }

    // Métodos para alternar visualizações
    private void showKanbanView() {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, "KANBAN_VIEW");
        carregarAnotacoesKanban(); // Carrega anotações específicas do Kanban
        setTitle("Listaflex - Kanban - Usuário ID: " + loggedInUserId);
    }

    private void showListaComumView() {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, "LISTA_COMUM_VIEW");
        listaComumPanel.carregarAnotacoes(); // Chamada para carregar anotações da lista comum
        setTitle("Listaflex - Lista Comum - Usuário ID: " + loggedInUserId);
    }

    // --- MÉTODOS DE CADASTRO, CARREGAMENTO E EDIÇÃO PARA A TELA KANBAN ESPECÍFICOS ---
    private void abrirCadastroKanban() {
        JTextField titulo = new JTextField();
        JTextArea descricao = new JTextArea(5, 20);
        String[] statusOptions = {"AFazer", "Fazendo", "Feito"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Kanban)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Passa o tipo de lista "KANBAN"
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.loggedInUserId, "KANBAN");
            System.out.println("DEBUG: Tentando criar anotação Kanban na UI. UserID: " + a.getUserId() + ", Título: " + a.getTitulo());
            dao.inserir(a);
            System.out.println("DEBUG: Chamando carregarAnotacoesKanban() após inserção.");
            carregarAnotacoesKanban(); // Atualiza a visualização Kanban
        }
    }

    public void carregarAnotacoesKanban() { // Método renomeado para ser específico do Kanban
        panelAFazer.removeAll();
        panelFazendo.removeAll();
        panelFeito.removeAll();

        System.out.println("DEBUG: Carregando anotações Kanban para o usuário ID: " + this.loggedInUserId);
        List<Anotacao> lista = dao.listar(this.loggedInUserId, "KANBAN"); // Filtra por tipo "KANBAN"
        System.out.println("DEBUG: Anotações Kanban encontradas para o usuário ID " + this.loggedInUserId + ": " + lista.size());
        for (Anotacao a : lista) {
            System.out.println("DEBUG:   - Anotação Kanban carregada: ID=" + a.getId() + ", Título='" + a.getTitulo() + "', Status='" + a.getStatus() + "', UserID=" + a.getUserId() + ", Tipo=" + a.getTipoLista());
            JButton botao = new JButton("<html><b>" + a.getTitulo() + "</b><br>" + a.getDescricao() + "</html>");
            botao.addActionListener(e -> editarAnotacaoKanban(a)); // Renomeado para ser específico do Kanban
            switch (a.getStatus()) {
                case "AFazer" -> panelAFazer.add(botao);
                case "Fazendo" -> panelFazendo.add(botao);
                case "Feito" -> panelFeito.add(botao);
            }
        }
        revalidate();
        repaint();
    }

    private void editarAnotacaoKanban(Anotacao a) { // Método renomeado para ser específico do Kanban
        JTextField titulo = new JTextField(a.getTitulo());
        JTextArea descricao = new JTextArea(a.getDescricao());
        String[] statusOptions = {"AFazer", "Fazendo", "Feito"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);
        statusBox.setSelectedItem(a.getStatus());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};

        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Kanban)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            a.setTitulo(titulo.getText());
            a.setDescricao(descricao.getText());
            a.setStatus((String) statusBox.getSelectedItem());
            // Mantenha o tipo de lista original se estiver editando um item Kanban
            a.setTipoLista("KANBAN"); // Garante que o tipo não mude na edição
            dao.atualizar(a);
            carregarAnotacoesKanban();
        } else if (result == 1) { // Clicou em "Excluir Anotação"
            int confirmExcluir = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmExcluir == JOptionPane.YES_OPTION) {
                dao.excluir(a.getId(), this.loggedInUserId);
                carregarAnotacoesKanban();
            }
        }
    }
}