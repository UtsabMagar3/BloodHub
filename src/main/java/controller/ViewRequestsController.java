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
import model.BloodRequest;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ViewRequestsController {
    @FXML private TableView<BloodRequest> requestTable;
    @FXML private TableColumn<BloodRequest, Integer> idColumn;
    @FXML private TableColumn<BloodRequest, String> userColumn;
    @FXML private TableColumn<BloodRequest, String> bloodGroupColumn;
    @FXML private TableColumn<BloodRequest, Integer> unitsColumn;
    @FXML private TableColumn<BloodRequest, LocalDate> requiredDateColumn;
    @FXML private TableColumn<BloodRequest, String> hospitalColumn;
    @FXML private TableColumn<BloodRequest, String> emergencyColumn;
    @FXML private TableColumn<BloodRequest, String> statusColumn;
    @FXML private TableColumn<BloodRequest, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<BloodRequest> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadRequests();
        setupStatusFilter();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("unitsNeeded"));
        requiredDateColumn.setCellValueFactory(new PropertyValueFactory<>("requiredDate"));
        hospitalColumn.setCellValueFactory(new PropertyValueFactory<>("hospital"));
        emergencyColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyLevel"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox buttons = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                approveBtn.setOnAction(event -> {
                    BloodRequest request = getTableView().getItems().get(getIndex());
                    updateRequestStatus(request, "Approved");
                });

                rejectBtn.setOnAction(event -> {
                    BloodRequest request = getTableView().getItems().get(getIndex());
                    updateRequestStatus(request, "Rejected");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BloodRequest request = getTableView().getItems().get(getIndex());
                    if ("Pending".equals(request.getStatus())) {
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadRequests() {
        requestList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.*, u.full_name FROM requests r " +
                     "JOIN users u ON r.user_id = u.id")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BloodRequest request = new BloodRequest(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("blood_group"),
                    rs.getInt("units_needed"),
                    rs.getDate("required_date").toLocalDate(),
                    rs.getString("hospital"),
                    rs.getString("emergency_level"),
                    rs.getString("status")
                );
                requestList.add(request);
            }
            requestTable.setItems(requestList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load blood requests", Alert.AlertType.ERROR);
        }
    }

    private void updateRequestStatus(BloodRequest request, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First update the request status
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE requests SET status = ? WHERE id = ?");
            stmt.setString(1, newStatus);
            stmt.setInt(2, request.getId());

            int result = stmt.executeUpdate();
            if (result > 0) {
                // Create notification
                PreparedStatement notifStmt = conn.prepareStatement(
                        "INSERT INTO notifications (user_id, message) " +
                                "SELECT user_id, ? FROM requests WHERE id = ?");

                String message = String.format("Your blood request for %d units of %s has been %s.",
                        request.getUnitsNeeded(),
                        request.getBloodGroup(),
                        newStatus.toLowerCase());

                notifStmt.setString(1, message);
                notifStmt.setInt(2, request.getId());
                notifStmt.executeUpdate();

                request.setStatus(newStatus);
                requestTable.refresh();
                showAlert("Success", "Request status updated successfully", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update request status", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        requestTable.setItems(requestList.filtered(request ->
            (searchText.isEmpty() ||
             request.getRequesterName().toLowerCase().contains(searchText) ||
             request.getBloodGroup().toLowerCase().contains(searchText) ||
             request.getHospital().toLowerCase().contains(searchText)) &&
            (status == null || status.equals("All") || status.equals(request.getStatus()))
        ));
    }

    private void setupStatusFilter() {
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> handleSearch());
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
            Stage stage = (Stage) requestTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}