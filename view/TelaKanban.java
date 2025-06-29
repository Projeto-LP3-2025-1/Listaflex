// START OF FILE: TelaKanban.java
package view;

import dao.AnotacaoDAO;
import model.Anotacao;
import view.TelaListaComum;
import view.TelaLogin;
import dao.UserListDAO;
import model.UserList;
import dao.ListCollaboratorDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaKanban extends JFrame {
    // --- CONSTANTES DE CORES DA PALETA PROFISSIONAL ---
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50"); // Azul Marinho Escuro
    private static final Color ACCENT_COLOR = Color.decode("#3498DB"); // Azul Céu
    private static final Color BACKGROUND_COLOR_LIGHT = Color.decode("#ECF0F1"); // Fundo claro
    private static final Color TEXT_COLOR_DARK = Color.decode("#34495E"); // Texto principal
    private static final Color BORDER_COLOR_NEUTRAL = Color.decode("#BDC3C7"); // Bordas

    // Cores de Status Kanban
    private static final Color STATUS_TODO_COLOR = Color.decode("#E74C3C"); // Vermelho escuro (A Fazer)
    private static final Color STATUS_DOING_COLOR = Color.decode("#F1C40F"); // Amarelo mostarda (Fazendo)
    private static final Color STATUS_DONE_COLOR = Color.decode("#27AE60"); // Verde escuro (Feito)
    // --- FIM CONSTANTES DE CORES ---


    private JPanel mainPanel;
    private JPanel kanbanPanel;
    private TelaListaComum listaComumPanel;

    private JPanel panelAFazer = new JPanel(new GridLayout(0, 1, 10, 10)); // Espaçamento entre anotações
    private JPanel panelFazendo = new JPanel(new GridLayout(0, 1, 10, 10));
    private JPanel panelFeito = new JPanel(new GridLayout(0, 1, 10, 10));

    private AnotacaoDAO anotacaoDAO = new AnotacaoDAO();
    private UserListDAO userListDAO = new UserListDAO();
    private ListCollaboratorDAO collaboratorDAO = new ListCollaboratorDAO();

    private int loggedInUserId;
    private int currentListId;
    private String currentListName;
    private String currentListType;
    private String currentUserRole; // Papel do usuário logado na lista atual

    public TelaKanban(int userId, String tipoListaInicial, int listId, String listName) {
        this.loggedInUserId = userId;
        this.currentListId = listId;
        this.currentListName = listName;
        this.currentListType = tipoListaInicial;
        
        this.currentUserRole = collaboratorDAO.getUserRoleInList(this.currentListId, this.loggedInUserId);
        System.out.println("DEBUG (TelaKanban Construtor): Usuário ID " + loggedInUserId + " na Lista '" + currentListName + "' (ID: " + currentListId + ") tem Papel: " + currentUserRole);


        setTitle("Listaflex - " + currentListName + " (" + currentListType + ") - Papel: " + currentUserRole);
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR_LIGHT); // Fundo geral do frame

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR); // Cor da barra de menu
        JMenu menu = new JMenu("Opções");
        menu.setForeground(Color.WHITE); // Texto do menu

        JMenuItem deslogar = new JMenuItem("Deslogar");
        deslogar.setBackground(PRIMARY_COLOR); // Fundo do item de menu
        deslogar.setForeground(Color.WHITE); // Texto do item de menu
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
        gerenciarListas.setBackground(PRIMARY_COLOR);
        gerenciarListas.setForeground(Color.WHITE);
        gerenciarListas.addActionListener(e -> {
            dispose();
            new TelaEscolhaLista(loggedInUserId);
        });
        menu.add(gerenciarListas);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel principal


        kanbanPanel = new JPanel(new GridLayout(1, 3, 15, 0)); // Espaçamento entre colunas
        kanbanPanel.setBackground(BACKGROUND_COLOR_LIGHT);
        kanbanPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margem interna
        
        kanbanPanel.add(criarColuna(panelAFazer, "A Fazer"));
        kanbanPanel.add(criarColuna(panelFazendo, "Fazendo"));
        kanbanPanel.add(criarColuna(panelFeito, "Feito"));

        listaComumPanel = new TelaListaComum(this.loggedInUserId, this.currentListId, this.currentListName, this); 

        mainPanel.add(kanbanPanel, "KANBAN_VIEW");
        mainPanel.add(listaComumPanel, "LISTA_COMUM_VIEW");

        add(mainPanel, BorderLayout.CENTER);

        if ("KANBAN".equals(currentListType)) {
            showKanbanView();
        } else if ("COMUM".equals(currentListType)) {
            showListaComumView();
        } else {
            System.err.println("DEBUG: Tipo de lista inicial desconhecido: " + currentListType);
            showKanbanView();
        }

        System.out.println("DEBUG: TelaKanban iniciada para o usuário ID: " + this.loggedInUserId + ", Lista ID: " + this.currentListId + ", Papel: " + currentUserRole);
        setVisible(true);
    }

    private JPanel criarColuna(JPanel panel, String titulo) {
        JPanel coluna = new JPanel(new BorderLayout());
        coluna.setBackground(Color.WHITE); // Fundo da coluna
        coluna.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL, 1, true)); // Borda arredondada
        coluna.setPreferredSize(new Dimension(280, 500)); // Tamanho preferencial da coluna

        JLabel tituloLabel = new JLabel(titulo, SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Fonte maior
        tituloLabel.setForeground(TEXT_COLOR_DARK); // Cor do texto
        tituloLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Margem interna
        coluna.add(tituloLabel, BorderLayout.NORTH);

        // Painel interno que contém as anotações
        JPanel anotacoesPanel = new JPanel(); // Usará FlowLayout ou BoxLayout para empilhar
        anotacoesPanel.setLayout(new BoxLayout(anotacoesPanel, BoxLayout.Y_AXIS)); // Empilha verticalmente
        anotacoesPanel.setBackground(Color.WHITE); // Fundo branco
        coluna.add(new JScrollPane(anotacoesPanel), BorderLayout.CENTER); // Adiciona ScrollPane

        // Mapear o painel interno de anotações para a variável de classe correspondente
        if (titulo.equals("A Fazer")) {
            this.panelAFazer = anotacoesPanel;
        } else if (titulo.equals("Fazendo")) {
            this.panelFazendo = anotacoesPanel;
        } else if (titulo.equals("Feito")) {
            this.panelFeito = anotacoesPanel;
        }

        JButton btnNovaAnotacaoColuna = new JButton("+ Nova Anotação");
        btnNovaAnotacaoColuna.setBackground(ACCENT_COLOR); // Cor do botão
        btnNovaAnotacaoColuna.setForeground(Color.WHITE); // Texto branco
        btnNovaAnotacaoColuna.setFont(new Font("Arial", Font.BOLD, 14));
        btnNovaAnotacaoColuna.setOpaque(true);
        btnNovaAnotacaoColuna.setBorderPainted(false);
        btnNovaAnotacaoColuna.setFocusPainted(false);
        
        boolean canAdd = "Criador(a)".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole); // Voltou para Criador(a)/ADMIN/EDITOR
        System.out.println("DEBUG (criarColuna): Botão 'Nova Anotação' para coluna '" + titulo + "'. Papel: " + currentUserRole + ", Pode Adicionar: " + canAdd);

        if (canAdd) {
            btnNovaAnotacaoColuna.addActionListener(e -> abrirCadastroKanbanComStatus(titulo));
            btnNovaAnotacaoColuna.setEnabled(true);
            btnNovaAnotacaoColuna.setToolTipText(null);
            System.out.println("DEBUG (criarColuna): Botão 'Nova Anotação' para '" + titulo + "' HABILITADO.");
        } else {
            btnNovaAnotacaoColuna.setEnabled(false);
            btnNovaAnotacaoColuna.setToolTipText("Você não tem permissão para adicionar anotações.");
            System.out.println("DEBUG (criarColuna): Botão 'Nova Anotação' para '" + titulo + "' DESABILITADO.");
        }
        coluna.add(btnNovaAnotacaoColuna, BorderLayout.SOUTH);

        return coluna;
    }

    private void showKanbanView() {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, "KANBAN_VIEW");
        carregarAnotacoesKanban();
        setTitle("Listaflex - " + currentListName + " (Kanban) - Papel: " + currentUserRole);
    }

    private void showListaComumView() {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, "LISTA_COMUM_VIEW");
        listaComumPanel.setListContext(this.currentListId, this.currentListName);
        listaComumPanel.updatePermissions(currentUserRole);
        setTitle("Listaflex - " + currentListName + " (Lista Comum) - Papel: " + currentUserRole);
    }

    public void showSpecificView(String viewName) {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());
        cl.show(mainPanel, viewName);
        if ("KANBAN_VIEW".equals(viewName)) {
            carregarAnotacoesKanban();
            setTitle("Listaflex - " + currentListName + " (Kanban) - Papel: " + currentUserRole);
        } else if ("LISTA_COMUM_VIEW".equals(viewName)) {
            listaComumPanel.setListContext(this.currentListId, this.currentListName);
            listaComumPanel.updatePermissions(currentUserRole);
            setTitle("Listaflex - " + currentListName + " (Lista Comum) - Papel: " + currentUserRole);
        }
    }

    public String getCurrentUserRole() {
        return this.currentUserRole;
    }

    private void abrirCadastroKanbanComStatus(String statusInicial) {
        System.out.println("DEBUG (abrirCadastroKanban): Verificando permissão antes de abrir diálogo. Papel: " + currentUserRole);
        if (!("Criador(a)".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole))) { // Voltou para Criador(a)/ADMIN/EDITOR
            JOptionPane.showMessageDialog(this, "Você não tem permissão para adicionar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            System.out.println("DEBUG (abrirCadastroKanban): Permissão NEGADA para adicionar anotação Kanban.");
            return;
        }
        System.out.println("DEBUG (abrirCadastroKanban): Permissão CONCEDIDA para adicionar anotação Kanban.");

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

        // Cores para o diálogo de nova anotação Kanban
        panel.setBackground(BACKGROUND_COLOR_LIGHT);
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel)comp).setForeground(TEXT_COLOR_DARK);
            }
            if (comp instanceof JTextField || comp instanceof JTextArea || comp instanceof JComboBox) {
                comp.setBackground(Color.WHITE);
                comp.setForeground(TEXT_COLOR_DARK);
            }
        }
        UIManager.put("OptionPane.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Panel.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Button.background", ACCENT_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));


        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Kanban)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        System.out.println("DEBUG (abrirCadastroKanban): Resultado do JOptionPane: " + result);

        // Resetar UIManager
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);


        if (result == JOptionPane.OK_OPTION) {
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.currentListId, "Pouco importante", false);
            System.out.println("DEBUG (abrirCadastroKanban): Anotação preparada para inserção: " + a.getTitulo() + ", ListID: " + a.getListId());
            anotacaoDAO.inserir(a);
            System.out.println("DEBUG (abrirCadastroKanban): Chamando carregarAnotacoesKanban() após inserção.");
            carregarAnotacoesKanban();
        } else {
            System.out.println("DEBUG (abrirCadastroKanban): Operação de criação cancelada/fechada.");
        }
    }

    public void carregarAnotacoesKanban() {
        panelAFazer.removeAll();
        panelFazendo.removeAll();
        panelFeito.removeAll();

        System.out.println("DEBUG: Carregando anotações Kanban para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listar(this.currentListId);
        System.out.println("DEBUG: Anotações Kanban encontradas para ListID " + this.currentListId + ": " + lista.size());
        for (Anotacao a : lista) {
            if (a.getStatus().equals("AFazer") || a.getStatus().equals("Fazendo") || a.getStatus().equals("Feito")) {
                System.out.println("DEBUG:   - Anotação Kanban carregada: ID=" + a.getId() + ", Título='" + a.getTitulo() + "', Status='" + a.getStatus() + "', ListID=" + a.getListId() + ", Prioridade=" + a.getPrioridade() + ", ConcluidaVisual: " + a.isConcluidaVisual());
                JButton botao = new JButton("<html><b>" + a.getTitulo() + "</b><br>" + a.getDescricao() + "</html>");
                boolean canEdit = "Criador(a)".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole); // Voltou para Criador(a)/ADMIN/EDITOR
                System.out.println("DEBUG (carregarAnotacoesKanban): Anotação '" + a.getTitulo() + "'. Papel: " + currentUserRole + ", Pode Editar: " + canEdit);
                
                if (canEdit) {
                    botao.addActionListener(e -> editarAnotacaoKanban(a));
                    botao.setEnabled(true);
                    botao.setToolTipText(null);
                    System.out.println("DEBUG (carregarAnotacoesKanban): Botão de anotação HABILITADO.");
                } else {
                    botao.setEnabled(false);
                    botao.setToolTipText("Você não tem permissão para editar esta anotação.");
                    System.out.println("DEBUG (carregarAnotacoesKanban): Botão de anotação DESABILITADO.");
                }
                // Adicione cores às anotações com base no status (opcional, para visualização)
                Color bgColor = Color.WHITE;
                switch (a.getStatus()) {
                    case "AFazer": bgColor = STATUS_TODO_COLOR; break;
                    case "Fazendo": bgColor = STATUS_DOING_COLOR; break;
                    case "Feito": bgColor = STATUS_DONE_COLOR; break;
                }
                botao.setBackground(bgColor);
                botao.setForeground(TEXT_COLOR_DARK); // Cor do texto do botão da anotação
                botao.setOpaque(true);
                botao.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL, 1));
                botao.setHorizontalAlignment(SwingConstants.LEFT); // Alinha texto à esquerda
                botao.setVerticalAlignment(SwingConstants.TOP); // Alinha texto ao topo
                botao.setPreferredSize(new Dimension(200, 60)); // Tamanho fixo para cada botão
                
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

    private void editarAnotacaoKanban(Anotacao a) {
        System.out.println("DEBUG (editarAnotacaoKanban): Verificando permissão antes de editar. Papel: " + currentUserRole);
        boolean canEdit = "Criador(a)".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole); // Voltou para Criador(a)/ADMIN/EDITOR
        if (!canEdit) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para editar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            System.out.println("DEBUG (editarAnotacaoKanban): Permissão NEGADA para editar.");
            return;
        }
        System.out.println("DEBUG (editarAnotacaoKanban): Permissão CONCEDIDA para editar.");


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

        // Cores para os componentes do diálogo
        panel.setBackground(BACKGROUND_COLOR_LIGHT);
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel)comp).setForeground(TEXT_COLOR_DARK);
            }
            if (comp instanceof JTextField || comp instanceof JTextArea || comp instanceof JComboBox) {
                comp.setBackground(Color.WHITE);
                comp.setForeground(TEXT_COLOR_DARK);
                ((JComponent)comp).setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));
            }
        }
        UIManager.put("OptionPane.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Panel.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Button.background", ACCENT_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));


        String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};

        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (" + currentListName + ")",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        System.out.println("DEBUG (editarAnotacaoKanban): Resultado do JOptionPane: " + result);

        // Resetar UIManager
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);


        if (result == 0) { // Clicou em "Salvar"
            a.setTitulo(titulo.getText());
            a.setDescricao(descricao.getText());
            a.setStatus((String) statusBox.getSelectedItem());
            
            // Lógica de movimento de tipo de lista REMOVIDA
            // A anotação continua amarrada à 'currentListId'
            a.setListId(currentListId); 
            anotacaoDAO.atualizar(a);
            carregarAnotacoesKanban();

        } else if (result == 1) { // Clicou em "Excluir Anotação"
            System.out.println("DEBUG (editarAnotacaoKanban): Verificando permissão para excluir. Papel: " + currentUserRole);
            boolean canDelete = "Criador(a)".equals(currentUserRole) || "ADMIN".equals(currentUserRole); // Voltou para Criador(a)/ADMIN
            if (!canDelete) {
                JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                System.out.println("DEBUG (editarAnotacaoKanban): Permissão NEGADA para excluir.");
                return;
            }
            System.out.println("DEBUG (editarAnotacaoKanban): Permissão CONCEDIDA para excluir.");
            
            int confirmExcluir = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            System.out.println("DEBUG (editarAnotacaoKanban - Excluir): Confirmação de exclusão: " + (confirmExcluir == JOptionPane.YES_OPTION));

            if (confirmExcluir == JOptionPane.YES_OPTION) {
                anotacaoDAO.excluir(a.getId(), this.currentListId);
                carregarAnotacoesKanban();
            } else {
                System.out.println("DEBUG (editarAnotacaoKanban - Excluir): Exclusão cancelada.");
            }
        } else {
            System.out.println("DEBUG (editarAnotacaoKanban): Operação de edição cancelada/fechada.");
        }
    }
}
// END OF FILE: TelaKanban.java