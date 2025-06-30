// START OF FILE: TelaListaComum.java
package project.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import project.dao.AnotacaoDAO;
import project.model.Anotacao;

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
        panel.setBackground(BACKGROUND_COLOR_LIGHT);

        // Adiciona os componentes ao painel
        JLabel labelTitulo = new JLabel("Título:");
        JLabel labelDescricao = new JLabel("Descrição:");
        JLabel labelPrioridade = new JLabel("Prioridade:");

        panel.add(labelTitulo);
        panel.add(titulo);

        panel.add(labelDescricao);
        panel.add(new JScrollPane(descricao));

        panel.add(labelPrioridade);
        panel.add(prioridadeBox);

        // Estiliza os componentes adicionados
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(TEXT_COLOR_DARK);
            }
            if (comp instanceof JTextField || comp instanceof JComboBox) {
                comp.setBackground(Color.WHITE);
                comp.setForeground(TEXT_COLOR_DARK);
                ((JComponent) comp).setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));
            }
            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JTextArea) {
                    view.setBackground(Color.WHITE);
                    view.setForeground(TEXT_COLOR_DARK);
                    ((JComponent) view).setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));
                }
            }
        }

        // (Opcional) debug: quantos componentes o painel tem
        System.out.println("DEBUG (abrirCadastroListaComum): Painel possui " + panel.getComponentCount() + " componentes.");

        UIManager.put("OptionPane.background", BACKGROUND_COLOR_LIGHT);
        UIManager.put("Panel.background", BACKGROUND_COLOR_LIGHT);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Lista Comum)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Resetar UIManager para não afetar outros modais
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);

        if (result == JOptionPane.OK_OPTION) {
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), "", this.currentListId, (String) prioridadeBox.getSelectedItem(), false);
            System.out.println("DEBUG (abrirCadastroListaComum): Anotação criada: " + a.getTitulo() + " | Prioridade: " + a.getPrioridade());
            anotacaoDAO.inserir(a);
            carregarAnotacoes();
        } else {
            System.out.println("DEBUG (abrirCadastroListaComum): Criação cancelada.");
        }
    }


private void editarAnotacaoListaComum(Anotacao anotacaoParaEditar) {
    System.out.println("DEBUG (editarAnotacaoListaComum): Verificando permissão antes de editar. Papel: " + mainFrame.getCurrentUserRole());
    boolean canEdit = "Criador(a)".equals(mainFrame.getCurrentUserRole()) || "ADMIN".equals(mainFrame.getCurrentUserRole()) || "EDITOR".equals(mainFrame.getCurrentUserRole());
    if (!canEdit) {
        JOptionPane.showMessageDialog(this, "Você não tem permissão para editar anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // COMPONENTES
    JTextField titulo = new JTextField(anotacaoParaEditar.getTitulo());
    JTextArea descricao = new JTextArea(anotacaoParaEditar.getDescricao(), 5, 20);
    String[] prioridadeOptions = {"Pouco importante", "Importante", "Muito importante"};
    JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
    prioridadeBox.setSelectedItem(anotacaoParaEditar.getPrioridade());
    JCheckBox concluidaCheckbox = new JCheckBox("Concluída", anotacaoParaEditar.isConcluidaVisual());

    // PAINEL
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.setBackground(BACKGROUND_COLOR_LIGHT);

    // LABELS
    JLabel labelTitulo = new JLabel("Título:");
    JLabel labelDescricao = new JLabel("Descrição:");
    JLabel labelPrioridade = new JLabel("Prioridade:");

    // ADICIONA AO PAINEL
    panel.add(labelTitulo);
    panel.add(titulo);
    panel.add(labelDescricao);
    panel.add(new JScrollPane(descricao));
    panel.add(labelPrioridade);
    panel.add(prioridadeBox);
    panel.add(concluidaCheckbox);

    // APLICA ESTILOS
    labelTitulo.setForeground(TEXT_COLOR_DARK);
    labelDescricao.setForeground(TEXT_COLOR_DARK);
    labelPrioridade.setForeground(TEXT_COLOR_DARK);

    titulo.setBackground(Color.WHITE);
    titulo.setForeground(TEXT_COLOR_DARK);
    titulo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));

    descricao.setBackground(Color.WHITE);
    descricao.setForeground(TEXT_COLOR_DARK);
    descricao.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));

    prioridadeBox.setBackground(Color.WHITE);
    prioridadeBox.setForeground(TEXT_COLOR_DARK);
    prioridadeBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_NEUTRAL));

    concluidaCheckbox.setBackground(BACKGROUND_COLOR_LIGHT);
    concluidaCheckbox.setForeground(TEXT_COLOR_DARK);

    // CONFIGURAÇÕES DO UIManager ANTES DO MODAL
    UIManager.put("OptionPane.background", BACKGROUND_COLOR_LIGHT);
    UIManager.put("Panel.background", BACKGROUND_COLOR_LIGHT);

    String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};
    int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Lista Comum)",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

    // LIMPA ESTILOS DO UIManager APÓS O MODAL
    UIManager.put("OptionPane.background", null);
    UIManager.put("Panel.background", null);
    UIManager.put("Button.background", null);
    UIManager.put("Button.foreground", null);
    UIManager.put("Button.font", null);

    // LÓGICA DE AÇÃO
    if (result == 0) { // Salvar
        anotacaoParaEditar.setTitulo(titulo.getText());
        anotacaoParaEditar.setDescricao(descricao.getText());
        anotacaoParaEditar.setPrioridade((String) prioridadeBox.getSelectedItem());
        anotacaoParaEditar.setConcluidaVisual(concluidaCheckbox.isSelected());

        anotacaoDAO.atualizar(anotacaoParaEditar);
        carregarAnotacoes();

    } else if (result == 1) { // Excluir
        boolean canDelete = "Criador(a)".equals(mainFrame.getCurrentUserRole()) || "ADMIN".equals(mainFrame.getCurrentUserRole());
        if (!canDelete) {
            JOptionPane.showMessageDialog(this, "Você não tem permissão para excluir anotações nesta lista.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            anotacaoDAO.excluir(anotacaoParaEditar.getId(), this.currentListId);
            carregarAnotacoes();
        }
    } else {
        System.out.println("DEBUG (editarAnotacaoListaComum): Edição cancelada.");
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