package view;

import dao.AnotacaoDAO;
import model.Anotacao;
import model.UserList; // Mantenha, ou remova se não for mais usado
import dao.UserListDAO; // Mantenha, ou remova se não for mais usado

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaListaComum extends JPanel {
    private JList<String> listaAnotacoes;
    private DefaultListModel<String> listModel;
    private AnotacaoDAO anotacaoDAO = new AnotacaoDAO();
    private int loggedInUserId;
    private int currentListId;
    private String currentListName;

    private TelaKanban mainFrame; // Referência ao JFrame principal para mudar a view

    // Construtor completo para quando a TelaKanban chama para exibir uma lista específica
    public TelaListaComum(int userId, int listId, String listName, TelaKanban mainFrame) {
        this.loggedInUserId = userId;
        this.currentListId = listId;
        this.currentListName = listName;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(currentListName + " (Lista Comum)")); // Título dinâmico

        listModel = new DefaultListModel<>();
        listaAnotacoes = new JList<>(listModel);
        listaAnotacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAnotacoes.setFont(new Font("Arial", Font.PLAIN, 16));
        listaAnotacoes.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = (String) value;
                setText("<html>" + text + "</html>");
                return this;
            }
        });

        add(new JScrollPane(listaAnotacoes), BorderLayout.CENTER);

        // Botões de ação para a lista comum
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnNova = new JButton("Nova Anotação");
        btnNova.addActionListener(e -> abrirCadastroListaComum());
        buttonPanel.add(btnNova);

        JButton btnEditar = new JButton("Editar Selecionada");
        btnEditar.addActionListener(e -> editarAnotacaoListaComum());
        buttonPanel.add(btnEditar);

        JButton btnExcluir = new JButton("Excluir Selecionada");
        btnExcluir.addActionListener(e -> excluirAnotacaoListaComum());
        buttonPanel.add(btnExcluir);

        // --- NOVO BOTÃO: Ordenar por Prioridade ---
        JButton btnOrdenarPrioridade = new JButton("Ordenar por Prioridade");
        btnOrdenarPrioridade.addActionListener(e -> ordenarPorPrioridade());
        buttonPanel.add(btnOrdenarPrioridade);
        // --- FIM DO NOVO BOTÃO ---

        add(buttonPanel, BorderLayout.SOUTH);

        // carregarAnotacoes(); // Não chama aqui, será chamado por setListContext
    }

    // Construtor para a criação inicial dentro de TelaKanban (para CardLayout)
    public TelaListaComum(int userId, TelaKanban mainFrame) {
        // Este construtor será chamado apenas para a criação inicial do CardLayout.
        // O carregamento e o listId serão definidos quando showListaComumView() for chamado.
        this.loggedInUserId = userId;
        this.mainFrame = mainFrame;
        this.currentListId = -1; // Temporário, será definido depois
        this.currentListName = "Lista Comum"; // Temporário
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(currentListName + " (Lista Comum)"));

        listModel = new DefaultListModel<>();
        listaAnotacoes = new JList<>(listModel);
        listaAnotacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAnotacoes.setFont(new Font("Arial", Font.PLAIN, 16));
        listaAnotacoes.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = (String) value;
                setText("<html>" + text + "</html>");
                return this;
            }
        });
        add(new JScrollPane(listaAnotacoes), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnNova = new JButton("Nova Anotação");
        btnNova.addActionListener(e -> abrirCadastroListaComum());
        buttonPanel.add(btnNova);
        JButton btnEditar = new JButton("Editar Selecionada");
        btnEditar.addActionListener(e -> editarAnotacaoListaComum());
        buttonPanel.add(btnEditar);
        JButton btnExcluir = new JButton("Excluir Selecionada");
        btnExcluir.addActionListener(e -> excluirAnotacaoListaComum());
        buttonPanel.add(btnExcluir);

        // --- NOVO BOTÃO: Ordenar por Prioridade ---
        JButton btnOrdenarPrioridade = new JButton("Ordenar por Prioridade");
        btnOrdenarPrioridade.addActionListener(e -> ordenarPorPrioridade());
        buttonPanel.add(btnOrdenarPrioridade);
        // --- FIM DO NOVO BOTÃO ---

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Método para configurar o contexto da lista quando ela é aberta
    public void setListContext(int listId, String listName) {
        this.currentListId = listId;
        this.currentListName = listName;
        setBorder(BorderFactory.createTitledBorder(currentListName + " (Lista Comum)"));
        carregarAnotacoes(); // Carrega após o contexto ser definido
    }

    public void carregarAnotacoes() { // Tornar público para TelaKanban poder chamar
        listModel.clear();
        if (currentListId == -1) {
            System.out.println("DEBUG: Contexto da lista comum não definido para carregar anotações.");
            return;
        }
        System.out.println("DEBUG: Carregando anotações da Lista Comum para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listar(this.currentListId); // Listagem padrão
        System.out.println("DEBUG: Anotações da Lista Comum encontradas: " + lista.size());
        for (Anotacao a : lista) {
            // Filtra por status de lista comum (se desejar, ou mostre todos os status)
            // Aqui estamos assumindo que a lista comum tem anotações com status "Pendente" ou "Concluído"
            if (a.getStatus().equals("Pendente") || a.getStatus().equals("Concluído")) {
                listModel.addElement("ID: " + a.getId() + " - Título: " + a.getTitulo() + " - Descrição: " + a.getDescricao() + " - Status: " + a.getStatus() + " - Prioridade: " + a.getPrioridade());
            }
        }
    }

    // --- NOVO MÉTODO: Ordenar por Prioridade ---
    private void ordenarPorPrioridade() {
        listModel.clear();
        if (currentListId == -1) {
            System.out.println("DEBUG: Contexto da lista comum não definido para ordenar.");
            return;
        }
        System.out.println("DEBUG: Ordenando anotações da Lista Comum por prioridade para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listarEOrdenarPorPrioridade(this.currentListId); // Chama o novo método DAO
        System.out.println("DEBUG: Anotações da Lista Comum ordenadas: " + lista.size());
        for (Anotacao a : lista) {
            if (a.getStatus().equals("Pendente") || a.getStatus().equals("Concluído")) {
                listModel.addElement("ID: " + a.getId() + " - Título: " + a.getTitulo() + " - Descrição: " + a.getDescricao() + " - Status: " + a.getStatus() + " - Prioridade: " + a.getPrioridade());
            }
        }
    }
    // --- FIM DO NOVO MÉTODO ---


    private void abrirCadastroListaComum() {
        JTextField titulo = new JTextField();
        JTextArea descricao = new JTextArea(5, 20);
        String[] statusOptions = {"Pendente", "Concluído"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        // --- NOVO: ComboBox para Prioridade ---
        String[] prioridadeOptions = {"POUCO_IMPORTANTE", "IMPORTANTE", "MUITO_IMPORTANTE"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem("POUCO_IMPORTANTE"); // Padrão
        // --- FIM NOVO ---

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        // --- NOVO: Adiciona o campo de Prioridade ---
        panel.add(new JLabel("Prioridade:"));
        panel.add(prioridadeBox);
        // --- FIM NOVO ---

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Lista Comum)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Passa a prioridade selecionada
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.currentListId, (String) prioridadeBox.getSelectedItem());
            anotacaoDAO.inserir(a);
            carregarAnotacoes();
        }
    }

    private void editarAnotacaoListaComum() {
        int selectedIndex = listaAnotacoes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma anotação para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedText = listModel.getElementAt(selectedIndex);
        int id = Integer.parseInt(selectedText.substring(selectedText.indexOf("ID: ") + 4, selectedText.indexOf(" - Título:")));

        List<Anotacao> todasAnotacoesComum = anotacaoDAO.listar(this.currentListId);
        Anotacao anotacaoParaEditar = null;
        for (Anotacao a : todasAnotacoesComum) {
            if (a.getId() == id) {
                anotacaoParaEditar = a;
                break;
            }
        }

        if (anotacaoParaEditar == null) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar anotação para edição.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField titulo = new JTextField(anotacaoParaEditar.getTitulo());
        JTextArea descricao = new JTextArea(anotacaoParaEditar.getDescricao());
        String[] statusOptions = {"Pendente", "Concluído"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);
        statusBox.setSelectedItem(anotacaoParaEditar.getStatus());

        // --- NOVO: ComboBox para Prioridade ---
        String[] prioridadeOptions = {"POUCO_IMPORTANTE", "IMPORTANTE", "MUITO_IMPORTANTE"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem(anotacaoParaEditar.getPrioridade()); // Pré-seleciona a prioridade atual
        // --- FIM NOVO ---

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        // --- NOVO: Adiciona o campo de Prioridade ---
        panel.add(new JLabel("Prioridade:"));
        panel.add(prioridadeBox);
        // --- FIM NOVO ---

        String[] options = {"Salvar", "Cancelar"};
        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Lista Comum)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            anotacaoParaEditar.setTitulo(titulo.getText());
            anotacaoParaEditar.setDescricao(descricao.getText());
            anotacaoParaEditar.setStatus((String) statusBox.getSelectedItem());
            anotacaoParaEditar.setPrioridade((String) prioridadeBox.getSelectedItem()); // Salva a nova prioridade
            anotacaoDAO.atualizar(anotacaoParaEditar);
            carregarAnotacoes();
        }
    }

    private void excluirAnotacaoListaComum() {
        int selectedIndex = listaAnotacoes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma anotação para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedText = listModel.getElementAt(selectedIndex);
        int id = Integer.parseInt(selectedText.substring(selectedText.indexOf("ID: ") + 4, selectedText.indexOf(" - Título:")));

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            anotacaoDAO.excluir(id, this.currentListId);
            carregarAnotacoes();
        }
    }
}