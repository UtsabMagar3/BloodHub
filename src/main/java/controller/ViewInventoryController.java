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
    @FXML private TableView<BloodInventory> inventoryTable;
    @FXML private TableColumn<BloodInventory, String> bloodGroupColumn;
    @FXML private TableColumn<BloodInventory, Integer> unitsColumn;
    @FXML private TableColumn<BloodInventory, String> statusColumn;
    @FXML private ComboBox<String> bloodGroupCombo;

    private ObservableList<BloodInventory> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupComboBox();
        loadInventory();
    }

    private void setupTable() {
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventoryTable.setItems(inventoryList);
    }

    private void setupComboBox() {
        bloodGroupCombo.getItems().addAll("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupCombo.setValue("All");
    }

    @FXML
    private void handleSearch() {
        String selectedGroup = bloodGroupCombo.getValue();
        if ("All".equals(selectedGroup)) {
            loadInventory();
        } else {
            filterInventory(selectedGroup);
        }
    }

    private void filterInventory(String bloodGroup) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM inventory WHERE blood_group = ?")) {
            stmt.setString(1, bloodGroup);
            ResultSet rs = stmt.executeQuery();

            inventoryList.clear();
            while (rs.next()) {
                inventoryList.add(new BloodInventory(
                    rs.getString("blood_group"),
                    rs.getInt("total_units"),
                    determineStatus(rs.getInt("total_units"))
                ));
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to filter inventory", Alert.AlertType.ERROR);
        }
    }

    private void loadInventory() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM inventory")) {
            ResultSet rs = stmt.executeQuery();

            inventoryList.clear();
            while (rs.next()) {
                inventoryList.add(new BloodInventory(
                    rs.getString("blood_group"),
                    rs.getInt("total_units"),
                    determineStatus(rs.getInt("total_units"))
                ));
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load inventory", Alert.AlertType.ERROR);
        }
    }

    private String determineStatus(int units) {
        if (units <= 0) return "Critical";
        if (units <= 5) return "Low";
        return "Available";
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            showAlert("Error", "Navigation failed", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}