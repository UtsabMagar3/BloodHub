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
import util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ViewDonationsController {
    // Table and columns for displaying donation data
    @FXML private TableView<DonationRecord> donationTable;
    @FXML private TableColumn<DonationRecord, Integer> idColumn;
    @FXML private TableColumn<DonationRecord, String> donorNameColumn;
    @FXML private TableColumn<DonationRecord, String> bloodGroupColumn;
    @FXML private TableColumn<DonationRecord, Integer> quantityColumn;
    @FXML private TableColumn<DonationRecord, String> dateColumn;
    @FXML private TableColumn<DonationRecord, String> locationColumn;
    @FXML private TableColumn<DonationRecord, String> remarksColumn;
    @FXML private TableColumn<DonationRecord, Void> actionsColumn;
    @FXML private TextField searchField;  // Field for searching donations
    @FXML private Label totalUnitsLabel;  // Label to display total units of blood donated

    // Observable list that will hold donation data
    private final ObservableList<DonationRecord> donationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws Exception {
        setupTableColumns();  // Initialize the table columns
        loadDonations();  // Load donations from the database
        updateTotalUnits();  // Update the total donated units display
    }

    // Set up table columns to map the data properties
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        donorNameColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantityUnit"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        // Create a button for deleting a donation record in each row
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                // Style the delete button
                deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    DonationRecord donation = getTableView().getItems().get(getIndex()); // Get selected row
                    handleDelete(donation);  // Call method to handle deletion
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);  // Show or hide the delete button
            }
        });
    }

    // Load donation records from the database
    private void loadDonations() throws Exception {
        donationList.clear();  // Clear any previous records
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT d.id, u.full_name, d.blood_group, d.quantity_unit, " +
                        "d.donation_date, d.location, d.remarks " +
                        "FROM donations d " +
                        "JOIN users u ON d.user_id = u.id " +
                        "ORDER BY d.donation_date DESC"  // Get the donations, ordered by date
        );

        while (rs.next()) {
            donationList.add(new DonationRecord(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("blood_group"),
                    rs.getInt("quantity_unit"),
                    rs.getString("donation_date"),
                    rs.getString("location"),
                    rs.getString("remarks")
            ));
        }
        donationTable.setItems(donationList);  // Set the table items to the donation list
    }

    // Update the total number of units donated and display it
    private void updateTotalUnits() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT SUM(quantity_unit) FROM donations");  // Get the sum of all donated units

        if (rs.next()) {
            int total = rs.getInt(1);  // Get the total value from the query result
            totalUnitsLabel.setText("Total Units: " + total);  // Update the label to show total units
        }
    }

    // Search functionality for filtering donations by donor name, blood group, or location
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();  // Get text from the search field
        ObservableList<DonationRecord> filteredList = donationList.filtered(donation ->
                donation.getDonorName().toLowerCase().contains(searchText) ||
                        donation.getBloodGroup().toLowerCase().contains(searchText) ||
                        donation.getLocation().toLowerCase().contains(searchText)  // Filter by these fields
        );
        donationTable.setItems(filteredList);  // Update the table to display the filtered results
    }

    // Handle the back action, loading the Admin Panel view
    @FXML
    private void handleBack() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
        Stage stage = (Stage) donationTable.getScene().getWindow();
        stage.setScene(new Scene(root, 1000, 700));  // Load the Admin Panel view in the same window
    }

    // Confirm and delete the selected donation record
    private void handleDelete(DonationRecord donation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this donation record?",
                ButtonType.YES, ButtonType.NO);  // Confirmation dialog before deletion

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    deleteDonation(donation);  // Delete the record if confirmed
                } catch (Exception e) {
                    throw new RuntimeException(e);  // Handle exceptions
                }
            }
        });
    }

    // Delete the donation record from the database and update the UI
    private void deleteDonation(DonationRecord donation) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();

        int result = stmt.executeUpdate("DELETE FROM donations WHERE id = " + donation.getId());  // Execute the delete query
        if (result > 0) {
            donationList.remove(donation);  // Remove the deleted donation from the list
            updateTotalUnits();  // Update the total units label
            showAlert("Success", "Donation record deleted successfully");  // Show success message
        }
    }

    // Show an informational alert with a title and content
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);  // Set the content of the alert
        alert.showAndWait();  // Display the alert and wait for user response
    }

    // DonationRecord class to represent a donation record
    public static class DonationRecord {
        private final int id;
        private final String donorName;
        private final String bloodGroup;
        private final int quantityUnit;
        private final String donationDate;
        private final String location;
        private final String remarks;

        public DonationRecord(int id, String donorName, String bloodGroup, int quantityUnit, String donationDate, String location, String remarks) {
            this.id = id;
            this.donorName = donorName;
            this.bloodGroup = bloodGroup;
            this.quantityUnit = quantityUnit;
            this.donationDate = donationDate;
            this.location = location;
            this.remarks = remarks;
        }

        public int getId() { return id; }
        public String getDonorName() { return donorName; }
        public String getBloodGroup() { return bloodGroup; }
        public int getQuantityUnit() { return quantityUnit; }
        public String getDonationDate() { return donationDate; }
        public String getLocation() { return location; }
        public String getRemarks() { return remarks; }
    }
}
