package controller;

// Importing required JavaFX and utility classes
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import util.Session;
import util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminPanelController {

    // FXML labels for displaying statistics
    @FXML private Label totalUsersLabel;
    @FXML private Label totalDonationsLabel;
    @FXML private Label totalRequestsLabel;
    @FXML private Label inventoryLabel;

    // Automatically called after FXML is loaded; initializes the dashboard stats
    @FXML
    public void initialize() throws Exception {
        loadDashboardStats();
    }

    // Loads total counts from the database and updates the dashboard labels
    private void loadDashboardStats() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();

        // Query to count total registered users
        ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM users");
        rs1.next();
        totalUsersLabel.setText(String.valueOf(rs1.getInt(1)));

        // Query to count total donations
        ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM donations");
        rs2.next();
        totalDonationsLabel.setText(String.valueOf(rs2.getInt(1)));

        // Query to count total blood requests
        ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM requests");
        rs3.next();
        totalRequestsLabel.setText(String.valueOf(rs3.getInt(1)));

        // Query to count total blood units in inventory (uses COUNT not SUM, assumes units are in rows)
        ResultSet rs4 = stmt.executeQuery("SELECT COUNT(*) FROM inventory");
        rs4.next();
        inventoryLabel.setText(String.valueOf(rs4.getInt(1)));

        conn.close();
    }

    // Handles admin logout and redirects to Log in.fxml
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) throws IOException {
        Session.setCurrentUser(null); // Clear session
        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml")); // Load login scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    // Navigates to Manage Users screen
    @FXML
    private void handleManageUsers() throws IOException {
        loadScene("/view/ManageUsers.fxml");
    }

    // Navigates to View Donations screen
    @FXML
    private void handleViewDonations() throws IOException {
        loadScene("/view/ViewDonations.fxml");
    }

    // Navigates to View Requests screen
    @FXML
    private void handleViewRequests() throws IOException {
        loadScene("/view/ViewRequests.fxml");
    }

    // Navigates to View Inventory screen
    @FXML
    private void handleViewInventory() throws IOException {
        loadScene("/view/ViewInventory.fxml");
    }

    // Utility method for loading a new FXML scene on the same stage
    private void loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) totalUsersLabel.getScene().getWindow(); // Reuse current stage
        stage.setScene(new Scene(root, 1000, 700));
    }
}
