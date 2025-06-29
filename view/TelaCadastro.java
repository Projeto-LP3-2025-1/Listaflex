// START OF FILE: TelaCadastro.java
package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class TelaCadastro extends JFrame {
    // --- CONSTANTES DE CORES DA PALETA PROFISSIONAL ---
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50"); // Azul Marinho Escuro
    private static final Color ACCENT_COLOR = Color.decode("#3498DB"); // Azul Céu
    private static final Color BACKGROUND_COLOR_LIGHT = Color.decode("#ECF0F1"); // Fundo claro
    private static final Color TEXT_COLOR_DARK = Color.decode("#34495E"); // Texto principal
    // --- FIM CONSTANTES DE CORES ---

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private UserDAO userDAO;

    public TelaCadastro() {
        userDAO = new UserDAO();

        setTitle("Cadastro - Listaflex");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR_LIGHT); // Fundo do painel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        // Labels e Fields - Usuário
        JLabel userLabel = new JLabel("Usuário:");
        userLabel.setForeground(TEXT_COLOR_DARK);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; panel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; usernameField = new JTextField(20);
        usernameField.setBackground(Color.WHITE); usernameField.setForeground(TEXT_COLOR_DARK);
        panel.add(usernameField, gbc);

        // Labels e Fields - Senha
        JLabel passLabel = new JLabel("Senha:");
        passLabel.setForeground(TEXT_COLOR_DARK);
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; panel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; passwordField = new JPasswordField(20);
        passwordField.setBackground(Color.WHITE); passwordField.setForeground(TEXT_COLOR_DARK);
        panel.add(passwordField, gbc);

        // Labels e Fields - Confirmar Senha
        JLabel confirmPassLabel = new JLabel("Confirmar Senha:");
        confirmPassLabel.setForeground(TEXT_COLOR_DARK);
        confirmPassLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; panel.add(confirmPassLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setBackground(Color.WHITE); confirmPasswordField.setForeground(TEXT_COLOR_DARK);
        panel.add(confirmPasswordField, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton registerButton = new JButton("Cadastrar");
        registerButton.setBackground(PRIMARY_COLOR); // Cor de fundo do botão
        registerButton.setForeground(Color.WHITE); // Cor do texto
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> performRegistration());
        panel.add(registerButton, gbc);

        // Back to Login Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        JButton backButton = new JButton("Voltar ao Login");
        backButton.setBackground(ACCENT_COLOR); // Cor de destaque
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> backToLogin());
        panel.add(backButton, gbc);

        add(panel);
        setVisible(true);
    }

    private void performRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User(username, password);
        if (userDAO.registerUser(newUser)) {
            JOptionPane.showMessageDialog(this, "Cadastro realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new TelaLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário. Tente outro nome de usuário.", "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToLogin() {
        dispose();
        new TelaLogin();
    }
}
// END OF FILE: TelaCadastro.java