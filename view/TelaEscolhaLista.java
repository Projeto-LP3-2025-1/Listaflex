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

        // Painel Superior: Título e Botão de Deslogar
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

        // Centro: JList de Listas do Usuário
        listModel = new DefaultListModel<>();
        userListsJList = new JList<>(listModel);
        userListsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListsJList.setFont(new Font("Arial", Font.PLAIN, 16));
        userListsJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                UserList ul = (UserList) value;
                String role = collaboratorDAO.getUserRoleInList(ul.getId(), loggedInUserId);
                if (role == null || role.isEmpty()) {
                     role = "Desconhecido"; // Fallback caso não encontre o papel
                }
                setText(ul.getListName() + " (" + ul.getListType() + ") - Papel: " + role);
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
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // <-- Este é o painel dos botões
        
        JButton btnAbrirLista = new JButton("Abrir Lista");
        btnAbrirLista.addActionListener(e -> {
            UserList selectedList = userListsJList.getSelectedValue();
            if (selectedList == null || selectedList.getId() == 0 || selectedList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
                JOptionPane.showMessageDialog(this, "Selecione uma lista para abrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            abrirListaSelecionada(selectedList);
        });
        bottomPanel.add(btnAbrirLista); // <-- O botão é ADICIONADO AQUI

        JButton btnCriarNovaLista = new JButton("Criar Nova Lista");
        btnCriarNovaLista.addActionListener(e -> criarNovaLista());
        bottomPanel.add(btnCriarNovaLista); // <-- O botão é ADICIONADO AQUI

        JButton btnEditarLista = new JButton("Editar Lista");
        btnEditarLista.addActionListener(e -> editarListaSelecionada());
        bottomPanel.add(btnEditarLista); // <-- O botão é ADICIONADO AQUI

        JButton btnExcluirLista = new JButton("Excluir Lista");
        btnExcluirLista.addActionListener(e -> excluirListaSelecionada());
        bottomPanel.add(btnExcluirLista); // <-- O botão é ADICIONADO AQUI

        JButton btnGerenciarColaboradores = new JButton("Gerenciar Colaboradores");
        btnGerenciarColaboradores.addActionListener(e -> gerenciarColaboradores());
        bottomPanel.add(btnGerenciarColaboradores); // <-- O botão é ADICIONADO AQUI

        add(bottomPanel, BorderLayout.SOUTH); // <-- O painel com os botões é ADICIONADO ao frame

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
            int newId = userListDAO.inserir(newUserList); // Inserir UserList e definir owner
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
        if (!"OWNER".equals(userRole) && !"ADMIN".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para editar esta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField listNameField = new JTextField(selectedList.getListName(), 20);
        String[] listTypes = {"KANBAN", "COMUM"};
        JComboBox<String> listTypeBox = new JComboBox<>(listTypes);
        listTypeBox.setSelectedItem(selectedList.getListType());
        listTypeBox.setEnabled(false); // Tipo de lista não pode ser alterado após a criação

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
        if (!"OWNER".equals(userRole)) { // Apenas o OWNER pode excluir a lista
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
        if (userRole == null) { // Se não encontrou o papel, o usuário não tem acesso
            JOptionPane.showMessageDialog(this, "Você não tem acesso a esta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose(); // Fecha a tela de gerenciamento de listas
        new TelaKanban(loggedInUserId, userList.getListType(), userList.getId(), userList.getListName());
    }

    // Este é o método para gerenciar colaboradores, ele abre a TelaGerenciarColaboradores
    private void gerenciarColaboradores() {
        UserList selectedList = userListsJList.getSelectedValue();
        if (selectedList == null || selectedList.getId() == 0 || selectedList.getListName().equals("Nenhuma lista encontrada. Crie uma!")) {
            JOptionPane.showMessageDialog(this, "Selecione uma lista para gerenciar colaboradores.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userRole = collaboratorDAO.getUserRoleInList(selectedList.getId(), loggedInUserId);
        if (!"OWNER".equals(userRole) && !"ADMIN".equals(userRole)) { // Apenas OWNER ou ADMIN podem gerenciar
            JOptionPane.showMessageDialog(this, "Você não tem permissão para gerenciar colaboradores desta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Se a permissão é OK, abre a nova tela de gerenciamento
        new TelaGerenciarColaboradores(this, selectedList, loggedInUserId);
    }
}
// END OF FILE: TelaEscolhaLista.java