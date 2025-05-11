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

public class UserViewInventoryController {
    @FXML private TableView<BloodInventory> inventoryTable;
    @FXML private TableColumn<BloodInventory, String> bloodGroupColumn;
    @FXML private TableColumn<BloodInventory, Integer> unitsColumn;
    @FXML private TableColumn<BloodInventory, String> statusColumn;
    @FXML private TableColumn<BloodInventory, Void> actionColumn;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private Label statusLabel;

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

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button requestButton = new Button("Request Blood");

            {
                requestButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                requestButton.setOnAction(event -> {
                    BloodInventory inventory = getTableView().getItems().get(getIndex());
                    handleBloodRequest(inventory);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BloodInventory inventory = getTableView().getItems().get(getIndex());
                    requestButton.setDisable(inventory.getQuantity() <= 0);
                    setGraphic(requestButton);
                }
            }
        });

        inventoryTable.setItems(inventoryList);
    }

    private void setupComboBox() {
        bloodGroupCombo.getItems().addAll("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupCombo.setValue("All");
        bloodGroupCombo.setOnAction(e -> handleSearch());
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

    private void loadInventory() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT blood_group, total_units FROM inventory")) {

            ResultSet rs = stmt.executeQuery();
            inventoryList.clear();

            while (rs.next()) {
                String bloodGroup = rs.getString("blood_group");
                int units = rs.getInt("total_units");
                String status = determineStatus(units);
                inventoryList.add(new BloodInventory(bloodGroup, units, status));
            }

            if (inventoryList.isEmpty()) {
                showAlert("No Stock", "No blood units available in inventory", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load inventory", Alert.AlertType.ERROR);
        }
    }

    private void filterInventory(String bloodGroup) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT blood_group, total_units FROM inventory WHERE blood_group = ?")) {

            stmt.setString(1, bloodGroup);
            ResultSet rs = stmt.executeQuery();
            inventoryList.clear();

            if (rs.next()) {
                int units = rs.getInt("total_units");
                String status = determineStatus(units);
                inventoryList.add(new BloodInventory(bloodGroup, units, status));
            } else {
                showAlert("No Stock", "No blood units available for blood group: " + bloodGroup, Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to filter inventory", Alert.AlertType.ERROR);
        }
    }

    private String determineStatus(int units) {
        if (units <= 0) return "Not Available";
        if (units <= 5) return "Low Stock";
        return "Available";
    }

    private void handleBloodRequest(BloodInventory inventory) {
        if (inventory.getQuantity() <= 0) {
            showAlert("No Stock",
                     "Sorry, blood group " + inventory.getBloodGroup() + " is currently not available",
                     Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RequestBloodView.fxml"));
            Parent root = loader.load();

            RequestBloodController controller = loader.getController();
            controller.preSelectBloodGroup(inventory.getBloodGroup());
            controller.setAvailableUnits(inventory.getQuantity());

            Stage stage = new Stage();
            stage.setTitle("Request Blood");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open blood request form", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}