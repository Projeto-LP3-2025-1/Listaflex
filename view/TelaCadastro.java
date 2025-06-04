package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class TelaCadastro extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private UserDAO userDAO;

    public TelaCadastro() {
        userDAO = new UserDAO();

        setTitle("Cadastro - Listaflex");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirmar Senha:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton registerButton = new JButton("Cadastrar");
        registerButton.addActionListener(e -> performRegistration());
        panel.add(registerButton, gbc);

        // Back to Login Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        JButton backButton = new JButton("Voltar ao Login");
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
        if (new UserDAO().registerUser(newUser)) {
            JOptionPane.showMessageDialog(this, "Cadastro realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new TelaLogin(); // Go back to login after successful registration
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário. Tente outro nome de usuário.", "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToLogin() {
        dispose();
        new TelaLogin();
    }
}