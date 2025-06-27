// START OF FILE: TelaListaComum.java
package view;

import dao.AnotacaoDAO;
import model.Anotacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList; // ArrayList ainda é usado aqui internamente para List
import java.util.List;

public class TelaListaComum extends JPanel {
    private JList<Anotacao> listaAnotacoes;
    private DefaultListModel<Anotacao> listModel;
    private AnotacaoDAO anotacaoDAO = new AnotacaoDAO();
    private int loggedInUserId;
    private int currentListId;
    private String currentListName;

    private TelaKanban mainFrame;
    // Removida a ArrayList<Boolean> concluidaStatusVisual, pois o estado agora está no objeto Anotacao

    // Botões como atributos de classe para poderem ser habilitados/desabilitados
    private JButton btnNova;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnOrdenarPrioridade;

    // Construtor principal
    public TelaListaComum(int userId, int listId, String listName, TelaKanban mainFrame) {
        this.loggedInUserId = userId;
        this.currentListId = listId;
        this.currentListName = listName;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(currentListName + " (Lista Comum)"));

        listModel = new DefaultListModel<>();
        listaAnotacoes = new JList<>(listModel);
        listaAnotacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAnotacoes.setFont(new Font("Arial", Font.PLAIN, 16));

        listaAnotacoes.setCellRenderer(new CheckBoxListRenderer());

        listaAnotacoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listaAnotacoes.locationToIndex(e.getPoint());
                if (index != -1) {
                    Anotacao clickedAnotacao = listModel.getElementAt(index);
                    Rectangle bounds = listaAnotacoes.getCellBounds(index, index);
                    if (e.getX() < bounds.x + 20) { // Clicou perto da área do checkbox
                        // Inverte o estado e ATUALIZA NO BANCO DE DADOS
                        boolean newState = !clickedAnotacao.isConcluidaVisual();
                        clickedAnotacao.setConcluidaVisual(newState);
                        anotacaoDAO.atualizar(clickedAnotacao); // Salva a mudança no BD
                        listaAnotacoes.repaint(bounds); // Repinta apenas a célula afetada
                        System.out.println("DEBUG: Anotação " + clickedAnotacao.getTitulo() + " marcada no BD como " + (newState ? "CONCLUÍDA" : "PENDENTE"));
                    } else if (e.getClickCount() == 2) { // Clique duplo para abrir edição
                        editarAnotacaoListaComum(clickedAnotacao);
                    }
                }
            }
        });

        add(new JScrollPane(listaAnotacoes), BorderLayout.CENTER);

        // Botões de ação para a lista comum
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        btnNova = new JButton("Nova Anotação"); // Inicializa como atributo
        btnNova.addActionListener(e -> abrirCadastroListaComum());
        buttonPanel.add(btnNova);

        btnEditar = new JButton("Editar Selecionada"); // Inicializa como atributo
        btnEditar.addActionListener(e -> {
            Anotacao selectedAnotacao = listaAnotacoes.getSelectedValue();
            if (selectedAnotacao != null) {
                editarAnotacaoListaComum(selectedAnotacao);
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma anotação para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(btnEditar);

        btnExcluir = new JButton("Excluir Selecionada"); // Inicializa como atributo
        btnExcluir.addActionListener(e -> excluirAnotacaoListaComum());
        buttonPanel.add(btnExcluir);

        btnOrdenarPrioridade = new JButton("Ordenar por Prioridade"); // Inicializa como atributo
        btnOrdenarPrioridade.addActionListener(e -> ordenarPorPrioridade());
        buttonPanel.add(btnOrdenarPrioridade);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Construtor para a criação inicial dentro de TelaKanban (para CardLayout)
    public TelaListaComum(int userId, TelaKanban mainFrame) {
        this(userId, -1, "Lista Comum", mainFrame); // Chama o construtor principal com valores temporários
    }

    // Método para configurar o contexto da lista quando ela é aberta
    public void setListContext(int listId, String listName) {
        this.currentListId = listId;
        this.currentListName = listName;
        setBorder(BorderFactory.createTitledBorder(currentListName + " (Lista Comum)"));
        carregarAnotacoes(); // Carrega após o contexto ser definido
    }

    // NOVO MÉTODO: Para habilitar/desabilitar botões com base no papel do usuário
    public void updatePermissions(String userRole) {
        boolean canWrite = "OWNER".equals(userRole) || "ADMIN".equals(userRole) || "EDITOR".equals(userRole);
        
        if (btnNova != null) btnNova.setEnabled(canWrite);
        if (btnEditar != null) btnEditar.setEnabled(canWrite);
        if (btnExcluir != null) btnExcluir.setEnabled(canWrite);
        // A ordenação geralmente pode ser feita por qualquer um, mas se quiser restringir:
        // if (btnOrdenarPrioridade != null) btnOrdenarPrioridade.setEnabled(canWrite);

        if (!canWrite) {
            if (btnNova != null) btnNova.setToolTipText("Você não tem permissão para adicionar anotações.");
            if (btnEditar != null) btnEditar.setToolTipText("Você não tem permissão para editar anotações.");
            if (btnExcluir != null) btnExcluir.setToolTipText("Você não tem permissão para excluir anotações.");
            // if (btnOrdenarPrioridade != null) btnOrdenarPrioridade.setToolTipText("Você não tem permissão para ordenar.");
        } else {
             if (btnNova != null) btnNova.setToolTipText(null);
             if (btnEditar != null) btnEditar.setToolTipText(null);
             if (btnExcluir != null) btnExcluir.setToolTipText(null);
             // if (btnOrdenarPrioridade != null) btnOrdenarPrioridade.setToolTipText(null);
        }
    }

    public void carregarAnotacoes() {
        listModel.clear();
        // Não precisamos mais de concluidaStatusVisual, o estado está na Anotacao

        if (currentListId == -1) {
            System.out.println("DEBUG: Contexto da lista comum não definido para carregar anotações.");
            return;
        }
        System.out.println("DEBUG: Carregando anotações da Lista Comum para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listar(this.currentListId);
        System.out.println("DEBUG: Anotações da Lista Comum encontradas: " + lista.size());
        for (Anotacao a : lista) {
            listModel.addElement(a);
        }
    }

    private void ordenarPorPrioridade() {
        listModel.clear();
        // Não precisamos mais de concluidaStatusVisual

        if (currentListId == -1) {
            System.out.println("DEBUG: Contexto da lista comum não definido para ordenar.");
            return;
        }
        System.out.println("DEBUG: Ordenando anotações da Lista Comum por prioridade para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listarEOrdenarPorPrioridade(this.currentListId);
        System.out.println("DEBUG: Anotações da Lista Comum ordenadas: " + lista.size());
        for (Anotacao a : lista) {
            listModel.addElement(a);
        }
    }

    private void abrirCadastroListaComum() {
    // DEBUG: Verificando papel antes de adicionar
    String currentUserRoleFromMainFrame = mainFrame.getCurrentUserRole();
    System.out.println("DEBUG: Tentando abrir cadastro Lista Comum. Papel atual: " + currentUserRoleFromMainFrame); // Adicionar esta linha
    if (!("OWNER".equals(currentUserRoleFromMainFrame) || "ADMIN".equals(currentUserRoleFromMainFrame) || "EDITOR".equals(currentUserRoleFromMainFrame))) {
        JOptionPane.showMessageDialog(this, "Você não tem permissão para adicionar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
        System.out.println("DEBUG: Permissão NEGADA para adicionar anotação Lista Comum."); // Adicionar esta linha
        return;
    }
    System.out.println("DEBUG: Permissão CONCEDIDA para adicionar anotação Lista Comum."); // Adicionar esta linha

    JTextField titulo = new JTextField();
    JTextArea descricao = new JTextArea(5, 20);

    String[] prioridadeOptions = {"POUCO_IMPORTANTE", "IMPORTANTE", "MUITO_IMPORTANTE"};
    JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
    prioridadeBox.setSelectedItem("POUCO_IMPORTANTE");

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Título:"));
    panel.add(titulo);
    panel.add(new JLabel("Descrição:"));
    panel.add(new JScrollPane(descricao));
    panel.add(new JLabel("Prioridade:"));
    panel.add(prioridadeBox);

    int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Lista Comum)",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    System.out.println("DEBUG: Resultado do JOptionPane (Lista Comum): " + result); // Adicionar esta linha

    if (result == JOptionPane.OK_OPTION) {
        Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), "", this.currentListId, (String) prioridadeBox.getSelectedItem(), false);
        System.out.println("DEBUG: Preparando para inserir Anotação Lista Comum no BD. Anotação: " + a.getTitulo() + ", ListID: " + a.getListId()); // Adicionar esta linha
        anotacaoDAO.inserir(a);
        System.out.println("DEBUG: Chamando carregarAnotacoes() após tentativa de inserção."); // Adicionar esta linha
        carregarAnotacoes();
    } else {
        System.out.println("DEBUG: Operação de Nova Anotação (Lista Comum) CANCELADA ou janela FECHADA."); // Adicionar esta linha
    }
}

    private void editarAnotacaoListaComum(Anotacao anotacaoParaEditar) {
        // CORREÇÃO: Renomear userRole para evitar conflito
        String currentUserRoleFromMainFrame = mainFrame.getCurrentUserRole();
        if (!("OWNER".equals(currentUserRoleFromMainFrame) || "ADMIN".equals(currentUserRoleFromMainFrame) || "EDITOR".equals(currentUserRoleFromMainFrame))) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para editar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField titulo = new JTextField(anotacaoParaEditar.getTitulo());
        JTextArea descricao = new JTextArea(anotacaoParaEditar.getDescricao());

        String[] prioridadeOptions = {"POUCO_IMPORTANTE", "IMPORTANTE", "MUITO_IMPORTANTE"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem(anotacaoParaEditar.getPrioridade());

        JCheckBox concluidaCheckbox = new JCheckBox("Concluída", anotacaoParaEditar.isConcluidaVisual()); // Usa o estado persistido


        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Prioridade:"));
        panel.add(prioridadeBox);
        panel.add(concluidaCheckbox);

        String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};
        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Lista Comum)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            anotacaoParaEditar.setTitulo(titulo.getText());
            anotacaoParaEditar.setDescricao(descricao.getText());
            anotacaoParaEditar.setPrioridade((String) prioridadeBox.getSelectedItem());
            anotacaoParaEditar.setConcluidaVisual(concluidaCheckbox.isSelected());
            anotacaoDAO.atualizar(anotacaoParaEditar);
            carregarAnotacoes();
        } else if (result == 1) { // Clicou em "Excluir Anotação"
            // CORREÇÃO: Renomear userRole para evitar conflito
            String userRoleFromMainFrameForDelete = mainFrame.getCurrentUserRole();
            if (!"OWNER".equals(userRoleFromMainFrameForDelete) && !"ADMIN".equals(userRoleFromMainFrameForDelete)) {
                JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirmExcluir = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmExcluir == JOptionPane.YES_OPTION) {
                anotacaoDAO.excluir(anotacaoParaEditar.getId(), this.currentListId);
                carregarAnotacoes();
            }
        }
    }

    private void excluirAnotacaoListaComum() {
        // CORREÇÃO: Renomear userRole para evitar conflito
        String currentUserRoleFromMainFrame = mainFrame.getCurrentUserRole();
        if (!("OWNER".equals(currentUserRoleFromMainFrame) || "ADMIN".equals(currentUserRoleFromMainFrame))) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Anotacao selectedAnotacao = listaAnotacoes.getSelectedValue();
        if (selectedAnotacao == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma anotação para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            anotacaoDAO.excluir(selectedAnotacao.getId(), this.currentListId);
            carregarAnotacoes();
        }
    }

    // CLASSE INTERNA: Renderer personalizado para JList
    private class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<Anotacao> {
        public CheckBoxListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Anotacao> list, Anotacao value, int index, boolean isSelected, boolean cellHasFocus) {
            setSelected(value.isConcluidaVisual()); // Lê o estado persistido do objeto Anotacao

            String baseText = "<b>" + value.getTitulo() + "</b> - " + value.getDescricao();
            baseText += "<br>Prioridade: " + value.getPrioridade();
            if (value.getStatus() != null && !value.getStatus().isEmpty()) { // Status de Kanban
                baseText += " - Status: " + value.getStatus();
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value.isConcluidaVisual()) { // Aplica o risco e estilo se a anotação estiver concluída
                setFont(list.getFont().deriveFont(Font.ITALIC | Font.BOLD));
                setText("<html><strike>" + baseText + "</strike></html>");
            } else {
                 setFont(list.getFont().deriveFont(Font.BOLD));
                 setText("<html>" + baseText + "</html>");
            }
            return this;
        }
    }
}
// END OF FILE: TelaListaComum.java