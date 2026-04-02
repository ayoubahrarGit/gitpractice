package com.stockmanager.controller;

import com.stockmanager.App;
import com.stockmanager.dao.UserDAO;
import com.stockmanager.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * Controller for the login screen.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez saisir un identifiant et un mot de passe.");
            return;
        }

        Optional<User> userOpt = userDAO.authenticate(username, password);
        if (userOpt.isEmpty()) {
            errorLabel.setText("Identifiant ou mot de passe incorrect.");
            passwordField.clear();
            return;
        }

        App.getInstance().showStockView(userOpt.get());
    }
}
