// START OF FILE: TelaLogin.java
package view;

import dao.UserDAO;
import model.User;
import view.TelaEscolhaLista;

import javax.swing.*;
import java.awt.*;
import java.net.URL; // Necessário para carregar a imagem

public class TelaLogin extends JFrame {
    // --- CONSTANTES DE CORES DA PALETA PROFISSIONAL ---
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50"); // Azul Marinho Escuro
    private static final Color ACCENT_COLOR = Color.decode("#3498DB"); // Azul Céu
    private static final Color BACKGROUND_COLOR_LIGHT = Color.decode("#ECF0F1"); // Fundo claro
    private static final Color TEXT_COLOR_DARK = Color.decode("#34495E"); // Texto principal
    // --- FIM CONSTANTES DE CORES ---

    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDAO userDAO;

    public TelaLogin() {
        userDAO = new UserDAO();

        setTitle("Login - Listaflex");
        setSize(400, 400); // Aumentei um pouco o tamanho para caber a imagem/slogan
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout()); // Usar BorderLayout para dividir top e centro
        mainPanel.setBackground(BACKGROUND_COLOR_LIGHT);
        
        // --- PAINEL SUPERIOR PARA IMAGEM E SLOGAN ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // Empilha verticalmente
        headerPanel.setBackground(BACKGROUND_COLOR_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0)); // Margem superior/inferior

        // Slogan
        JLabel sloganLabel = new JLabel("Uma nova forma de organizar a sua vida.");
        sloganLabel.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 16)); // Itálico e negrito
        sloganLabel.setForeground(TEXT_COLOR_DARK);
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centraliza horizontalmente
        headerPanel.add(sloganLabel);

        headerPanel.add(Box.createVerticalStrut(10)); // Espaço entre slogan e imagem

        // Imagem
        JLabel imageLabel = new JLabel();
        try {
            // Carrega a imagem do caminho relativo ao classpath
            // A imagem deve estar em uma pasta 'images' (ex: Listaflex/images/pessoa_com_lista.png)
            URL imageUrl = getClass().getClassLoader().getResource("images/pessoa_com_lista.png"); // Altere o nome do arquivo se for diferente
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                // Redimensionar a imagem se for muito grande
                Image image = originalIcon.getImage(); // transform it
                Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // scale it smoothly
                ImageIcon resizedIcon = new ImageIcon(newimg);  // convert it back to ImageIcon
                imageLabel.setIcon(resizedIcon);
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centraliza horizontalmente
                headerPanel.add(imageLabel);
            } else {
                System.err.println("Imagem não encontrada: images/pessoa_com_lista.png. Verifique o caminho e se o arquivo existe.");
                headerPanel.add(new JLabel("Imagem não encontrada"), Component.CENTER_ALIGNMENT);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + e.getMessage());
            headerPanel.add(new JLabel("Erro ao carregar imagem"), Component.CENTER_ALIGNMENT);
        }
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        // --- FIM PAINEL SUPERIOR ---


        // --- PAINEL PARA CAMPOS DE LOGIN (CENTER) ---
        JPanel loginFieldsPanel = new JPanel(new GridBagLayout());
        loginFieldsPanel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        // Labels
        JLabel userLabel = new JLabel("Usuário:");
        userLabel.setForeground(TEXT_COLOR_DARK);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; loginFieldsPanel.add(userLabel, gbc);

        // Username Field
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; usernameField = new JTextField(20);
        usernameField.setBackground(Color.WHITE); usernameField.setForeground(TEXT_COLOR_DARK);
        loginFieldsPanel.add(usernameField, gbc);

        // Password Label
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Senha:");
        passLabel.setForeground(TEXT_COLOR_DARK);
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginFieldsPanel.add(passLabel, gbc);

        // Password Field
        gbc.gridx = 1; gbc.gridy = 1;
        passwordField = new JPasswordField(20);
        passwordField.setBackground(Color.WHITE); passwordField.setForeground(TEXT_COLOR_DARK);
        loginFieldsPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> performLogin());
        loginFieldsPanel.add(loginButton, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        JButton registerButton = new JButton("Cadastrar");
        registerButton.setBackground(ACCENT_COLOR);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> openRegisterScreen());
        loginFieldsPanel.add(registerButton, gbc);

        mainPanel.add(loginFieldsPanel, BorderLayout.CENTER);
        // --- FIM PAINEL PARA CAMPOS DE LOGIN ---

        add(mainPanel); // Adiciona o mainPanel ao JFrame
        setVisible(true);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = userDAO.loginUser(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login bem-sucedido!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new TelaEscolhaLista(user.getId());
        } else {
            JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos.", "Erro de Login", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterScreen() {
        dispose();
        new TelaCadastro();
    }
}
// END OF FILE: TelaLogin.java