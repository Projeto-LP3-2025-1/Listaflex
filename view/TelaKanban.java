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
    private JPanel mainPanel;
    private JPanel kanbanPanel;
    private TelaListaComum listaComumPanel;

    private JPanel panelAFazer = new JPanel(new GridLayout(0, 1));
    private JPanel panelFazendo = new JPanel(new GridLayout(0, 1));
    private JPanel panelFeito = new JPanel(new GridLayout(0, 1));

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
        this.currentUserRole = collaboratorDAO.getUserRoleInList(this.currentListId, this.loggedInUserId); // Carrega o papel do usuário logado

        setTitle("Listaflex - " + currentListName + " (" + currentListType + ") - Papel: " + currentUserRole);
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
        coluna.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        coluna.add(new JScrollPane(panel), BorderLayout.CENTER);

        JButton btnNovaAnotacaoColuna = new JButton("+ Nova Anotação");
        // Habilita o botão apenas se tiver permissão de escrita
        if ("OWNER".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole)) {
            btnNovaAnotacaoColuna.addActionListener(e -> abrirCadastroKanbanComStatus(titulo));
        } else {
            btnNovaAnotacaoColuna.setEnabled(false); // Desabilita o botão
            btnNovaAnotacaoColuna.setToolTipText("Você não tem permissão para adicionar anotações.");
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
        listaComumPanel.setListContext(this.currentListId, this.currentListName); // Configura o contexto
        listaComumPanel.updatePermissions(currentUserRole); // Atualiza permissões dos botões da lista comum
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

    // CORREÇÃO: Novo método getter para o papel do usuário logado na lista atual
    public String getCurrentUserRole() {
        return this.currentUserRole;
    }

    // START OF SNIPPET: TelaKanban.java - abrirCadastroKanbanComStatus
    private void abrirCadastroKanbanComStatus(String statusInicial) {
    // DEBUG: Verificando papel antes de adicionar
    System.out.println("DEBUG: Tentando abrir cadastro Kanban. Papel atual: " + currentUserRole); // Adicionar esta linha
    if (!("OWNER".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole))) {
        JOptionPane.showMessageDialog(this, "Você não tem permissão para adicionar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
        System.out.println("DEBUG: Permissão NEGADA para adicionar anotação Kanban."); // Adicionar esta linha
        return;
    }
    System.out.println("DEBUG: Permissão CONCEDIDA para adicionar anotação Kanban."); // Adicionar esta linha

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
    System.out.println("DEBUG: Resultado do JOptionPane (Kanban): " + result); // Adicionar esta linha

    if (result == JOptionPane.OK_OPTION) {
        Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.currentListId, "POUCO_IMPORTANTE", false);
        System.out.println("DEBUG: Preparando para inserir Anotação Kanban no BD. Anotação: " + a.getTitulo() + ", ListID: " + a.getListId()); // Adicionar esta linha
        anotacaoDAO.inserir(a);
        System.out.println("DEBUG: Chamando carregarAnotacoesKanban() após tentativa de inserção."); // Adicionar esta linha
        carregarAnotacoesKanban();
    } else {
        System.out.println("DEBUG: Operação de Nova Anotação (Kanban) CANCELADA ou janela FECHADA."); // Adicionar esta linha
    }
}
// END OF SNIPPET: TelaKanban.java - abrirCadastroKanbanComStatus

    public void carregarAnotacoesKanban() {
        panelAFazer.removeAll();
        panelFazendo.removeAll();
        panelFeito.removeAll();

        System.out.println("DEBUG: Carregando anotações Kanban para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listar(this.currentListId);
        System.out.println("DEBUG: Anotações Kanban encontradas para ListID " + this.currentListId + ": " + lista.size());
        for (Anotacao a : lista) {
            if (a.getStatus().equals("AFazer") || a.getStatus().equals("Fazendo") || a.getStatus().equals("Feito")) {
                System.out.println("DEBUG:   - Anotação Kanban carregada: ID=" + a.getId() + ", Título='" + a.getTitulo() + "', Status='" + a.getStatus() + "', ListID=" + a.getListId() + ", Prioridade=" + a.getPrioridade());
                JButton botao = new JButton("<html><b>" + a.getTitulo() + "</b><br>" + a.getDescricao() + "</html>");
                if ("OWNER".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole)) {
                    botao.addActionListener(e -> editarAnotacaoKanban(a));
                } else {
                    botao.setEnabled(false);
                    botao.setToolTipText("Você não tem permissão para editar esta anotação.");
                }
                
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
        if (!("OWNER".equals(currentUserRole) || "ADMIN".equals(currentUserRole) || "EDITOR".equals(currentUserRole))) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para editar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

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

        String[] tipoListaOptions = {"KANBAN", "COMUM"};
        JComboBox<String> tipoListBox = new JComboBox<>(tipoListaOptions);
        tipoListBox.setSelectedItem(currentListType);

        panel.add(new JLabel("Mover para Tipo:"));
        panel.add(tipoListBox);


        String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};

        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (" + currentListName + ")",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            a.setTitulo(titulo.getText());
            a.setDescricao(descricao.getText());
            a.setStatus((String) statusBox.getSelectedItem());

            String novoTipoListaSelecionado = (String) tipoListBox.getSelectedItem();

            if (!novoTipoListaSelecionado.equals(currentListType)) {
                int confirmMove = JOptionPane.showConfirmDialog(this,
                        "Esta anotação será movida para uma lista do tipo '" + novoTipoListaSelecionado + "'. Deseja continuar?",
                        "Mover Anotação", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirmMove == JOptionPane.YES_OPTION) {
                    anotacaoDAO.excluir(a.getId(), this.currentListId); // Excluir da lista atual

                    UserList targetList = null;
                    List<UserList> userLists = userListDAO.listarPorUsuario(loggedInUserId);
                    for(UserList ul : userLists) {
                        if (ul.getListType().equals(novoTipoListaSelecionado)) {
                            targetList = ul;
                            break;
                        }
                    }

                    if (targetList == null) {
                        String newListName = JOptionPane.showInputDialog(this, "Você não tem uma lista '" + novoTipoListaSelecionado + "'. Digite um nome para criar uma nova:", "Criar Nova Lista", JOptionPane.PLAIN_MESSAGE);
                        if (newListName != null && !newListName.trim().isEmpty()) {
                            UserList newTargetList = new UserList(loggedInUserId, newListName.trim(), novoTipoListaSelecionado);
                            int newTargetListId = userListDAO.inserir(newTargetList);
                            if (newTargetListId != -1) {
                                targetList = userListDAO.getById(newTargetListId);
                                JOptionPane.showMessageDialog(this, "Nova lista '" + newListName + "' criada com sucesso!", "Lista Criada", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }

                    if (targetList != null) {
                        Anotacao newAnotacao = new Anotacao(a.getTitulo(), a.getDescricao(), a.getStatus(), targetList.getId(), a.getPrioridade(), a.isConcluidaVisual()); 
                        anotacaoDAO.inserir(newAnotacao);
                        JOptionPane.showMessageDialog(this, "Anotação movida para a lista '" + targetList.getListName() + "'.", "Anotação Movida", JOptionPane.INFORMATION_MESSAGE);
                        carregarAnotacoesKanban(); // Recarrega a view atual para remover a anotação
                    } else {
                        JOptionPane.showMessageDialog(this, "Não foi possível mover a anotação.", "Erro", JOptionPane.ERROR_MESSAGE);
                        anotacaoDAO.atualizar(a); // Se não moveu, salva as edições na lista original
                    }

                } else {
                    anotacaoDAO.atualizar(a); // Salva as edições na lista original
                    carregarAnotacoesKanban();
                }

            } else { // O tipo da lista NÃO mudou, apenas editou a anotação na lista atual
                a.setListId(currentListId);
                anotacaoDAO.atualizar(a);
                carregarAnotacoesKanban(); // Recarrega a view Kanban
            }


        } else if (result == 1) { // Clicou em "Excluir Anotação"
            if (!"OWNER".equals(currentUserRole) && !"ADMIN".equals(currentUserRole)) {
                JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirmExcluir = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmExcluir == JOptionPane.YES_OPTION) {
                anotacaoDAO.excluir(a.getId(), this.currentListId);
                carregarAnotacoesKanban();
            }
        }
    }
}
// END OF FILE: TelaKanban.java