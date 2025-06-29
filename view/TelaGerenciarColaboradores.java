// START OF FILE: TelaGerenciarColaboradores.java
package view;

import dao.ListCollaboratorDAO;
import dao.UserDAO;
import model.ListCollaborator;
import model.User;
import model.UserList;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaGerenciarColaboradores extends JDialog {
    // --- CONSTANTES DE CORES DA PALETA PROFISSIONAL ---
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50"); // Azul Marinho Escuro
    private static final Color ACCENT_COLOR = Color.decode("#3498DB"); // Azul Céu
    private static final Color BACKGROUND_COLOR_LIGHT = Color.decode("#ECF0F1"); // Fundo claro
    private static final Color TEXT_COLOR_DARK = Color.decode("#34495E"); // Texto principal
    private static final Color BORDER_COLOR_NEUTRAL = Color.decode("#BDC3C7"); // Bordas
    // --- FIM CONSTANTES DE CORES ---

    private UserList list; // A lista que está sendo gerenciada
    private int loggedInUserId; // O ID do usuário logado
    private ListCollaboratorDAO collaboratorDAO;
    private UserDAO userDAO;

    private DefaultListModel<ListCollaborator> collaboratorsListModel;
    private JList<ListCollaborator> collaboratorsJList;

    public TelaGerenciarColaboradores(JFrame parent, UserList list, int loggedInUserId) {
        super(parent, "Gerenciar Colaboradores para '" + list.getListName() + "'", true);
        this.list = list;
        this.loggedInUserId = loggedInUserId;
        this.collaboratorDAO = new ListCollaboratorDAO();
        this.userDAO = new UserDAO();

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR_LIGHT); // Fundo geral do diálogo

        // Lista de Colaboradores
        collaboratorsListModel = new DefaultListModel<>();
        collaboratorsJList = new JList<>(collaboratorsListModel);
        collaboratorsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        collaboratorsJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ListCollaborator lc = (ListCollaborator) value;
                User user = userDAO.getUserById(lc.getUserId()); // Busca o nome do usuário pelo ID
                String username = (user != null) ? user.getUsername() : "Usuário Desconhecido";
                setText(username + " - Papel: " + lc.getRole());
                setForeground(TEXT_COLOR_DARK); // Cor do texto da lista
                if (isSelected) {
                    setBackground(ACCENT_COLOR); // Cor de seleção
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });
        collaboratorsJList.setBackground(Color.WHITE);
        collaboratorsJList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));
        add(new JScrollPane(collaboratorsJList), BorderLayout.CENTER);

        // Painel de Ações
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionsPanel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel de ações
        
        JButton btnAdicionar = new JButton("Adicionar Colaborador");
        btnAdicionar.setBackground(ACCENT_COLOR);
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAdicionar.setOpaque(true);
        btnAdicionar.setBorderPainted(false);
        btnAdicionar.setFocusPainted(false);
        btnAdicionar.addActionListener(e -> adicionarColaborador());
        actionsPanel.add(btnAdicionar);

        JButton btnMudarPapel = new JButton("Mudar Papel");
        btnMudarPapel.setBackground(ACCENT_COLOR);
        btnMudarPapel.setForeground(Color.WHITE);
        btnMudarPapel.setFont(new Font("Arial", Font.BOLD, 12));
        btnMudarPapel.setOpaque(true);
        btnMudarPapel.setBorderPainted(false);
        btnMudarPapel.setFocusPainted(false);
        btnMudarPapel.addActionListener(e -> mudarPapelColaborador());
        actionsPanel.add(btnMudarPapel);

        JButton btnRemover = new JButton("Remover Colaborador");
        btnRemover.setBackground(ACCENT_COLOR);
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setFont(new Font("Arial", Font.BOLD, 12));
        btnRemover.setOpaque(true);
        btnRemover.setBorderPainted(false);
        btnRemover.setFocusPainted(false);
        btnRemover.addActionListener(e -> removerColaborador());
        actionsPanel.add(btnRemover);

        add(actionsPanel, BorderLayout.SOUTH);

        loadCollaborators();
        setVisible(true);
    }

    private void loadCollaborators() {
        collaboratorsListModel.clear();
        List<ListCollaborator> collaborators = collaboratorDAO.getCollaboratorsByList(list.getId());
        for (ListCollaborator lc : collaborators) {
            collaboratorsListModel.addElement(lc);
        }
        if (collaborators.isEmpty()) {
            collaboratorsListModel.addElement(new ListCollaborator(0, 0, "Nenhum colaborador ainda."));
        }
    }

    private void adicionarColaborador() {
        String username = JOptionPane.showInputDialog(this, "Digite o nome de usuário do colaborador:", "Adicionar Colaborador", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        User userToAdd = userDAO.getUserByUsername(username);
        if (userToAdd == null) {
            JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userToAdd.getId() == loggedInUserId) {
            JOptionPane.showMessageDialog(this, "Você não pode adicionar a si mesmo como colaborador. Você já é o criador/administrador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] roles = {"ADMIN", "EDITOR", "VIEWER"}; // Papéis em inglês
        String selectedRole = (String) JOptionPane.showInputDialog(this, "Escolha o papel para " + userToAdd.getUsername() + ":", "Definir Papel",
                JOptionPane.PLAIN_MESSAGE, null, roles, roles[2]);
        if (selectedRole == null) {
            return;
        }

        ListCollaborator newCollaborator = new ListCollaborator(list.getId(), userToAdd.getId(), selectedRole);
        if (collaboratorDAO.addCollaborator(newCollaborator)) {
            JOptionPane.showMessageDialog(this, "Colaborador adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadCollaborators();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar colaborador. Ele já pode ser um colaborador desta lista.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mudarPapelColaborador() {
        ListCollaborator selectedCollaborator = collaboratorsJList.getSelectedValue();
        if (selectedCollaborator == null || selectedCollaborator.getListId() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione um colaborador para mudar o papel.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedCollaborator.getUserId() == list.getUserId() && !"Criador(a)".equals(selectedCollaborator.getRole())) { // Voltou para Criador(a)
            JOptionPane.showMessageDialog(this, "Não é possível mudar o papel do criador da lista para algo diferente de Criador(a) por esta tela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCollaborator.getUserId() == loggedInUserId && !"Criador(a)".equals(selectedCollaborator.getRole())) { // Voltou para Criador(a)
             JOptionPane.showMessageDialog(this, "Você não pode mudar seu próprio papel se não for o Criador(a).", "Aviso", JOptionPane.WARNING_MESSAGE);
             return;
        }


        String[] roles = {"ADMIN", "EDITOR", "VIEWER"}; // Voltou para ADMIN, EDITOR, VIEWER
        String selectedRole = (String) JOptionPane.showInputDialog(this, "Escolha o novo papel para " + userDAO.getUserById(selectedCollaborator.getUserId()).getUsername() + ":", "Mudar Papel",
                JOptionPane.PLAIN_MESSAGE, null, roles, selectedCollaborator.getRole());
        if (selectedRole == null) {
            return;
        }

        if (collaboratorDAO.updateCollaboratorRole(selectedCollaborator.getListId(), selectedCollaborator.getUserId(), selectedRole)) {
            JOptionPane.showMessageDialog(this, "Papel do colaborador atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadCollaborators();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao mudar papel do colaborador.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerColaborador() {
        ListCollaborator selectedCollaborator = collaboratorsJList.getSelectedValue();
        if (selectedCollaborator == null || selectedCollaborator.getListId() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione um colaborador para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCollaborator.getUserId() == loggedInUserId) {
            JOptionPane.showMessageDialog(this, "Você não pode remover a si mesmo da lista por esta opção. Use a opção 'Excluir Lista' se for o Criador(a).", "Aviso", JOptionPane.WARNING_MESSAGE); // Voltou para Criador(a)
            return;
        }
        if (selectedCollaborator.getUserId() == list.getUserId() && "Criador(a)".equals(selectedCollaborator.getRole())) { // Voltou para Criador(a)
            JOptionPane.showMessageDialog(this, "Você não pode remover o criador da lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja remover " + userDAO.getUserById(selectedCollaborator.getUserId()).getUsername() + " desta lista?",
                "Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (collaboratorDAO.removeCollaborator(selectedCollaborator.getListId(), selectedCollaborator.getUserId())) {
                JOptionPane.showMessageDialog(this, "Colaborador removido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                loadCollaborators();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao remover colaborador.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}