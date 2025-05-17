package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import util.DatabaseConnection;
import util.PasswordHasher;
import util.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField emailField;  // Field for entering email
    @FXML private PasswordField passwordField;  // Field for entering password
    @FXML private Label messageLabel;  // Label to display messages (e.g., error or success)

    // Handles the login process
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();  // Get the entered email
        String password = passwordField.getText().trim();  // Get the entered password

        // Check if email or password is empty
        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both email and password.");
            return;
        }
        String hashedPassword = PasswordHasher.hashPassword(password);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM users WHERE email = ? AND password = ?")) {

            stmt.setString(1, email);  // Set email parameter in query
            stmt.setString(2, hashedPassword);  // Set password parameter in query
            ResultSet rs = stmt.executeQuery();  // Execute query

            if (rs.next()) {  // If user found
                // Create User object from result set
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("blood_group"),
                        rs.getString("role")
                );
                Session.setCurrentUser(user);  // Store user in session

                // Load appropriate view based on user role
                String fxml = user.getRole().equals("Admin") ? "/view/AdminPanel.fxml" : "/view/Home.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();  // Load the next scene

                // If it's the Home view, pass the user object to the controller
                if (loader.getController() instanceof HomeController) {
                    ((HomeController) loader.getController()).setUser(user);
                }

                // Set the new scene on the current stage
                Stage stage = (Stage) messageLabel.getScene().getWindow();
                stage.setScene(new Scene(root));  // Switch to the loaded scene
            } else {
                messageLabel.setText("Invalid email or password.");  // Display error if login fails
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error occurred during login.");  // Display error in case of exception
        }
    }

    // Redirects user to the register screen
    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            // Load the Register view (FXML)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Register.fxml"));
            Parent root = loader.load();

            // Get the current stage and switch scenes to the Register view
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();  // Log any errors during view loading
        }
    }
}
