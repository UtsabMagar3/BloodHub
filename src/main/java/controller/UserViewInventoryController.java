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
    // Table and columns to display the inventory
    @FXML private TableView<BloodInventory> inventoryTable;
    @FXML private TableColumn<BloodInventory, String> bloodGroupColumn;
    @FXML private TableColumn<BloodInventory, Integer> unitsColumn;
    @FXML private TableColumn<BloodInventory, String> statusColumn;
    @FXML private TableColumn<BloodInventory, Void> actionColumn;

    // ComboBox for filtering blood groups and a label for status messages
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private Label statusLabel;

    // List to hold inventory data
    private ObservableList<BloodInventory> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup the table and ComboBox, then load the inventory
        setupTable();
        setupComboBox();
        loadInventory();
    }

    // Set up the table to display inventory data
    private void setupTable() {
        // Map table columns to data properties
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add a button in each row for blood request
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button requestButton = new Button("Request Blood");

            {
                // Style the button
                requestButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

                // Action for the button when clicked
                requestButton.setOnAction(event -> {
                    BloodInventory inventory = getTableView().getItems().get(getIndex());
                    handleBloodRequest(inventory);
                });
            }

            // Display the button if there are available units for the selected blood group
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BloodInventory inventory = getTableView().getItems().get(getIndex());
                    requestButton.setDisable(inventory.getQuantity() <= 0); // Disable if no units available
                    setGraphic(requestButton);
                }
            }
        });

        // Bind the table to the inventory list
        inventoryTable.setItems(inventoryList);
    }

    // Set up the ComboBox to filter inventory based on blood group
    private void setupComboBox() {
        // Add options to filter blood groups
        bloodGroupCombo.getItems().addAll("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupCombo.setValue("All"); // Default to "All"
        bloodGroupCombo.setOnAction(e -> handleSearch()); // Handle filtering when a selection is made
    }

    // Handle filtering when a blood group is selected in the ComboBox
    @FXML
    private void handleSearch() {
        String selectedGroup = bloodGroupCombo.getValue();
        if ("All".equals(selectedGroup)) {
            loadInventory(); // Load all inventory
        } else {
            filterInventory(selectedGroup); // Filter based on selected blood group
        }
    }

    // Load all the blood inventory from the database
    private void loadInventory() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT blood_group, total_units FROM inventory")) {

            ResultSet rs = stmt.executeQuery();
            inventoryList.clear(); // Clear previous inventory data

            // Loop through result set and add data to the inventory list
            while (rs.next()) {
                String bloodGroup = rs.getString("blood_group");
                int units = rs.getInt("total_units");
                String status = determineStatus(units); // Determine the availability status
                inventoryList.add(new BloodInventory(bloodGroup, units, status));
            }

            // Show an alert if no inventory data was found
            if (inventoryList.isEmpty()) {
                showAlert("No Stock", "No blood units available in inventory", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load inventory", Alert.AlertType.ERROR);
        }
    }

    // Filter inventory based on the selected blood group
    private void filterInventory(String bloodGroup) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT blood_group, total_units FROM inventory WHERE blood_group = ?")) {

            stmt.setString(1, bloodGroup);
            ResultSet rs = stmt.executeQuery();
            inventoryList.clear(); // Clear previous inventory data

            // If the selected blood group is found, add it to the list
            if (rs.next()) {
                int units = rs.getInt("total_units");
                String status = determineStatus(units); // Determine the status
                inventoryList.add(new BloodInventory(bloodGroup, units, status));
            } else {
                // Show an alert if no data for the selected blood group
                showAlert("No Stock", "No blood units available for blood group: " + bloodGroup, Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to filter inventory", Alert.AlertType.ERROR);
        }
    }

    // Determine the status of a blood group based on available units
    private String determineStatus(int units) {
        if (units <= 0) return "Not Available";
        if (units <= 5) return "Low Stock";
        return "Available";
    }

    // Open the blood request form when the "Request Blood" button is clicked
    private void handleBloodRequest(BloodInventory inventory) {
        if (inventory.getQuantity() <= 0) {
            showAlert("No Stock", "Sorry, blood group " + inventory.getBloodGroup() + " is currently not available", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Load the RequestBloodView.fxml and pass the selected blood group and available units
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RequestBloodView.fxml"));
            Parent root = loader.load();

            RequestBloodController controller = loader.getController();
            controller.preSelectBloodGroup(inventory.getBloodGroup());
            controller.setAvailableUnits(inventory.getQuantity());

            // Open a new window for the blood request form
            Stage stage = new Stage();
            stage.setTitle("Request Blood");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open blood request form", Alert.AlertType.ERROR);
        }
    }

    // Show an alert with a message and type (Information, Warning, or Error)
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
