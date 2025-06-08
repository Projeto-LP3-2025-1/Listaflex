package view;

import dao.AnotacaoDAO;
import model.Anotacao;
import view.TelaListaComum;
import view.TelaLogin;
import dao.UserListDAO;
import model.UserList;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaKanban extends JFrame { // Início da classe TelaKanban
    private JPanel mainPanel;
    private JPanel kanbanPanel;
    private TelaListaComum listaComumPanel;

    private JPanel panelAFazer = new JPanel(new GridLayout(0, 1));
    private JPanel panelFazendo = new JPanel(new GridLayout(0, 1));
    private JPanel panelFeito = new JPanel(new GridLayout(0, 1));

    private AnotacaoDAO anotacaoDAO = new AnotacaoDAO();
    private UserListDAO userListDAO = new UserListDAO();

    private int loggedInUserId;
    private int currentListId;
    private String currentListName;
    private String currentListType;

    public TelaKanban(int userId, String tipoListaInicial, int listId, String listName) {
        System.out.println("DEBUG: Construtor TelaKanban iniciado para UserID: " + userId + ", Tipo: " + tipoListaInicial + ", ListID: " + listId + ", ListName: " + listName); // DEBUG AQUI
        this.loggedInUserId = userId;
        this.currentListId = listId;
        this.currentListName = listName;
        this.currentListType = tipoListaInicial;


        setTitle("Listaflex - " + currentListName + " (" + currentListType + ")");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Opções");

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

        JMenuItem gerenciarListas = new JMenuItem("Gerenciar Listas");
        gerenciarListas.addActionListener(e -> {
            dispose();
            new TelaEscolhaLista(loggedInUserId);
        });
        menu.add(gerenciarListas);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        mainPanel = new JPanel(new CardLayout());

        kanbanPanel = new JPanel(new GridLayout(1, 3));
        // CHAMA criarColuna AQUI
        kanbanPanel.add(criarColuna(panelAFazer, "A Fazer"));
        kanbanPanel.add(criarColuna(panelFazendo, "Fazendo"));
        kanbanPanel.add(criarColuna(panelFeito, "Feito"));

        listaComumPanel = new TelaListaComum(this.loggedInUserId, this.currentListId, this.currentListName, this); // Construtor completo

        mainPanel.add(kanbanPanel, "KANBAN_VIEW");
        mainPanel.add(listaComumPanel, "LISTA_COMUM_VIEW");

        add(mainPanel, BorderLayout.CENTER);

        // CHAMA showKanbanView/showListaComumView AQUI
        if ("KANBAN".equals(currentListType)) {
            showKanbanView();
        } else if ("COMUM".equals(currentListType)) {
            showListaComumView();
        } else {
            System.err.println("DEBUG: Tipo de lista inicial desconhecido: " + currentListType);
            showKanbanView();
        }

        System.out.println("DEBUG: TelaKanban iniciada para o usuário ID: " + this.loggedInUserId + ", Lista ID: " + this.currentListId);
        setVisible(true);
    }

    // --- MÉTODOS DEVEM ESTAR ABAIXO DESTE PONTO E ANTES DO FIM DA CLASSE ---

    // Método criarColuna
    private JPanel criarColuna(JPanel panel, String titulo) {
        JPanel coluna = new JPanel(new BorderLayout());
        coluna.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        coluna.add(new JScrollPane(panel), BorderLayout.CENTER);

        JButton btnNovaAnotacaoColuna = new JButton("+ Nova Anotação");
        btnNovaAnotacaoColuna.addActionListener(e -> abrirCadastroKanbanComStatus(titulo));
        coluna.add(btnNovaAnotacaoColuna, BorderLayout.SOUTH);

        return coluna;
    }

    // Método showKanbanView
    private void showKanbanView() {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, "KANBAN_VIEW");
        carregarAnotacoesKanban();
        setTitle("Listaflex - " + currentListName + " (Kanban)"); // Título dinâmico
    }

    // Método showListaComumView
    private void showListaComumView() {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, "LISTA_COMUM_VIEW");
        listaComumPanel.setListContext(this.currentListId, this.currentListName); // Configura o contexto antes de carregar
        setTitle("Listaflex - " + currentListName + " (Lista Comum)"); // Título dinâmico
    }

    // Método showSpecificView (para TelaListaComum chamar de volta)
    public void showSpecificView(String viewName) {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, viewName);
        if ("KANBAN_VIEW".equals(viewName)) {
            carregarAnotacoesKanban();
            setTitle("Listaflex - " + currentListName + " (Kanban)");
        } else if ("LISTA_COMUM_VIEW".equals(viewName)) {
            listaComumPanel.carregarAnotacoes(); // Chamada após definir contexto
            setTitle("Listaflex - " + currentListName + " (Lista Comum)");
        }
    }

    // Método abrirCadastroKanbanComStatus
    private void abrirCadastroKanbanComStatus(String statusInicial) {
    JTextField titulo = new JTextField();
    JTextArea descricao = new JTextArea(5, 20);
    String[] statusOptions = {"AFazer", "Fazendo", "Feito"};
    JComboBox<String> statusBox = new JComboBox<>(statusOptions);
    statusBox.setSelectedItem(statusInicial.replace(" ", ""));

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
        // --- LINHA CORRIGIDA AQUI: Adicionado "POUCO_IMPORTANTE" como prioridade padrão ---
        Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.currentListId, "POUCO_IMPORTANTE"); // Adiciona prioridade padrão
        // --- FIM DA LINHA CORRIGIDA ---
        System.out.println("DEBUG: Tentando criar anotação Kanban na UI. ListID: " + a.getListId() + ", Título: " + a.getTitulo());
        anotacaoDAO.inserir(a);
        System.out.println("DEBUG: Chamando carregarAnotacoesKanban() após inserção.");
        carregarAnotacoesKanban();
    }
}

    // Método carregarAnotacoesKanban
    public void carregarAnotacoesKanban() {
        panelAFazer.removeAll();
        panelFazendo.removeAll();
        panelFeito.removeAll();

        System.out.println("DEBUG: Carregando anotações Kanban para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listar(this.currentListId);
        System.out.println("DEBUG: Anotações Kanban encontradas para ListID " + this.currentListId + ": " + lista.size());
        for (Anotacao a : lista) {
            if (a.getStatus().equals("AFazer") || a.getStatus().equals("Fazendo") || a.getStatus().equals("Feito")) {
                System.out.println("DEBUG:   - Anotação Kanban carregada: ID=" + a.getId() + ", Título='" + a.getTitulo() + "', Status='" + a.getStatus() + "', ListID=" + a.getListId());
                JButton botao = new JButton("<html><b>" + a.getTitulo() + "</b><br>" + a.getDescricao() + "</html>");
                botao.addActionListener(e -> editarAnotacaoKanban(a));
                switch (a.getStatus()) {
                    case "AFazer" -> panelAFazer.add(botao);
                    case "Fazendo" -> panelFazendo.add(botao);
                    case "Feito" -> panelFeito.add(botao);
                }
            }
        }
        revalidate();
        repaint();
    }

    // Método editarAnotacaoKanban
    private void editarAnotacaoKanban(Anotacao a) {
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

        // Adiciona um ComboBox para o tipo de lista (para mover entre listas)
        String[] tipoListaOptions = {"KANBAN", "COMUM"};
        JComboBox<String> tipoListBox = new JComboBox<>(tipoListaOptions);
        // Precisamos obter o tipo da lista atual que 'a' pertence.
        // Já que 'a' não tem tipoLista, precisamos buscar o tipo da UserList pelo currentListId
        // Para simplicidade AGORA, vamos assumir que se você está editando um KANBAN,
        // o tipo default é KANBAN. Se mudar, ele se move.
        // No entanto, para ser preciso, você buscaria userListDAO.getById(currentListId).getListType()
        tipoListBox.setSelectedItem(currentListType); // Pré-seleciona o tipo da lista ATUAL

        panel.add(new JLabel("Mover para Tipo:")); // Label alterada para "Mover para Tipo"
        panel.add(tipoListBox);


        String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};

        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (" + currentListName + ")", // Título dinâmico
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            a.setTitulo(titulo.getText());
            a.setDescricao(descricao.getText());
            a.setStatus((String) statusBox.getSelectedItem());

            String novoTipoListaSelecionado = (String) tipoListBox.getSelectedItem();

            if (!novoTipoListaSelecionado.equals(currentListType)) { // Se o tipo da lista mudou
                // Crie uma nova lista para a anotação no tipo selecionado, ou mova ela
                // Para mover, precisamos do ID de uma lista EXISTENTE do tipo selecionado
                // OU, precisamos criar uma nova lista SE NÃO HOUVER nenhuma do tipo.
                // Isso é complexo. A forma mais simples aqui é:
                // 1. Apagar da lista atual (excluir)
                // 2. Criar uma nova anotação na nova lista (inserir)
                // Isso duplicaria o conteúdo se o usuário não gerenciar as listas.

                // Abordagem mais simples AGORA para migrar a anotação para OUTRA LISTA:
                // Você precisaria de um método no UserListDAO para encontrar uma lista do tipo 'novoTipoListaSelecionado'
                // OU criar uma nova lista desse tipo para o usuário.
                // Por enquanto, vamos manter o tipo da anotação amarrado à list_id, e a list_id não muda na anotação.
                // Se a anotação deve ser movida, ela deve ser excluída e recriada em outra lista.

                // ***** SE A ANOTAÇÃO MUDAR DE TIPO AQUI, ELA DEVE SER MOVIDA PARA OUTRA LISTA REALMENTE. *****
                // Isso requer um novo ListID. Não é o currentListId.
                // Esta funcionalidade seria mais complexa e envolveria:
                // 1. Perguntar para qual lista específica mover (não apenas tipo)
                // 2. Alterar o list_id da anotação
                // 3. Atualizar a anotação.

                // Por enquanto, vamos assumir que o 'Tipo de Lista' no editar é apenas para visualizar.
                // Se o objetivo é MOVER ANOTAÇÃO ENTRE LISTAS, precisamos de um diálogo de seleção de lista.

                // Se o objetivo é editar a anotação DENTRO DA ATUAL LISTA, não mudamos o list_id dela aqui.
                // A linha abaixo garante que o list_id da anotação não é alterado (permanece no currentListId)
                a.setListId(currentListId); // Garante que a anotação continua na lista atual

                anotacaoDAO.atualizar(a); // Usa anotacaoDAO

                // Recarrega a visualização (apenas a atual, já que não mudamos a lista)
                if ("KANBAN".equals(currentListType)) {
                    carregarAnotacoesKanban();
                } else if ("COMUM".equals(currentListType)) {
                    showListaComumView(); // Recarrega o painel da lista comum
                }


            } else { // Não mudou o tipo da lista, apenas editou a anotação na lista atual
                a.setListId(currentListId); // Garante que o listId da anotação é o da lista atual
                anotacaoDAO.atualizar(a); // Usa anotacaoDAO
                if ("KANBAN".equals(currentListType)) {
                    carregarAnotacoesKanban();
                } else if ("COMUM".equals(currentListType)) {
                    showListaComumView(); // Recarrega o painel da lista comum
                }
            }


        } else if (result == 1) { // Clicou em "Excluir Anotação"
            int confirmExcluir = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmExcluir == JOptionPane.YES_OPTION) {
                anotacaoDAO.excluir(a.getId(), this.currentListId);
                if ("KANBAN".equals(currentListType)) {
                    carregarAnotacoesKanban();
                } else if ("COMUM".equals(currentListType)) {
                    showListaComumView(); // Recarrega o painel da lista comum
                }
            }
        }
    }
}