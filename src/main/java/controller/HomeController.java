package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.User;
import util.DatabaseConnection;
import util.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HomeController {

    private User loggedInUser;

    // Sets the logged-in user and stores it in session
    public void setUser(User user) {
        this.loggedInUser = user;
        Session.setCurrentUser(user);
    }

    // Show unread notifications and mark them as read
    @FXML
    private void handleNotifications() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, message, created_at FROM notifications " +
                            "WHERE user_id = ? AND is_read = FALSE " +
                            "ORDER BY created_at DESC");

            stmt.setInt(1, Session.getCurrentUser().getId());
            ResultSet rs = stmt.executeQuery();

            StringBuilder notifications = new StringBuilder();
            boolean hasNotifications = false;

            // Collect notifications and mark each as read
            while (rs.next()) {
                hasNotifications = true;
                notifications.append("• ")
                        .append(rs.getString("message"))
                        .append("\n")
                        .append("  (")
                        .append(rs.getTimestamp("created_at"))
                        .append(")\n\n");

                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE notifications SET is_read = TRUE WHERE id = ?");
                updateStmt.setInt(1, rs.getInt("id"));
                updateStmt.executeUpdate();
            }

            // Display notifications in alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText(null);
            alert.setContentText(hasNotifications ? notifications.toString() : "No new notifications");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load notifications");
            alert.showAndWait();
        }
    }

    // Show user profile details
    @FXML
    private void handleProfile() {
        User user = Session.getCurrentUser();
        if (user != null) {
            String profile = "Name: " + user.getFullName() +
                    "\nEmail: " + user.getEmail() +
                    "\nBlood Group: " + user.getBloodGroup() +
                    "\nRole: " + user.getRole();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile");
            alert.setHeaderText("User Profile");
            alert.setContentText(profile);
            alert.showAndWait();
        }
    }

    // Clear session and redirect to login screen
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

    // Load donation form
    @FXML
    private void handleDonateBlood() {
        openWindow("/view/DonateBloodView.fxml", "Donate Blood");
    }

    // Load request form
    @FXML
    private void handleRequestBlood() {
        openWindow("/view/RequestBloodView.fxml", "Request Blood");
    }

    // Load inventory view
    @FXML
    private void handleInventory() {
        openWindow("/view/UserViewInventory.fxml", "Blood Inventory");
    }

    // Utility method to load new windows
    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
