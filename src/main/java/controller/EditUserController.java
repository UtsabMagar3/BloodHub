package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditUserController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private ComboBox<String> roleCombo;

    private User user;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        roleCombo.getItems().addAll("Admin", "User");
    }

    public void setUser(User user) {
        this.user = user;
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        bloodGroupCombo.setValue(user.getBloodGroup());
        roleCombo.setValue(user.getRole());
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE users SET full_name=?, email=?, blood_group=?, role=? WHERE id=?")) {

                stmt.setString(1, fullNameField.getText());
                stmt.setString(2, emailField.getText());
                stmt.setString(3, bloodGroupCombo.getValue());
                stmt.setString(4, roleCombo.getValue());
                stmt.setInt(5, user.getId());

                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    saveClicked = true;
                    closeDialog();
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to update user");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (fullNameField.getText().trim().isEmpty()) {
            errorMessage += "Full name is required!\n";
        }
        if (emailField.getText().trim().isEmpty()) {
            errorMessage += "Email is required!\n";
        }
        if (bloodGroupCombo.getValue() == null) {
            errorMessage += "Blood group is required!\n";
        }
        if (roleCombo.getValue() == null) {
            errorMessage += "Role is required!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }
}