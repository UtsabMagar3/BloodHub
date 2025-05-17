package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML private TextField fullNameField, emailField, phoneField, addressField, dobField;  // Input fields
    @FXML private PasswordField passwordField;  // Password field
    @FXML private ComboBox<String> bloodGroupComboBox, genderComboBox;  // Combo boxes for blood group and gender
    @FXML private Label messageLabel;  // Label to display messages

    // Handle registration action
    @FXML
    public void handleRegister(ActionEvent event) {
        // Get input values
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String bloodGroup = bloodGroupComboBox.getValue();
        String gender = genderComboBox.getValue();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String dob = dobField.getText().trim();

        // Check if required fields are filled
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() ||
            bloodGroup == null || gender == null) {
            messageLabel.setText("Please fill in all required fields.");
            return;
        }

        // Hash the password
        String hashedPassword = PasswordHasher.hashPassword(password);

        // Attempt to register the user in the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (full_name, email, password, phone, address, blood_group, date_of_birth, gender) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, fullName);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword); // Store hashed password
                stmt.setString(4, phone);
                stmt.setString(5, address);
                stmt.setString(6, bloodGroup);
                stmt.setString(7, dob);
                stmt.setString(8, gender);

                int result = stmt.executeUpdate();
                if (result > 0) {
                    messageLabel.setText("Registration successful.");
                    // Clear fields after successful registration
                    clearFields();
                } else {
                    messageLabel.setText("Registration failed.");
                }
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                messageLabel.setText("Email already exists.");
            } else {
                e.printStackTrace();
                messageLabel.setText("Registration failed: " + e.getMessage());
            }
        }
    }

    // Helper method to clear form fields
    private void clearFields() {
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        phoneField.clear();
        addressField.clear();
        dobField.clear();
        bloodGroupComboBox.setValue(null);
        genderComboBox.setValue(null);
    }

    // Navigate to the login page
    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root));  // Change the scene to the login page
        } catch (Exception e) {
            e.printStackTrace();  // Log error
        }
    }
}
