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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class ViewDonationsController {
    @FXML private TableView<DonationRecord> donationTable;
    @FXML private TableColumn<DonationRecord, Integer> idColumn;
    @FXML private TableColumn<DonationRecord, String> donorNameColumn;
    @FXML private TableColumn<DonationRecord, String> bloodGroupColumn;
    @FXML private TableColumn<DonationRecord, Integer> quantityColumn;
    @FXML private TableColumn<DonationRecord, LocalDateTime> dateColumn;
    @FXML private TableColumn<DonationRecord, String> locationColumn;
    @FXML private TableColumn<DonationRecord, String> remarksColumn;
    @FXML private TextField searchField;

    private final ObservableList<DonationRecord> donationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws Exception {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        donorNameColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        loadDonations();
    }

    private void loadDonations() throws Exception {
        donationList.clear();
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT d.*, u.full_name FROM donations d " +
                        "JOIN users u ON d.user_id = u.id " +
                        "ORDER BY d.donation_date DESC"
        );
        while (rs.next()) {
            donationList.add(new DonationRecord(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("blood_group"),
                    rs.getInt("quantity_ml"),
                    rs.getTimestamp("donation_date").toLocalDateTime(),
                    rs.getString("location"),
                    rs.getString("remarks")
            ));
        }
        donationTable.setItems(donationList);
        conn.close();
    }

    @FXML
    private void handleSearch() {
        String text = searchField.getText().toLowerCase();
        ObservableList<DonationRecord> filtered = donationList.filtered(d ->
                d.getDonorName().toLowerCase().contains(text) ||
                        d.getBloodGroup().toLowerCase().contains(text) ||
                        d.getLocation().toLowerCase().contains(text)
        );
        donationTable.setItems(filtered);
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
        Stage stage = (Stage) donationTable.getScene().getWindow();
        stage.setScene(new Scene(root, 1000, 700));
    }

    public static class DonationRecord {
        private final int id;
        private final String donorName;
        private final String bloodGroup;
        private final int quantity;
        private final LocalDateTime donationDate;
        private final String location;
        private final String remarks;

        public DonationRecord(int id, String donorName, String bloodGroup, int quantity, LocalDateTime donationDate, String location, String remarks) {
            this.id = id;
            this.donorName = donorName;
            this.bloodGroup = bloodGroup;
            this.quantity = quantity;
            this.donationDate = donationDate;
            this.location = location;
            this.remarks = remarks;
        }

        public int getId() { return id; }
        public String getDonorName() { return donorName; }
        public String getBloodGroup() { return bloodGroup; }
        public int getQuantity() { return quantity; }
        public LocalDateTime getDonationDate() { return donationDate; }
        public String getLocation() { return location; }
        public String getRemarks() { return remarks; }
    }
}
