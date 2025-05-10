package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class RequestBloodController {
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField unitsField, hospitalField, contactField;
    @FXML private DatePicker requiredDatePicker;
    @FXML private ComboBox<String> emergencyLevelCombo;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;

    @FXML
    private void handleSubmit() throws Exception {
        if (!isValid()) return;

        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO requests (user_id, blood_group, units_needed, required_date, hospital, contact_number, emergency_level, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Pending')"
        );

        stmt.setInt(1, Session.getCurrentUser().getId());
        stmt.setString(2, bloodGroupCombo.getValue());
        stmt.setInt(3, Integer.parseInt(unitsField.getText().trim()));
        stmt.setDate(4, java.sql.Date.valueOf(requiredDatePicker.getValue()));
        stmt.setString(5, hospitalField.getText().trim());
        stmt.setString(6, contactField.getText().trim());
        stmt.setString(7, emergencyLevelCombo.getValue());
        stmt.setString(8, notesArea.getText().trim());

        int result = stmt.executeUpdate();
        setMessage(result > 0 ? "Request submitted successfully!" : "Failed to submit request", result > 0 ? "green" : "red");

        if (result > 0) clearFields();

        stmt.close();
        conn.close();
    }

    private boolean isValid() {
        if (bloodGroupCombo.getValue() == null || unitsField.getText().trim().isEmpty() ||
                requiredDatePicker.getValue() == null || hospitalField.getText().trim().isEmpty() ||
                contactField.getText().trim().isEmpty() || emergencyLevelCombo.getValue() == null) {
            setMessage("Please fill all required fields", "red");
            return false;
        }

        int units;
        try {
            units = Integer.parseInt(unitsField.getText().trim());
            if (units <= 0) {
                setMessage("Units must be greater than 0", "red");
                return false;
            }
        } catch (NumberFormatException e) {
            setMessage("Units must be a valid number", "red");
            return false;
        }

        if (requiredDatePicker.getValue().isBefore(LocalDate.now())) {
            setMessage("Required date cannot be in the past", "red");
            return false;
        }

        return true;
    }

    private void setMessage(String msg, String color) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    private void clearFields() {
        bloodGroupCombo.setValue(null);
        unitsField.clear();
        requiredDatePicker.setValue(null);
        hospitalField.clear();
        contactField.clear();
        emergencyLevelCombo.setValue(null);
        notesArea.clear();
    }

    @FXML
    private void handleCancel() {
        ((Stage) bloodGroupCombo.getScene().getWindow()).close();
    }
}
