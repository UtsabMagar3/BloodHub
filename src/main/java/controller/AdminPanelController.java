package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import util.Session;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminPanelController {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalDonationsLabel;
    @FXML private Label totalRequestsLabel;
    @FXML private Label inventoryLabel;

    @FXML
    public void initialize() {
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bloodhub", "root", "")) {
            Statement stmt = conn.createStatement();

            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs1.next()) totalUsersLabel.setText("Users: " + rs1.getInt(1));

            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM donations");
            if (rs2.next()) totalDonationsLabel.setText("Donations: " + rs2.getInt(1));

            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM requests");
            if (rs3.next()) totalRequestsLabel.setText("Requests: " + rs3.getInt(1));

            ResultSet rs4 = stmt.executeQuery("SELECT SUM(units) FROM inventory");
            if (rs4.next()) inventoryLabel.setText("Inventory Units: " + rs4.getInt(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        Session.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageUsers() {
        loadScene("/view/ManageUsers.fxml");
    }

    @FXML
    private void handleViewDonations() {
        loadScene("/view/ViewDonations.fxml");
    }

    @FXML
    private void handleViewRequests() {
        loadScene("/view/ViewRequests.fxml");
    }

    @FXML
    private void handleViewInventory() {
        loadScene("/view/ViewInventory.fxml");
    }


    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
