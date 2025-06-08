package view;

import dao.UserListDAO;
import model.UserList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TelaEscolhaLista extends JFrame {
    private int loggedInUserId;
    private UserListDAO userListDAO;
    private DefaultListModel<UserList> listModel;
    private JList<UserList> userListsJList;

    public TelaEscolhaLista(int userId) {
        System.out.println("DEBUG: Construtor TelaEscolhaLista iniciado para UserID: " + userId); // DEBUG AQUI
        this.loggedInUserId = userId;
        this.userListDAO = new UserListDAO();
        setTitle("Gerenciar Listas - Usuário ID: " + userId);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // --- Painel Superior: Título e Botão de Deslogar ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Minhas Listas", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton btnDeslogar = new JButton("Deslogar");
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

        // --- Centro: JList de Listas do Usuário ---
        listModel = new DefaultListModel<>();
        userListsJList = new JList<>(listModel);
        userListsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Seleção única
        userListsJList.setFont(new Font("Arial", Font.PLAIN, 16));
        userListsJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                UserList ul = (UserList) value;
                setText(ul.getListName() + " (" + ul.getListType() + ")");
                return this;
            }
        });

        // Adiciona o listener para clique duplo para abrir a lista
        userListsJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userListsJList.getSelectedIndex() != -1) {
                UserList selectedList = userListsJList.getSelectedValue();
                if (selectedList != null) {
                    abrirListaSelecionada(selectedList);
                }
            }
        });
        userListsJList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click
                    int index = userListsJList.locationToIndex(evt.getPoint());
                    if (index != -1) {
                        UserList selectedList = listModel.getElementAt(index);
                        abrirListaSelecionada(selectedList);
                    }
                }
            }
        });


        add(new JScrollPane(userListsJList), BorderLayout.CENTER); // Adiciona a lista com barra de rolagem

        // --- Painel Inferior: Botões de Ação ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Com espaçamento
        JButton btnCriarNovaLista = new JButton("Criar Nova Lista");
        btnCriarNovaLista.addActionListener(e -> criarNovaLista());
        bottomPanel.add(btnCriarNovaLista);

        JButton btnEditarLista = new JButton("Editar Lista");
        btnEditarLista.addActionListener(e -> editarListaSelecionada());
        bottomPanel.add(btnEditarLista);

        JButton btnExcluirLista = new JButton("Excluir Lista");
        btnExcluirLista.addActionListener(e -> excluirListaSelecionada());
        bottomPanel.add(btnExcluirLista);

        add(bottomPanel, BorderLayout.SOUTH);

       carregarListas(); // Carrega as listas ao iniciar a tela
        System.out.println("DEBUG: TelaEscolhaLista carregou listas e está tentando ser visível."); // DEBUG AQUI
        setVisible(true);
    }

    private void carregarListas() {
        System.out.println("DEBUG: Chamando carregarListas na TelaEscolhaLista."); // DEBUG AQUI
        listModel.clear();
        List<UserList> listas = userListDAO.listarPorUsuario(loggedInUserId);
        System.out.println("DEBUG: " + listas.size() + " listas encontradas para UserID: " + loggedInUserId); // DEBUG AQUI
        for (UserList ul : listas) {
            listModel.addElement(ul);
            System.out.println("DEBUG: Adicionando lista à JList: " + ul.getListName()); // DEBUG AQUI
        }
        if (listas.isEmpty()) {
            // Adicionar um item dummy para indicar que não há listas, mas sem ID -1 que pode causar problemas
            listModel.addElement(new UserList(loggedInUserId, "Nenhuma lista encontrada. Crie uma!", "")); // Dummy item com ID de usuário, mas ID de lista 0 (ou outro valor sentinela)
            // Para evitar NullPointerException ou erros de ID -1 ao tentar usar este dummy item:
            // Se o ID da lista for -1 no UserList, trate-o como não selecionável para edição/exclusão.
        }
    }

    private void criarNovaLista() {
        JTextField listNameField = new JTextField(20);
        String[] listTypes = {"KANBAN", "COMUM"};
        JComboBox<String> listTypeBox = new JComboBox<>(listTypes);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nome da Lista:"));
        panel.add(listNameField);
        panel.add(new JLabel("Tipo de Lista:"));
        panel.add(listTypeBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Criar Nova Lista",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

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
                carregarListas(); // Recarrega a lista
                // Opcional: Abrir a nova lista automaticamente
                // abrirListaSelecionada(newUserList);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao criar lista. Tente outro nome.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarListaSelecionada() {
        UserList selectedList = userListsJList.getSelectedValue();
        if (selectedList == null || selectedList.getId() == -1) { // -1 para o dummy item
            JOptionPane.showMessageDialog(this, "Selecione uma lista para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField listNameField = new JTextField(selectedList.getListName(), 20);
        String[] listTypes = {"KANBAN", "COMUM"};
        JComboBox<String> listTypeBox = new JComboBox<>(listTypes);
        listTypeBox.setSelectedItem(selectedList.getListType());
        listTypeBox.setEnabled(false); // Tipo de lista não pode ser alterado após a criação (ou implemente lógica de migração)

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nome da Lista:"));
        panel.add(listNameField);
        panel.add(new JLabel("Tipo de Lista:"));
        panel.add(listTypeBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Lista",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newListName = listNameField.getText().trim();
            if (newListName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome da lista não pode ser vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedList.setListName(newListName);
            // selectedList.setListType((String) listTypeBox.getSelectedItem()); // Não editável por padrão

            if (userListDAO.atualizar(selectedList)) {
                JOptionPane.showMessageDialog(this, "Lista atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarListas();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar lista.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void excluirListaSelecionada() {
        UserList selectedList = userListsJList.getSelectedValue();
        if (selectedList == null || selectedList.getId() == -1) { // -1 para o dummy item
            JOptionPane.showMessageDialog(this, "Selecione uma lista para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Erro ao excluir lista.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirListaSelecionada(UserList userList) {
        dispose(); // Fecha a tela de gerenciamento de listas
        new TelaKanban(loggedInUserId, userList.getListType(), userList.getId(), userList.getListName()); // Passa todos os detalhes da lista
    }
}