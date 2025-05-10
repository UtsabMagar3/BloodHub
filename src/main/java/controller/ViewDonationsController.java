package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ViewDonationsController {
    @FXML private TableView<DonationRecord> donationTable;
    @FXML private TableColumn<DonationRecord, Integer> idColumn;
    @FXML private TableColumn<DonationRecord, String> donorNameColumn;
    @FXML private TableColumn<DonationRecord, String> bloodGroupColumn;
    @FXML private TableColumn<DonationRecord, Integer> quantityColumn;
    @FXML private TableColumn<DonationRecord, String> dateColumn;
    @FXML private TableColumn<DonationRecord, String> locationColumn;
    @FXML private TableColumn<DonationRecord, String> remarksColumn;
    @FXML private TableColumn<DonationRecord, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label totalUnitsLabel;

    private final ObservableList<DonationRecord> donationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws Exception {
        setupTableColumns();
        loadDonations();
        updateTotalUnits();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        donorNameColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantityUnit"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    DonationRecord donation = getTableView().getItems().get(getIndex());
                    handleDelete(donation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void loadDonations() throws Exception {
        donationList.clear();
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT d.id, u.full_name, d.blood_group, d.quantity_unit, " +
                        "d.donation_date, d.location, d.remarks " +
                        "FROM donations d " +
                        "JOIN users u ON d.user_id = u.id " +
                        "ORDER BY d.donation_date DESC"
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
        donationTable.setItems(donationList);
    }

    private void updateTotalUnits() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT SUM(quantity_unit) FROM donations");

        if (rs.next()) {
            int total = rs.getInt(1);
            totalUnitsLabel.setText("Total Units: " + total);
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<DonationRecord> filteredList = donationList.filtered(donation ->
                donation.getDonorName().toLowerCase().contains(searchText) ||
                        donation.getBloodGroup().toLowerCase().contains(searchText) ||
                        donation.getLocation().toLowerCase().contains(searchText)
        );
        donationTable.setItems(filteredList);
    }

    @FXML
    private void handleBack() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
        Stage stage = (Stage) donationTable.getScene().getWindow();
        stage.setScene(new Scene(root, 1000, 700));
    }

    private void handleDelete(DonationRecord donation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this donation record?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    deleteDonation(donation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void deleteDonation(DonationRecord donation) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();

        int result = stmt.executeUpdate("DELETE FROM donations WHERE id = " + donation.getId());
        if (result > 0) {
            donationList.remove(donation);
            updateTotalUnits();
            showAlert("Success", "Donation record deleted successfully");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

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
