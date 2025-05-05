package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {
    @FXML private TextField fullNameField, emailField, phoneField, addressField, dobField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> bloodGroupComboBox, genderComboBox;
    @FXML private Label messageLabel;

    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String bloodGroup = bloodGroupComboBox.getValue();
        String gender = genderComboBox.getValue();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String dob = dobField.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || bloodGroup == null || gender == null) {
            messageLabel.setText("Please fill in all required fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (full_name, email, password, phone, address, blood_group, date_of_birth, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, fullName);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.setString(4, phone);
                stmt.setString(5, address);
                stmt.setString(6, bloodGroup);
                stmt.setString(7, dob);
                stmt.setString(8, gender);
                stmt.executeUpdate();
                messageLabel.setText("Registration successful.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Registration failed.");
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
