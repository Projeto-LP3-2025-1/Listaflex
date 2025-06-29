// START OF FILE: TelaEscolhaLista.java
package view;

import dao.UserListDAO;
import dao.ListCollaboratorDAO;
import model.UserList;
import model.ListCollaborator;
import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class TelaEscolhaLista extends JFrame {
    // --- CONSTANTES DE CORES DA PALETA PROFISSIONAL ---
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50"); // Azul Marinho Escuro
    private static final Color ACCENT_COLOR = Color.decode("#3498DB"); // Azul Céu
    private static final Color BACKGROUND_COLOR_LIGHT = Color.decode("#ECF0F1"); // Fundo claro
    private static final Color TEXT_COLOR_DARK = Color.decode("#34495E"); // Texto principal
    private static final Color BORDER_COLOR_NEUTRAL = Color.decode("#BDC3C7"); // Bordas
    // --- FIM CONSTANTES DE CORES ---

    private int loggedInUserId;
    private UserListDAO userListDAO;
    private ListCollaboratorDAO collaboratorDAO;
    private UserDAO userDAO;

    private DefaultListModel<UserList> listModel;
    private JList<UserList> userListsJList;

    public TelaEscolhaLista(int userId) {
        System.out.println("DEBUG: Construtor TelaEscolhaLista iniciado para UserID: " + userId);
        this.loggedInUserId = userId;
        this.userListDAO = new UserListDAO();
        this.collaboratorDAO = new ListCollaboratorDAO();
        this.userDAO = new UserDAO();

        setTitle("Gerenciar Listas - Usuário ID: " + userId);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR_LIGHT); // Fundo geral do frame

        // Painel Superior: Título e Botão de Deslogar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY_COLOR); // Fundo do painel superior
        JLabel titleLabel = new JLabel("Minhas Listas", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE); // Texto branco
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton btnDeslogar = new JButton("Deslogar");
        btnDeslogar.setBackground(ACCENT_COLOR); // Cor do botão
        btnDeslogar.setForeground(Color.WHITE);
        btnDeslogar.setFont(new Font("Arial", Font.BOLD, 12));
        btnDeslogar.setOpaque(true);
        btnDeslogar.setBorderPainted(false);
        btnDeslogar.setFocusPainted(false);
        btnDeslogar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja deslogar?", "Deslogar",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new TelaLogin();
            }
        });
        topPanel.add(btnDeslogar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Centro: JList de Listas do Usuário
        listModel = new DefaultListModel<>();
        userListsJList = new JList<>(listModel);
        userListsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListsJList.setFont(new Font("Arial", Font.PLAIN, 16));
        userListsJList.setBackground(Color.WHITE); // Fundo da lista
        userListsJList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL)); // Borda da lista

        userListsJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                UserList ul = (UserList) value;
                String role = collaboratorDAO.getUserRoleInList(ul.getId(), loggedInUserId);
                if (role == null || role.isEmpty()) {
                     role = "Desconhecido"; // Fallback caso não encontre o papel
                }
                setText(ul.getListName() + " (" + ul.getListType() + ") - Papel: " + role); // Exibe o papel
                setForeground(TEXT_COLOR_DARK); // Cor do texto da lista
                if (isSelected) {
                    setBackground(ACCENT_COLOR); // Cor de seleção
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });

        // MouseListener para clique DUPLO para abrir
        userListsJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Reage apenas a clique DUPLO
                    int index = userListsJList.locationToIndex(evt.getPoint());
                    if (index != -1) {
                        UserList selectedList = listModel.getElementAt(index);
                        abrirListaSelecionada(selectedList);
                    }
                }
            }
        });

        add(new JScrollPane(userListsJList), BorderLayout.CENTER);

        // Painel Inferior: Botões de Ação
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel de botões

        JButton btnAbrirLista = new JButton("Abrir Lista");
        btnAbrirLista.setBackground(PRIMARY_COLOR);
        btnAbrirLista.setForeground(Color.WHITE);
        btnAbrirLista.setFont(new Font("Arial", Font.BOLD, 14));
        btnAbrirLista.setOpaque(true);
        btnAbrirLista.setBorderPainted(false);
        btnAbrirLista.setFocusPainted(false);
        btnAbrirLista.addActionListener(e -> {
            UserList selectedList = userListsJList.getSelectedValue();
            if (selectedList == null || selectedList.getId() == 0 || selectedList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
                JOptionPane.showMessageDialog(this, "Selecione uma lista para abrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            abrirListaSelecionada(selectedList);
        });
        bottomPanel.add(btnAbrirLista);

        JButton btnCriarNovaLista = new JButton("Criar Nova Lista");
        btnCriarNovaLista.setBackground(ACCENT_COLOR); // Cor de destaque
        btnCriarNovaLista.setForeground(Color.WHITE);
        btnCriarNovaLista.setFont(new Font("Arial", Font.BOLD, 14));
        btnCriarNovaLista.setOpaque(true);
        btnCriarNovaLista.setBorderPainted(false);
        btnCriarNovaLista.setFocusPainted(false);
        btnCriarNovaLista.addActionListener(e -> criarNovaLista());
        bottomPanel.add(btnCriarNovaLista);

        JButton btnEditarLista = new JButton("Editar Lista");
        btnEditarLista.setBackground(ACCENT_COLOR);
        btnEditarLista.setForeground(Color.WHITE);
        btnEditarLista.setFont(new Font("Arial", Font.BOLD, 14));
        btnEditarLista.setOpaque(true);
        btnEditarLista.setBorderPainted(false);
        btnEditarLista.setFocusPainted(false);
        btnEditarLista.addActionListener(e -> editarListaSelecionada());
        bottomPanel.add(btnEditarLista);

        JButton btnExcluirLista = new JButton("Excluir Lista");
        btnExcluirLista.setBackground(ACCENT_COLOR);
        btnExcluirLista.setForeground(Color.WHITE);
        btnExcluirLista.setFont(new Font("Arial", Font.BOLD, 14));
        btnExcluirLista.setOpaque(true);
        btnExcluirLista.setBorderPainted(false);
        btnExcluirLista.setFocusPainted(false);
        btnExcluirLista.addActionListener(e -> excluirListaSelecionada());
        bottomPanel.add(btnExcluirLista);

        JButton btnGerenciarColaboradores = new JButton("Gerenciar Colaboradores");
        btnGerenciarColaboradores.setBackground(ACCENT_COLOR);
        btnGerenciarColaboradores.setForeground(Color.WHITE);
        btnGerenciarColaboradores.setFont(new Font("Arial", Font.BOLD, 14));
        btnGerenciarColaboradores.setOpaque(true);
        btnGerenciarColaboradores.setBorderPainted(false);
        btnGerenciarColaboradores.setFocusPainted(false);
        btnGerenciarColaboradores.addActionListener(e -> gerenciarColaboradores());
        bottomPanel.add(btnGerenciarColaboradores);

        add(bottomPanel, BorderLayout.SOUTH);

        carregarListas();
        System.out.println("DEBUG: TelaEscolhaLista carregou listas e está tentando ser visível.");
        setVisible(true);
    }

    private void carregarListas() {
        System.out.println("DEBUG: Chamando carregarListas na TelaEscolhaLista.");
        listModel.clear();
        List<UserList> listas = userListDAO.listarPorUsuario(loggedInUserId);
        System.out.println("DEBUG: " + listas.size() + " listas encontradas para UserID: " + loggedInUserId);
        for (UserList ul : listas) {
            listModel.addElement(ul);
            System.out.println("DEBUG: Adicionando lista à JList: " + ul.getListName() + " (Owner ID: " + ul.getUserId() + ")");
        }
        if (listas.isEmpty()) {
            listModel.addElement(new UserList(0, loggedInUserId, "Nenhuma lista encontrada. Crie uma!", "")); // Dummy item com ID 0
        }
    }

    private void criarNovaLista() {
        JTextField listNameField = new JTextField(20);
        listNameField.setBackground(Color.WHITE);
        listNameField.setForeground(TEXT_COLOR_DARK);
        listNameField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));

        String[] listTypes = {"KANBAN", "COMUM"};
        JComboBox<String> listTypeBox = new JComboBox<>(listTypes);
        listTypeBox.setBackground(Color.WHITE);
        listTypeBox.setForeground(TEXT_COLOR_DARK);
        listTypeBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel do diálogo
        panel.add(new JLabel("Nome da Lista:", SwingConstants.LEFT)).setForeground(TEXT_COLOR_DARK); // Label com cor
        panel.add(listNameField);
        panel.add(new JLabel("Tipo de Lista:", SwingConstants.LEFT)).setForeground(TEXT_COLOR_DARK); // Label com cor
        panel.add(listTypeBox);

        // Define a cor de fundo do JOptionPane em si (se possível, depende do L&F)
        UIManager.put("OptionPane.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Panel.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Button.background", ACCENT_COLOR); // Botões do JOptionPane
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));


        int result = JOptionPane.showConfirmDialog(this, panel, "Criar Nova Lista",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Reseta as propriedades do UIManager após o diálogo para não afetar outros componentes
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);


        if (result == JOptionPane.OK_OPTION) {
            String listName = listNameField.getText().trim();
            String listType = (String) listTypeBox.getSelectedItem();

            if (listName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome da lista não pode ser vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            UserList newUserList = new UserList(loggedInUserId, listName, listType);
            int newId = userListDAO.inserir(newUserList);
            if (newId != -1) {
                JOptionPane.showMessageDialog(this, "Lista criada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarListas();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao criar lista. Tente outro nome.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarListaSelecionada() {
        UserList selectedList = userListsJList.getSelectedValue();
        if (selectedList == null || selectedList.getId() == 0 || selectedList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
            JOptionPane.showMessageDialog(this, "Selecione uma lista válida para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userRole = collaboratorDAO.getUserRoleInList(selectedList.getId(), loggedInUserId);
        if (!"Criador(a)".equals(userRole) && !"ADMIN".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para editar esta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField listNameField = new JTextField(selectedList.getListName(), 20);
        listNameField.setBackground(Color.WHITE);
        listNameField.setForeground(TEXT_COLOR_DARK);
        listNameField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));

        String[] listTypes = {"KANBAN", "COMUM"};
        JComboBox<String> listTypeBox = new JComboBox<>(listTypes);
        listTypeBox.setSelectedItem(selectedList.getListType());
        listTypeBox.setEnabled(false); // Tipo de lista não pode ser alterado após a criação
        listTypeBox.setBackground(Color.WHITE);
        listTypeBox.setForeground(TEXT_COLOR_DARK);
        listTypeBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));


        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel do diálogo
        panel.add(new JLabel("Nome da Lista:", SwingConstants.LEFT)).setForeground(TEXT_COLOR_DARK);
        panel.add(listNameField);
        panel.add(new JLabel("Tipo de Lista:", SwingConstants.LEFT)).setForeground(TEXT_COLOR_DARK);
        panel.add(listTypeBox);

        // Define a cor de fundo do JOptionPane em si
        UIManager.put("OptionPane.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Panel.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Button.background", ACCENT_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));


        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Lista",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Reseta as propriedades do UIManager
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);


        if (result == JOptionPane.OK_OPTION) {
            String newListName = listNameField.getText().trim();
            if (newListName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome da lista não pode ser vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedList.setListName(newListName);

            if (userListDAO.atualizar(selectedList)) {
                JOptionPane.showMessageDialog(this, "Lista atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarListas();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar lista. Tente outro nome.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void excluirListaSelecionada() {
        UserList selectedList = userListsJList.getSelectedValue();
        if (selectedList == null || selectedList.getId() == 0 || selectedList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
            JOptionPane.showMessageDialog(this, "Selecione uma lista válida para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userRole = collaboratorDAO.getUserRoleInList(selectedList.getId(), loggedInUserId);
        if (!"Criador(a)".equals(userRole)) { // Apenas o Criador(a) pode excluir a lista
            JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir esta lista. Apenas o criador pode excluir.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR a lista '" + selectedList.getListName() + "' e TODAS as suas anotações?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userListDAO.excluir(selectedList.getId(), loggedInUserId)) {
                JOptionPane.showMessageDialog(this, "Lista excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarListas();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir lista. Certifique-se de que você é o criador.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirListaSelecionada(UserList userList) {
        if (userList.getId() == 0 || userList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
            JOptionPane.showMessageDialog(this, "Por favor, crie uma lista antes de tentar abri-la.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String userRole = collaboratorDAO.getUserRoleInList(userList.getId(), loggedInUserId);
        if (userRole == null) {
            JOptionPane.showMessageDialog(this, "Você não tem acesso a esta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        new TelaKanban(loggedInUserId, userList.getListType(), userList.getId(), userList.getListName());
    }

    private void gerenciarColaboradores() {
        UserList selectedList = userListsJList.getSelectedValue();
        if (selectedList == null || selectedList.getId() == 0 || selectedList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
            JOptionPane.showMessageDialog(this, "Selecione uma lista para gerenciar colaboradores.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userRole = collaboratorDAO.getUserRoleInList(selectedList.getId(), loggedInUserId);
        if (!"Criador(a)".equals(userRole) && !"ADMIN".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para gerenciar colaboradores desta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new TelaGerenciarColaboradores(this, selectedList, loggedInUserId);
    }
}
// END OF FILE: TelaEscolhaLista.java