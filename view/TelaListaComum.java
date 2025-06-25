package view;

import dao.AnotacaoDAO;
import model.Anotacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TelaListaComum extends JPanel {
    private JList<Anotacao> listaAnotacoes;
    private DefaultListModel<Anotacao> listModel;
    private AnotacaoDAO anotacaoDAO = new AnotacaoDAO();
    private int loggedInUserId;
    private int currentListId;
    private String currentListName;

    private TelaKanban mainFrame;
    private ArrayList<Boolean> concluidaStatusVisual; // Armazena o estado VISUAL de conclusão para cada Anotacao na lista

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

        // Define o CellRenderer personalizado
        listaAnotacoes.setCellRenderer(new CheckBoxListRenderer());

        // Listener de clique para o checkbox visual e clique duplo para editar
        listaAnotacoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listaAnotacoes.locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle bounds = listaAnotacoes.getCellBounds(index, index);
                    // Supondo que o checkbox tem aproximadamente 20 pixels de largura à esquerda
                    if (e.getX() < bounds.x + 20) { // Clicou perto da área do checkbox
                        // Inverte o estado visual (não no banco de dados)
                        boolean currentState = concluidaStatusVisual.get(index);
                        concluidaStatusVisual.set(index, !currentState);
                        listaAnotacoes.repaint(bounds); // Repinta apenas a célula afetada
                        System.out.println("DEBUG: Anotação " + listModel.getElementAt(index).getTitulo() + " marcada VISUALMENTE como " + (!currentState ? "CONCLUÍDA" : "PENDENTE"));
                    } else if (e.getClickCount() == 2) { // Clique duplo para abrir edição
                        editarAnotacaoListaComum(listModel.getElementAt(index));
                    }
                }
            }
        });

        add(new JScrollPane(listaAnotacoes), BorderLayout.CENTER);

        // Botões de ação para a lista comum
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnNova = new JButton("Nova Anotação");
        btnNova.addActionListener(e -> abrirCadastroListaComum());
        buttonPanel.add(btnNova);

        JButton btnEditar = new JButton("Editar Selecionada");
        btnEditar.addActionListener(e -> {
            Anotacao selectedAnotacao = listaAnotacoes.getSelectedValue();
            if (selectedAnotacao != null) {
                editarAnotacaoListaComum(selectedAnotacao);
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma anotação para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(btnEditar);

        JButton btnExcluir = new JButton("Excluir Selecionada");
        btnExcluir.addActionListener(e -> excluirAnotacaoListaComum());
        buttonPanel.add(btnExcluir);

        JButton btnOrdenarPrioridade = new JButton("Ordenar por Prioridade");
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

    public void carregarAnotacoes() {
        listModel.clear();
        concluidaStatusVisual = new ArrayList<>(); // Sempre reinicializa a lista de status visuais

        if (currentListId == -1) {
            System.out.println("DEBUG: Contexto da lista comum não definido para carregar anotações.");
            return;
        }
        System.out.println("DEBUG: Carregando anotações da Lista Comum para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listar(this.currentListId);
        System.out.println("DEBUG: Anotações da Lista Comum encontradas: " + lista.size());
        for (Anotacao a : lista) {
            // AQUI É A CORREÇÃO: Removendo o filtro de status, para garantir que todas as anotações da lista comum sejam adicionadas.
            // O status que você estava usando ("Pendente", "Concluído") não existe mais para anotações comuns no BD.
            listModel.addElement(a);
            concluidaStatusVisual.add(false); // Adiciona o status visual para CADA item adicionado
        }
    }

    private void ordenarPorPrioridade() {
        listModel.clear();
        concluidaStatusVisual = new ArrayList<>(); // Limpa e reinicializa

        if (currentListId == -1) {
            System.out.println("DEBUG: Contexto da lista comum não definido para ordenar.");
            return;
        }
        System.out.println("DEBUG: Ordenando anotações da Lista Comum por prioridade para ListID: " + this.currentListId);
        List<Anotacao> lista = anotacaoDAO.listarEOrdenarPorPrioridade(this.currentListId);
        System.out.println("DEBUG: Anotações da Lista Comum ordenadas: " + lista.size());
        for (Anotacao a : lista) {
            // AQUI É A CORREÇÃO: Removendo o filtro de status
            listModel.addElement(a);
            concluidaStatusVisual.add(false); // Adiciona o status visual para CADA item adicionado
        }
    }

    private void abrirCadastroListaComum() {
        JTextField titulo = new JTextField();
        JTextArea descricao = new JTextArea(5, 20);
        // Removido o statusBox, pois não será mais "Pendente" / "Concluído"
        // String[] statusOptions = {"Pendente", "Concluído"};
        // JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        String[] prioridadeOptions = {"POUCO_IMPORTANTE", "IMPORTANTE", "MUITO_IMPORTANTE"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem("POUCO_IMPORTANTE");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        // Removido o statusBox do painel
        // panel.add(new JLabel("Status:"));
        // panel.add(statusBox);
        panel.add(new JLabel("Prioridade:"));
        panel.add(prioridadeBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Lista Comum)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Passa um status vazio ou genérico para a anotação comum, já que o checkbox lida com a conclusão
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), "", this.currentListId, (String) prioridadeBox.getSelectedItem());
            anotacaoDAO.inserir(a);
            carregarAnotacoes();
        }
    }

    private void editarAnotacaoListaComum(Anotacao anotacaoParaEditar) {
        JTextField titulo = new JTextField(anotacaoParaEditar.getTitulo());
        JTextArea descricao = new JTextArea(anotacaoParaEditar.getDescricao());
        // Removido o statusBox
        // String[] statusOptions = {"Pendente", "Concluído"};
        // JComboBox<String> statusBox = new JComboBox<>(statusOptions);
        // statusBox.setSelectedItem(anotacaoParaEditar.getStatus());

        String[] prioridadeOptions = {"POUCO_IMPORTANTE", "IMPORTANTE", "MUITO_IMPORTANTE"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setSelectedItem(anotacaoParaEditar.getPrioridade());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        // Removido o statusBox do painel
        // panel.add(new JLabel("Status:"));
        // panel.add(statusBox);
        panel.add(new JLabel("Prioridade:"));
        panel.add(prioridadeBox);

        String[] options = {"Salvar", "Excluir Anotação", "Cancelar"};
        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Lista Comum)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            anotacaoParaEditar.setTitulo(titulo.getText());
            anotacaoParaEditar.setDescricao(descricao.getText());
            // Não atualiza mais o status
            // anotacaoParaEditar.setStatus((String) statusBox.getSelectedItem());
            anotacaoParaEditar.setPrioridade((String) prioridadeBox.getSelectedItem());
            anotacaoDAO.atualizar(anotacaoParaEditar);
            carregarAnotacoes();
        } else if (result == 1) { // Clicou em "Excluir Anotação"
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
            // A correção principal já está aqui para garantir que 'concluidaStatusVisual' está sincronizado.
            // A verificação de segurança adicionada previne crashes, mas a causa raiz é a sincronização.
            boolean isConcluidaVisual = false;
            if (index >= 0 && index < concluidaStatusVisual.size()) {
                 isConcluidaVisual = concluidaStatusVisual.get(index);
            } else {
                 // Isso indica um problema de sincronização de dados que a correção do loop deveria resolver.
                 // Apenas para evitar crash, mas investigue por que isso acontece se você vir este erro no console.
                 System.err.println("ERRO INTERNO: Index " + index + " fora dos limites para concluidaStatusVisual (tamanho: " + concluidaStatusVisual.size() + ") ao renderizar. Recarregando lista...");
                 // Uma tentativa de recuperação, embora a causa raiz deva ser evitada pela sincronização.
                 SwingUtilities.invokeLater(() -> carregarAnotacoes()); // Tentar recarregar em outro thread da EDT
                 setText("<html>Erro de Renderização (Recarregando...)</html>");
                 setSelected(false);
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
                 return this;
            }

            String baseText = "<b>" + value.getTitulo() + "</b> - " + value.getDescricao();
            baseText += "<br>Prioridade: " + value.getPrioridade();
            // A linha abaixo foi ajustada para só mostrar o Status se ele não for vazio.
            if (value.getStatus() != null && !value.getStatus().isEmpty()) {
                baseText += " - Status: " + value.getStatus();
            }

            setSelected(isConcluidaVisual);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (isConcluidaVisual) {
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