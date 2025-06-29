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
    // --- CONSTANTES DE CORES DA PALETA PROFISSIONAL ---
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50"); // Azul Marinho Escuro
    private static final Color ACCENT_COLOR = Color.decode("#3498DB"); // Azul Céu
    private static final Color BACKGROUND_COLOR_LIGHT = Color.decode("#ECF0F1"); // Fundo claro
    private static final Color TEXT_COLOR_DARK = Color.decode("#34495E"); // Texto principal
    private static final Color BORDER_COLOR_NEUTRAL = Color.decode("#BDC3C7"); // Bordas

    // Cores de Prioridade
    private static final Color LOW_PRIORITY_COLOR = Color.decode("#B0BEC5"); // Cinza azulado (Pouco importante)
    private static final Color MEDIUM_PRIORITY_COLOR = Color.decode("#F39C12"); // Laranja terroso (Importante)
    private static final Color HIGH_PRIORITY_COLOR = Color.decode("#C0392B"); // Vermelho tijolo (Muito importante)
    // --- FIM CONSTANTES DE CORES ---

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
        setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel
        setBorder(BorderFactory.createTitledBorder(currentListName + " (Lista Comum)"));
        ((javax.swing.border.TitledBorder) getBorder()).setTitleColor(TEXT_COLOR_DARK); // Cor do título da borda
        ((javax.swing.border.TitledBorder) getBorder()).setTitleFont(new Font("Arial", Font.BOLD, 16));


        listModel = new DefaultListModel<>();
        listaAnotacoes = new JList<>(listModel);
        listaAnotacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAnotacoes.setFont(new Font("Arial", Font.PLAIN, 16));
        listaAnotacoes.setBackground(Color.WHITE); // Fundo da lista

        listaAnotacoes.setCellRenderer(new CheckBoxListRenderer());

        listaAnotacoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listaAnotacoes.locationToIndex(e.getPoint());
                if (index != -1) {
                    Anotacao clickedAnotacao = listModel.getElementAt(index);
                    Rectangle bounds = listaAnotacoes.getCellBounds(index, index);
                    if (e.getX() < bounds.x + 20) { // Clicou perto da área do checkbox
                        // DEBUG: Verificando permissão antes de alterar checkbox
                        String userRole = mainFrame.getCurrentUserRole();
                        System.out.println("DEBUG (Checkbox Click): Papel: " + userRole);
                        boolean canEdit = "Criador(a)".equals(userRole) || "ADMIN".equals(userRole) || "EDITOR".equals(userRole);
                        if (!canEdit) {
                            JOptionPane.showMessageDialog(TelaListaComum.this, "Você não tem permissão para marcar/desmarcar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                            System.out.println("DEBUG (Checkbox Click): Permissão NEGADA para marcar/desmarcar.");
                            return; // Não altera o estado se não tiver permissão
                        }
                        System.out.println("DEBUG (Checkbox Click): Permissão CONCEDIDA para marcar/desmarcar.");

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

        // Painel de botões de ação
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel de botões

        btnNova = new JButton("Nova Anotação"); // Inicializa como atributo
        btnNova.setBackground(ACCENT_COLOR); // Cor do botão
        btnNova.setForeground(Color.WHITE); // Texto branco
        btnNova.setFont(new Font("Arial", Font.BOLD, 14));
        btnNova.setOpaque(true);
        btnNova.setBorderPainted(false);
        btnNova.setFocusPainted(false);
        btnNova.addActionListener(e -> abrirCadastroListaComum());
        buttonPanel.add(btnNova);

        btnEditar = new JButton("Editar Selecionada"); // Inicializa como atributo
        btnEditar.setBackground(ACCENT_COLOR); // Cor do botão
        btnEditar.setForeground(Color.WHITE); // Texto branco
        btnEditar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEditar.setOpaque(true);
        btnEditar.setBorderPainted(false);
        btnEditar.setFocusPainted(false);
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
        btnExcluir.setBackground(ACCENT_COLOR); // Cor do botão
        btnExcluir.setForeground(Color.WHITE); // Texto branco
        btnExcluir.setFont(new Font("Arial", Font.BOLD, 14));
        btnExcluir.setOpaque(true);
        btnExcluir.setBorderPainted(false);
        btnExcluir.setFocusPainted(false);
        btnExcluir.addActionListener(e -> excluirAnotacaoListaComum());
        buttonPanel.add(btnExcluir);

        btnOrdenarPrioridade = new JButton("Ordenar por Prioridade"); // Inicializa como atributo
        btnOrdenarPrioridade.setBackground(ACCENT_COLOR); // Cor do botão
        btnOrdenarPrioridade.setForeground(Color.WHITE); // Texto branco
        btnOrdenarPrioridade.setFont(new Font("Arial", Font.BOLD, 14));
        btnOrdenarPrioridade.setOpaque(true);
        btnOrdenarPrioridade.setBorderPainted(false);
        btnOrdenarPrioridade.setFocusPainted(false);
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
        ((javax.swing.border.TitledBorder) getBorder()).setTitleColor(TEXT_COLOR_DARK);
        ((javax.swing.border.TitledBorder) getBorder()).setTitleFont(new Font("Arial", Font.BOLD, 16));
        
        carregarAnotacoes();
    }

    // NOVO MÉTODO: Para habilitar/desabilitar botões com base no papel do usuário
    public void updatePermissions(String userRole) {
        System.out.println("DEBUG (TelaListaComum updatePermissions): Recebendo papel: " + userRole);
        boolean canWrite = "Criador(a)".equals(userRole) || "ADMIN".equals(userRole) || "EDITOR".equals(userRole); // Voltou para Criador(a)/ADMIN/EDITOR
        
        if (btnNova != null) {
            btnNova.setEnabled(canWrite);
            btnNova.setToolTipText(canWrite ? null : "Você não tem permissão para adicionar anotações.");
            System.out.println("DEBUG (TelaListaComum updatePermissions): Botão 'Nova Anotação' HABILITADO: " + canWrite);
        }
        if (btnEditar != null) {
            btnEditar.setEnabled(canWrite);
            btnEditar.setToolTipText(canWrite ? null : "Você não tem permissão para editar anotações.");
            System.out.println("DEBUG (TelaListaComum updatePermissions): Botão 'Editar' HABILITADO: " + canWrite);
        }
        if (btnExcluir != null) {
            btnExcluir.setEnabled(canWrite);
            btnExcluir.setToolTipText(canWrite ? null : "Você não tem permissão para excluir anotações.");
            System.out.println("DEBUG (TelaListaComum updatePermissions): Botão 'Excluir' HABILITADO: " + canWrite);
        }
        
        System.out.println("DEBUG (TelaListaComum updatePermissions): canWrite = " + canWrite);
    }

    public void carregarAnotacoes() {
        listModel.clear();

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
        listaAnotacoes.revalidate();
        listaAnotacoes.repaint();
    }

    private void ordenarPorPrioridade() {
        listModel.clear();

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
        listaAnotacoes.revalidate();
        listaAnotacoes.repaint();
    }

    private void abrirCadastroListaComum() {
        System.out.println("DEBUG (abrirCadastroListaComum): Verificando permissão antes de abrir diálogo. Papel: " + mainFrame.getCurrentUserRole());
        boolean canAdd = "Criador(a)".equals(mainFrame.getCurrentUserRole()) || "ADMIN".equals(mainFrame.getCurrentUserRole()) || "EDITOR".equals(mainFrame.getCurrentUserRole());
        if (!canAdd) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para adicionar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            System.out.println("DEBUG (abrirCadastroListaComum): Permissão NEGADA para adicionar.");
            return;
        }
        System.out.println("DEBUG (abrirCadastroListaComum): Permissão CONCEDIDA para adicionar.");

        JTextField titulo = new JTextField();
        JTextArea descricao = new JTextArea(5, 20);

        String[] prioridadeOptions = {"Pouco importante", "Importante", "Muito importante"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem("Pouco importante");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        // Cores para o painel do diálogo
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

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Lista Comum)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        System.out.println("DEBUG (abrirCadastroListaComum): Resultado do JOptionPane: " + result);

        // Resetar UIManager
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);


        if (result == JOptionPane.OK_OPTION) {
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), "", this.currentListId, (String) prioridadeBox.getSelectedItem(), false);
            System.out.println("DEBUG (abrirCadastroListaComum): Anotação preparada para inserção: " + a.getTitulo() + ", ListID: " + a.getListId());
            anotacaoDAO.inserir(a);
            System.out.println("DEBUG (abrirCadastroListaComum): Chamando carregarAnotacoes() após inserção.");
            carregarAnotacoes();
        } else {
            System.out.println("DEBUG (abrirCadastroListaComum): Operação de criação cancelada/fechada.");
        }
    }

    private void editarAnotacaoListaComum(Anotacao anotacaoParaEditar) {
        System.out.println("DEBUG (editarAnotacaoListaComum): Verificando permissão antes de editar. Papel: " + mainFrame.getCurrentUserRole());
        boolean canEdit = "Criador(a)".equals(mainFrame.getCurrentUserRole()) || "ADMIN".equals(mainFrame.getCurrentUserRole()) || "EDITOR".equals(mainFrame.getCurrentUserRole());
        if (!canEdit) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para editar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            System.out.println("DEBUG (editarAnotacaoListaComum): Permissão NEGADA para editar.");
            return;
        }
        System.out.println("DEBUG (editarAnotacaoListaComum): Permissão CONCEDIDA para editar.");


        JTextField titulo = new JTextField(anotacaoParaEditar.getTitulo());
        JTextArea descricao = new JTextArea(anotacaoParaEditar.getDescricao());

        String[] prioridadeOptions = {"Pouco importante", "Importante", "Muito importante"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem(anotacaoParaEditar.getPrioridade());

        JCheckBox concluidaCheckbox = new JCheckBox("Concluída", anotacaoParaEditar.isConcluidaVisual());


        JPanel panel = new JPanel(new GridLayout(0, 1));
        // Cores para o painel do diálogo
        panel.setBackground(BACKGROUND_COLOR_LIGHT);
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel)comp).setForeground(TEXT_COLOR_DARK);
            }
            if (comp instanceof JTextField || comp instanceof JTextArea || comp instanceof JComboBox || comp instanceof JCheckBox) { // Adicionado JCheckBox
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
        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Lista Comum)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        System.out.println("DEBUG (editarAnotacaoListaComum): Resultado do JOptionPane: " + result);

        // Resetar UIManager
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);


        if (result == 0) { // Clicou em "Salvar"
            anotacaoParaEditar.setTitulo(titulo.getText());
            anotacaoParaEditar.setDescricao(descricao.getText());
            anotacaoParaEditar.setPrioridade((String) prioridadeBox.getSelectedItem());
            anotacaoParaEditar.setConcluidaVisual(concluidaCheckbox.isSelected());
            System.out.println("DEBUG (editarAnotacaoListaComum): Atualizando anotação no BD: " + anotacaoParaEditar.getTitulo() + ", ConcluidaVisual: " + anotacaoParaEditar.isConcluidaVisual());
            anotacaoDAO.atualizar(anotacaoParaEditar);
            System.out.println("DEBUG (editarAnotacaoListaComum): Chamando carregarAnotacoes() após atualização.");
            carregarAnotacoes();
        } else if (result == 1) { // Clicou em "Excluir Anotação"
            String userRoleFromMainFrameForDelete = mainFrame.getCurrentUserRole();
            System.out.println("DEBUG (editarAnotacaoListaComum - Excluir): Papel: " + userRoleFromMainFrameForDelete);
            boolean canDelete = "Criador(a)".equals(userRoleFromMainFrameForDelete) || "ADMIN".equals(userRoleFromMainFrameForDelete);
            if (!canDelete) {
                JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                System.out.println("DEBUG (editarAnotacaoListaComum - Excluir): Permissão NEGADA para excluir.");
                return;
            }
            System.out.println("DEBUG (editarAnotacaoListaComum - Excluir): Permissão CONCEDIDA para excluir.");
            
            int confirmExcluir = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            System.out.println("DEBUG (editarAnotacaoListaComum - Excluir): Confirmação de exclusão: " + (confirmExcluir == JOptionPane.YES_OPTION));

            if (confirmExcluir == JOptionPane.YES_OPTION) {
                anotacaoDAO.excluir(anotacaoParaEditar.getId(), this.currentListId);
                System.out.println("DEBUG (editarAnotacaoListaComum - Excluir): Anotação excluída. Chamando carregarAnotacoes().");
                carregarAnotacoes();
            } else {
                System.out.println("DEBUG (editarAnotacaoListaComum - Excluir): Exclusão cancelada.");
            }
        } else {
            System.out.println("DEBUG (editarAnotacaoListaComum): Operação de edição cancelada/fechada.");
        }
    }

    private void excluirAnotacaoListaComum() {
        System.out.println("DEBUG (excluirAnotacaoListaComum): Verificando permissão antes de excluir. Papel: " + mainFrame.getCurrentUserRole());
        boolean canDelete = "Criador(a)".equals(mainFrame.getCurrentUserRole()) || "ADMIN".equals(mainFrame.getCurrentUserRole());
        if (!canDelete) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            System.out.println("DEBUG (excluirAnotacaoListaComum): Permissão NEGADA para excluir.");
            return;
        }
        System.out.println("DEBUG (excluirAnotacaoListaComum): Permissão CONCEDIDA para excluir.");

        Anotacao selectedAnotacao = listaAnotacoes.getSelectedValue();
        if (selectedAnotacao == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma anotação para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            System.out.println("DEBUG (excluirAnotacaoListaComum): Nenhuma anotação selecionada.");
            return;
        }
        System.out.println("DEBUG (excluirAnotacaoListaComum): Anotação selecionada: " + selectedAnotacao.getTitulo());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        System.out.println("DEBUG (excluirAnotacaoListaComum): Confirmação de exclusão: " + (confirm == JOptionPane.YES_OPTION));

        if (confirm == JOptionPane.YES_OPTION) {
            anotacaoDAO.excluir(selectedAnotacao.getId(), this.currentListId);
            System.out.println("DEBUG (excluirAnotacaoListaComum): Anotação excluída. Chamando carregarAnotacoes().");
            carregarAnotacoes();
        } else {
            System.out.println("DEBUG (excluirAnotacaoListaComum): Exclusão cancelada.");
        }
    }

    private class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<Anotacao> {
        public CheckBoxListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Anotacao> list, Anotacao value, int index, boolean isSelected, boolean cellHasFocus) {
            setSelected(value.isConcluidaVisual());

            String baseText = "<b>" + value.getTitulo() + "</b> - " + value.getDescricao();
            baseText += "<br>Prioridade: " + value.getPrioridade(); // Prioridade já está traduzida no modelo/BD
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

            if (value.isConcluidaVisual()) {
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