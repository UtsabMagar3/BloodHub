package controller;

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

    @FXML private Label totalUsersLabel;
    @FXML private Label totalDonationsLabel;
    @FXML private Label totalRequestsLabel;
    @FXML private Label inventoryLabel;

    @FXML
    public void initialize() throws Exception {
        loadDashboardStats();
    }

    private void loadDashboardStats() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();

        // Count total registered users
        ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM users");
        rs1.next();
        totalUsersLabel.setText(String.valueOf(rs1.getInt(1)));

        // Count total donations
        ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM donations");
        rs2.next();
        totalDonationsLabel.setText(String.valueOf(rs2.getInt(1)));

        // Count total blood requests
        ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM requests");
        rs3.next();
        totalRequestsLabel.setText(String.valueOf(rs3.getInt(1)));

        // Sum total blood units - assuming 'quantity' is the column name
        ResultSet rs4 = stmt.executeQuery("SELECT COUNT(*) FROM inventory");
        rs4.next();
        inventoryLabel.setText(String.valueOf(rs4.getInt(1)));

        conn.close();
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) throws IOException {
        Session.setCurrentUser(null);
        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @FXML
    private void handleManageUsers() throws IOException {
        loadScene("/view/ManageUsers.fxml");
    }

    @FXML
    private void handleViewDonations() throws IOException {
        loadScene("/view/ViewDonations.fxml");
    }

    @FXML
    private void handleViewRequests() throws IOException {
        loadScene("/view/ViewRequests.fxml");
    }

    @FXML
    private void handleViewInventory() throws IOException {
        loadScene("/view/ViewInventory.fxml");
    }

    private void loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
        stage.setScene(new Scene(root, 1000, 700));
    }
}