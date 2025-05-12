package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.BloodInventory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewInventoryController {
    // FXML elements for the inventory table and combo box
    @FXML private TableView<BloodInventory> inventoryTable;
    @FXML private TableColumn<BloodInventory, String> bloodGroupColumn;
    @FXML private TableColumn<BloodInventory, Integer> unitsColumn;
    @FXML private TableColumn<BloodInventory, String> statusColumn;
    @FXML private ComboBox<String> bloodGroupCombo;

    // Observable list to hold the inventory data
    private ObservableList<BloodInventory> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();  // Set up table columns for displaying inventory data
        setupComboBox();  // Set up combo box for filtering by blood group
        loadInventory();  // Load inventory data from database
    }

    private void setupTable() {
        // Set cell value factories for each column to map data properties to table columns
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventoryTable.setItems(inventoryList);  // Bind the table view with the inventory list
    }

    private void setupComboBox() {
        // Populate combo box with blood group options
        bloodGroupCombo.getItems().addAll("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupCombo.setValue("All");  // Set default value to "All"
    }

    @FXML
    private void handleSearch() {
        // When a search is performed, filter the inventory based on selected blood group
        String selectedGroup = bloodGroupCombo.getValue();
        if ("All".equals(selectedGroup)) {
            loadInventory();  // Load all inventory if "All" is selected
        } else {
            filterInventory(selectedGroup);  // Filter inventory based on selected blood group
        }
    }

    private void filterInventory(String bloodGroup) {
        // Filter inventory by blood group using a prepared statement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM inventory WHERE blood_group = ?")) {
            stmt.setString(1, bloodGroup);  // Set the blood group parameter
            ResultSet rs = stmt.executeQuery();  // Execute the query

            inventoryList.clear();  // Clear the previous results
            while (rs.next()) {
                // Add filtered records to the inventory list
                inventoryList.add(new BloodInventory(
                        rs.getString("blood_group"),
                        rs.getInt("total_units"),
                        determineStatus(rs.getInt("total_units"))
                ));
            }
        } catch (Exception e) {
            // Display an error alert if filtering fails
            showAlert("Error", "Failed to filter inventory", Alert.AlertType.ERROR);
        }
    }

    private void loadInventory() {
        // Load the entire inventory from the database
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM inventory")) {
            ResultSet rs = stmt.executeQuery();  // Execute the query

            inventoryList.clear();  // Clear the previous results
            while (rs.next()) {
                // Add each record to the inventory list
                inventoryList.add(new BloodInventory(
                        rs.getString("blood_group"),
                        rs.getInt("total_units"),
                        determineStatus(rs.getInt("total_units"))
                ));
            }
        } catch (Exception e) {
            // Display an error alert if loading fails
            showAlert("Error", "Failed to load inventory", Alert.AlertType.ERROR);
        }
    }

    private String determineStatus(int units) {
        // Determine the status of blood group based on the number of units
        if (units <= 0) return "Critical";  // If no units are available, status is Critical
        if (units <= 5) return "Low";  // If there are fewer than or equal to 5 units, status is Low
        return "Available";  // If there are more than 5 units, status is Available
    }

    @FXML
    private void handleBack() {
        // Navigate back to the Admin Panel
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));  // Set the new scene
        } catch (Exception e) {
            // Display an error alert if navigation fails
            showAlert("Error", "Navigation failed", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        // Display an alert with the given title, content, and alert type
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
