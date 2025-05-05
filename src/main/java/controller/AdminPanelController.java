// AdminPanelController.java

package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import util.Session;

import java.io.IOException;

public class AdminPanelController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Optional: Set welcome message or perform startup logic
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageUsers() {
        // Load manage users page
        loadScene("/view/ManageUsers.fxml");
    }

    @FXML
    private void handleViewDonations() {
        // Load view donations page
        loadScene("/view/ViewDonations.fxml");
    }

    @FXML
    private void handleViewRequests() {
        // Load view requests page
        loadScene("/view/ViewRequests.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        Session.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
