package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class RequestBloodController {
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField unitsField;
    @FXML private DatePicker requiredDatePicker;
    @FXML private TextField hospitalField;
    @FXML private ComboBox<String> emergencyLevelCombo;
    @FXML private TextArea reasonArea;
    @FXML private Label messageLabel;

    private int availableUnits = 0;

    @FXML
    public void initialize() {
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        emergencyLevelCombo.getItems().addAll("Normal", "Urgent", "Critical");
        requiredDatePicker.setValue(LocalDate.now());

        // Add listener for blood group selection
        bloodGroupCombo.setOnAction(e -> checkAvailability());
    }

    private void checkAvailability() {
        String selectedBloodGroup = bloodGroupCombo.getValue();
        if (selectedBloodGroup != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT total_units FROM inventory WHERE blood_group = ?")) {

                stmt.setString(1, selectedBloodGroup);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    availableUnits = rs.getInt("total_units");
                    if (availableUnits <= 0) {
                        showMessage("No blood units available for " + selectedBloodGroup, "error");
                        unitsField.setDisable(true);
                    } else {
                        showMessage(availableUnits + " units available for " + selectedBloodGroup, "success");
                        unitsField.setDisable(false);
                    }
                } else {
                    availableUnits = 0;
                    showMessage("No blood units available for " + selectedBloodGroup, "error");
                    unitsField.setDisable(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Error checking availability", "error");
            }
        }
    }

    public void setAvailableUnits(int units) {
        this.availableUnits = units;
        if (units <= 0) {
            unitsField.setDisable(true);
            showMessage("No blood units available for this blood group", "error");
        } else {
            unitsField.setPromptText("Max units available: " + units);
            unitsField.setDisable(false);
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateFields()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            int requestedUnits = Integer.parseInt(unitsField.getText().trim());

            // Recheck availability before submitting
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT total_units FROM inventory WHERE blood_group = ?");
            checkStmt.setString(1, bloodGroupCombo.getValue());
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next() || rs.getInt("total_units") < requestedUnits) {
                showMessage("Not enough units available. Please check availability again.", "error");
                return;
            }

            // Insert request with Pending status
            String sql = "INSERT INTO requests (user_id, blood_group, units_needed, " +
                    "required_date, hospital, emergency_level, request_reason, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Session.getCurrentUser().getId());
            stmt.setString(2, bloodGroupCombo.getValue());
            stmt.setInt(3, requestedUnits);
            stmt.setString(4, requiredDatePicker.getValue().toString());
            stmt.setString(5, hospitalField.getText().trim());
            stmt.setString(6, emergencyLevelCombo.getValue());
            stmt.setString(7, reasonArea.getText().trim());

            int result = stmt.executeUpdate();
            if (result > 0) {
                showMessage("Blood request submitted successfully! Waiting for approval.", "success");
                clearFields();
                ((Stage) bloodGroupCombo.getScene().getWindow()).close();
            } else {
                showMessage("Failed to submit request", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error: " + e.getMessage(), "error");
        }
    }

    private boolean validateFields() {
        if (bloodGroupCombo.getValue() == null || unitsField.getText().isEmpty() ||
                requiredDatePicker.getValue() == null || hospitalField.getText().isEmpty() ||
                emergencyLevelCombo.getValue() == null) {
            showMessage("Please fill all required fields", "error");
            return false;
        }

        try {
            int requestedUnits = Integer.parseInt(unitsField.getText().trim());
            if (requestedUnits <= 0) {
                showMessage("Units must be greater than 0", "error");
                return false;
            }
            if (requestedUnits > availableUnits) {
                showMessage("Cannot request more than available units (" + availableUnits + ")", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("Units must be a valid number", "error");
            return false;
        }

        if (requiredDatePicker.getValue().isBefore(LocalDate.now())) {
            showMessage("Required date cannot be in the past", "error");
            return false;
        }

        return true;
    }

    public void preSelectBloodGroup(String bloodGroup) {
        bloodGroupCombo.setValue(bloodGroup);
        checkAvailability();
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setStyle(type.equals("success") ?
                "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void clearFields() {
        bloodGroupCombo.setValue(null);
        unitsField.clear();
        requiredDatePicker.setValue(LocalDate.now());
        hospitalField.clear();
        emergencyLevelCombo.setValue(null);
        reasonArea.clear();
    }

    @FXML
    private void handleCancel() {
        ((Stage) bloodGroupCombo.getScene().getWindow()).close();
    }
}
