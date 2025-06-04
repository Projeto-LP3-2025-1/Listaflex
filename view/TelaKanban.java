package view;

import dao.AnotacaoDAO;
import model.Anotacao;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaKanban extends JFrame {
    private JPanel panelAFazer = new JPanel(new GridLayout(0, 1));
    private JPanel panelFazendo = new JPanel(new GridLayout(0, 1));
    private JPanel panelFeito = new JPanel(new GridLayout(0, 1));
    private AnotacaoDAO dao = new AnotacaoDAO();
    private int loggedInUserId; // Campo para armazenar o ID do usuário logado

    public TelaKanban(int userId) { // Construtor agora recebe o userId
        this.loggedInUserId = userId; // Armazena o ID do usuário

        setTitle("Kanban de Anotações - Usuário ID: " + userId); // Opcional: exibir ID na barra de título
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Opções");
        JMenuItem cadastrar = new JMenuItem("Nova Anotação");
        cadastrar.addActionListener(e -> abrirCadastro());
        menu.add(cadastrar);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        JPanel kanban = new JPanel(new GridLayout(1, 3));
        kanban.add(criarColuna(panelAFazer, "A Fazer"));
        kanban.add(criarColuna(panelFazendo, "Fazendo"));
        kanban.add(criarColuna(panelFeito, "Feito"));
        add(kanban, BorderLayout.CENTER);

        System.out.println("DEBUG: TelaKanban iniciada para o usuário ID: " + this.loggedInUserId);
        carregarAnotacoes();
        setVisible(true);
    }

    private JPanel criarColuna(JPanel panel, String titulo) {
        JPanel coluna = new JPanel(new BorderLayout());
        coluna.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        coluna.add(new JScrollPane(panel), BorderLayout.CENTER);
        return coluna;
    }

    private void abrirCadastro() {
        JTextField titulo = new JTextField();
        JTextArea descricao = new JTextArea(5, 20);
        String[] statusOptions = {"AFazer", "Fazendo", "Feito"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Descrição:"));
        panel.add(new JScrollPane(descricao));
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nova Anotação",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Anotacao a = new Anotacao(titulo.getText(), descricao.getText(), (String) statusBox.getSelectedItem(), this.loggedInUserId);
            System.out.println("DEBUG: Tentando criar anotação na UI. UserID: " + a.getUserId() + ", Título: " + a.getTitulo());
            dao.inserir(a);
            System.out.println("DEBUG: Chamando carregarAnotacoes() após inserção.");
            carregarAnotacoes();
        }
    }

    private void carregarAnotacoes() {
        panelAFazer.removeAll();
        panelFazendo.removeAll();
        panelFeito.removeAll();

        System.out.println("DEBUG: Carregando anotações para o usuário ID: " + this.loggedInUserId);
        List<Anotacao> lista = dao.listar(this.loggedInUserId);
        System.out.println("DEBUG: Anotações encontradas para o usuário ID " + this.loggedInUserId + ": " + lista.size());
        for (Anotacao a : lista) {
            System.out.println("DEBUG:   - Anotação carregada: ID=" + a.getId() + ", Título='" + a.getTitulo() + "', Status='" + a.getStatus() + "', UserID=" + a.getUserId());
            JButton botao = new JButton("<html><b>" + a.getTitulo() + "</b><br>" + a.getDescricao() + "</html>");
            botao.addActionListener(e -> editarAnotacao(a));
            switch (a.getStatus()) {
                case "AFazer" -> panelAFazer.add(botao);
                case "Fazendo" -> panelFazendo.add(botao);
                case "Feito" -> panelFeito.add(botao);
            }
        }
        revalidate();
        repaint();
    }

    private void editarAnotacao(Anotacao a) {
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

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Anotação",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            a.setTitulo(titulo.getText());
            a.setDescricao(descricao.getText());
            a.setStatus((String) statusBox.getSelectedItem());
            dao.atualizar(a); // user_id já está no objeto 'a'
            carregarAnotacoes();
        }

        int excluir = JOptionPane.showConfirmDialog(this, "Deseja excluir esta anotação?", "Excluir", JOptionPane.YES_NO_OPTION);
        if (excluir == JOptionPane.YES_OPTION) {
            dao.excluir(a.getId(), this.loggedInUserId);
            carregarAnotacoes();
        }
    }
}