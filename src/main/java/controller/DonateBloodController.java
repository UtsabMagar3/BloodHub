package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class DonateBloodController {
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField quantityField;
    @FXML private TextField locationField;
    @FXML private TextArea remarksArea;
    @FXML private Label messageLabel;

    @FXML
    private void handleSubmit() {
        if (!validateFields()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO donations (user_id, blood_group, quantity_unit, " +
                        "donation_date, location, remarks) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Session.getCurrentUser().getId());
            stmt.setString(2, bloodGroupCombo.getValue());
            stmt.setInt(3, Integer.parseInt(quantityField.getText().trim()));
            stmt.setString(4, LocalDate.now().toString());
            stmt.setString(5, locationField.getText().trim());
            stmt.setString(6, remarksArea.getText().trim());

            int result = stmt.executeUpdate();
            if (result > 0) {
                updateInventory(conn, bloodGroupCombo.getValue(),
                              Integer.parseInt(quantityField.getText().trim()));
                showMessage("Donation recorded successfully!", "success");
                clearFields();
            } else {
                showMessage("Failed to record donation", "error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error: " + e.getMessage(), "error");
        }
    }

    private void updateInventory(Connection conn, String bloodGroup, int quantity) {
        try {
            String sql = "INSERT INTO inventory (blood_group, total_units) " +
                        "VALUES (?, ?) ON DUPLICATE KEY UPDATE " +
                        "total_units = total_units + ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bloodGroup);
            stmt.setInt(2, quantity);
            stmt.setInt(3, quantity);

            int result = stmt.executeUpdate();
            if (result > 0) {
                showMessage("Blood inventory updated successfully", "success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error updating inventory: " + e.getMessage(), "error");
        }
    }

    private boolean validateFields() {
        if (bloodGroupCombo.getValue() == null || bloodGroupCombo.getValue().isEmpty() ||
            quantityField.getText().isEmpty() || locationField.getText().isEmpty()) {
            showMessage("Please fill all required fields", "error");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showMessage("Quantity must be greater than 0", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("Quantity must be a valid number", "error");
            return false;
        }

        return true;
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setStyle(type.equals("success") ?
            "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void clearFields() {
        bloodGroupCombo.setValue(null);
        quantityField.clear();
        locationField.clear();
        remarksArea.clear();
    }

    @FXML
    private void handleCancel() {
        ((Stage) bloodGroupCombo.getScene().getWindow()).close();
    }
}