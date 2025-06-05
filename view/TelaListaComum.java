package view;

import dao.AnotacaoDAO;
import model.Anotacao;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaListaComum extends JPanel { // Extende JPanel, será incorporado em outra tela
    private JList<String> listaAnotacoes;
    private DefaultListModel<String> listModel;
    private AnotacaoDAO dao = new AnotacaoDAO();
    private int loggedInUserId;

    public TelaListaComum(int userId) {
        this.loggedInUserId = userId;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Minha Lista Comum")); // Título do painel

        listModel = new DefaultListModel<>();
        listaAnotacoes = new JList<>(listModel);
        listaAnotacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Seleção única
        listaAnotacoes.setCellRenderer(new DefaultListCellRenderer() { // Renderiza a célula para mostrar título e descrição
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = (String) value;
                // Assumindo que o valor é "Título: X - Descrição: Y"
                label.setText("<html>" + text + "</html>");
                return label;
            }
        });


        add(new JScrollPane(listaAnotacoes), BorderLayout.CENTER); // Adiciona a lista com barra de rolagem

        // Botões de ação para a lista comum
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnNova = new JButton("Nova Anotação");
        btnNova.addActionListener(e -> abrirCadastroListaComum());
        JButton btnEditar = new JButton("Editar Selecionada");
        btnEditar.addActionListener(e -> editarAnotacaoListaComum());
        JButton btnExcluir = new JButton("Excluir Selecionada");
        btnExcluir.addActionListener(e -> excluirAnotacaoListaComum());

        buttonPanel.add(btnNova);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnExcluir);
        add(buttonPanel, BorderLayout.SOUTH);

        carregarAnotacoes(); // Carrega as anotações ao iniciar
    }

    public void carregarAnotacoes() {
        listModel.clear(); // Limpa a lista antes de recarregar
        System.out.println("DEBUG: Carregando anotações da Lista Comum para UserID: " + this.loggedInUserId);
        List<Anotacao> lista = dao.listar(this.loggedInUserId, "COMUM"); // Filtra por tipo "COMUM"
        System.out.println("DEBUG: Anotações da Lista Comum encontradas: " + lista.size());
        for (Anotacao a : lista) {
            // Adiciona ao modelo da lista, armazenando o ID e o tipo para edição/exclusão
            listModel.addElement("ID: " + a.getId() + " - Título: " + a.getTitulo() + " - Descrição: " + a.getDescricao() + " - Status: " + a.getStatus());
        }
    }

    private void abrirCadastroListaComum() {
        JTextField titulo = new JTextField();
        JTextArea descricao = new JTextArea(5, 20);
        String[] statusOptions = {"Pendente", "Concluído"}; // Status diferentes para lista comum?
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação (Lista Comum)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.loggedInUserId, "COMUM");
            dao.inserir(a);
            carregarAnotacoes();
        }
    }

    private void editarAnotacaoListaComum() {
        int selectedIndex = listaAnotacoes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma anotação para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Recupera o ID da anotação selecionada (formato "ID: X - Título: Y...")
        String selectedText = listModel.getElementAt(selectedIndex);
        int id = Integer.parseInt(selectedText.substring(selectedText.indexOf("ID: ") + 4, selectedText.indexOf(" - Título:")));

        // Para editar, precisamos buscar a anotação completa do banco para obter todos os dados
        // Isso requer um método getAnotacaoById(id, userId, tipoLista) no DAO, que não temos.
        // Para simplificar, vou recriar a anotação com base nos dados visíveis, o que não é ideal.
        // Em uma aplicação real, você buscaria a anotação completa do banco de dados.

        // Solução temporária para editar: buscar todas as anotações do tipo COMUM e encontrar a selecionada
        List<Anotacao> todasAnotacoesComum = dao.listar(this.loggedInUserId, "COMUM");
        Anotacao anotacaoParaEditar = null;
        for (Anotacao a : todasAnotacoesComum) {
            if (a.getId() == id) {
                anotacaoParaEditar = a;
                break;
            }
        }

        if (anotacaoParaEditar == null) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar anotação para edição.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField titulo = new JTextField(anotacaoParaEditar.getTitulo());
        JTextArea descricao = new JTextArea(anotacaoParaEditar.getDescricao());
        String[] statusOptions = {"Pendente", "Concluído"}; // Assegure que estes são os status corretos
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);
        statusBox.setSelectedItem(anotacaoParaEditar.getStatus());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        String[] options = {"Salvar", "Cancelar"};
        int result = JOptionPane.showOptionDialog(this, panel, "Editar Anotação (Lista Comum)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Clicou em "Salvar"
            anotacaoParaEditar.setTitulo(titulo.getText());
            anotacaoParaEditar.setDescricao(descricao.getText());
            anotacaoParaEditar.setStatus((String) statusBox.getSelectedItem());
            dao.atualizar(anotacaoParaEditar);
            carregarAnotacoes();
        }
    }

    private void excluirAnotacaoListaComum() {
        int selectedIndex = listaAnotacoes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma anotação para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedText = listModel.getElementAt(selectedIndex);
        int id = Integer.parseInt(selectedText.substring(selectedText.indexOf("ID: ") + 4, selectedText.indexOf(" - Título:")));

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR esta anotação?", "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dao.excluir(id, this.loggedInUserId);
            carregarAnotacoes();
        }
    }
}