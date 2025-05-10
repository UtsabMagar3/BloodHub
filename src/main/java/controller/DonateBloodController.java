package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.Session;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DonateBloodController {
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField quantityField;
    @FXML private TextField locationField;
    @FXML private TextArea remarksArea;

    @FXML
    private void handleSubmit() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Get current user from session
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No user logged in");
                return;
            }

            // Database operation
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO donations (user_id, blood_group, quantity_ml, location, remarks) VALUES (?, ?, ?, ?, ?)"
                 )) {

                stmt.setInt(1, currentUser.getId());
                stmt.setString(2, bloodGroupCombo.getValue());
                stmt.setInt(3, Integer.parseInt(quantityField.getText().trim()));
                stmt.setString(4, locationField.getText().trim());
                stmt.setString(5, remarksArea.getText().trim());

                int result = stmt.executeUpdate();
                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Blood donation recorded successfully!");
                    handleCancel();
                }

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (bloodGroupCombo.getValue() == null || bloodGroupCombo.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select blood group");
            return false;
        }

        if (quantityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter quantity");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be a valid number");
            return false;
        }

        if (locationField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter location");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) bloodGroupCombo.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}