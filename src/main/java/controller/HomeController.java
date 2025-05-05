package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.User;
import util.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;


public class HomeController {

    private User loggedInUser;

    public void setUser(User user) {
        this.loggedInUser = user;
        Session.setCurrentUser(user);
    }

    @FXML
    private void handleNotifications() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText(null);
        alert.setContentText("Here you would see unread notifications.");
        alert.showAndWait();
    }

    @FXML
    private void handleProfile() {
        User user = Session.getCurrentUser();
        if (user != null) {
            String profile = "Name: " + user.getFullName() + "\nEmail: " + user.getEmail() +
                    "\nBlood Group: " + user.getBloodGroup() + "\nRole: " + user.getRole();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile");
            alert.setHeaderText("User Profile");
            alert.setContentText(profile);
            alert.showAndWait();
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
    @FXML
    private void handleDonateBlood() {
        openWindow("/view/DonateBloodView.fxml", "Donate Blood");
    }

    @FXML
    private void handleRequestBlood() {
        openWindow("/view/RequestBloodView.fxml", "Request Blood");
    }

    @FXML
    private void handleInventory() {
        openWindow("/view/InventoryView.fxml", "Blood Inventory");
    }

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
