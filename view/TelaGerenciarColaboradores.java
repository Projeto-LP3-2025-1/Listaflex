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
    private UserList list;
    private int loggedInUserId;
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

        // Lista de Colaboradores
        collaboratorsListModel = new DefaultListModel<>();
        collaboratorsJList = new JList<>(collaboratorsListModel);
        collaboratorsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        collaboratorsJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ListCollaborator lc = (ListCollaborator) value;
                User user = userDAO.getUserById(lc.getUserId()); // Busca o nome do usuário
                String username = (user != null) ? user.getUsername() : "Usuário Desconhecido";
                setText(username + " - Papel: " + lc.getRole());
                return this;
            }
        });
        add(new JScrollPane(collaboratorsJList), BorderLayout.CENTER);

        // Painel de Ações
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAdicionar = new JButton("Adicionar Colaborador");
        btnAdicionar.addActionListener(e -> adicionarColaborador());
        actionsPanel.add(btnAdicionar);

        JButton btnMudarPapel = new JButton("Mudar Papel");
        btnMudarPapel.addActionListener(e -> mudarPapelColaborador());
        actionsPanel.add(btnMudarPapel);

        JButton btnRemover = new JButton("Remover Colaborador");
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
            collaboratorsListModel.addElement(new ListCollaborator(0, 0, "Nenhum colaborador ainda.")); // Dummy item
        }
    }

    private void adicionarColaborador() {
        String username = JOptionPane.showInputDialog(this, "Digite o nome de usuário do colaborador:", "Adicionar Colaborador", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        User userToAdd = userDAO.getUserByUsername(username); // Modificado para buscar por username
        if (userToAdd == null) {
            JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userToAdd.getId() == loggedInUserId) {
            JOptionPane.showMessageDialog(this, "Você não pode adicionar a si mesmo como colaborador. Você já é o criador/administrador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] roles = {"ADMIN", "EDITOR", "VIEWER"};
        String selectedRole = (String) JOptionPane.showInputDialog(this, "Escolha o papel para " + userToAdd.getUsername() + ":", "Definir Papel",
                JOptionPane.PLAIN_MESSAGE, null, roles, roles[2]); // Padrão VIEW
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
        if (selectedCollaborator == null || selectedCollaborator.getListId() == 0) { // Verifica dummy item
            JOptionPane.showMessageDialog(this, "Selecione um colaborador para mudar o papel.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedCollaborator.getUserId() == list.getUserId() && !"OWNER".equals(selectedCollaborator.getRole())) {
            JOptionPane.showMessageDialog(this, "Não é possível mudar o papel do criador da lista para algo diferente de OWNER por esta tela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCollaborator.getUserId() == loggedInUserId && !"OWNER".equals(selectedCollaborator.getRole())) {
             JOptionPane.showMessageDialog(this, "Você não pode mudar seu próprio papel se não for o OWNER.", "Aviso", JOptionPane.WARNING_MESSAGE);
             return;
        }


        String[] roles = {"ADMIN", "EDITOR", "VIEWER"}; // OWNER não é editável aqui
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
        if (selectedCollaborator == null || selectedCollaborator.getListId() == 0) { // Verifica dummy item
            JOptionPane.showMessageDialog(this, "Selecione um colaborador para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCollaborator.getUserId() == loggedInUserId) {
            JOptionPane.showMessageDialog(this, "Você não pode remover a si mesmo da lista por esta opção. Use a opção 'Excluir Lista' se for o criador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCollaborator.getUserId() == list.getUserId() && "OWNER".equals(selectedCollaborator.getRole())) {
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
// END OF FILE: TelaGerenciarColaboradores.java