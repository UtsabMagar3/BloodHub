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
    // Table and columns for displaying blood requests
    @FXML private TableView<BloodRequest> requestTable;
    @FXML private TableColumn<BloodRequest, Integer> idColumn;
    @FXML private TableColumn<BloodRequest, String> userColumn;
    @FXML private TableColumn<BloodRequest, String> bloodGroupColumn;
    @FXML private TableColumn<BloodRequest, Integer> unitsColumn;
    @FXML private TableColumn<BloodRequest, LocalDate> requiredDateColumn;
    @FXML private TableColumn<BloodRequest, String> hospitalColumn;
    @FXML private TableColumn<BloodRequest, String> emergencyColumn;
    @FXML private TableColumn<BloodRequest, String> statusColumn;
    @FXML private TableColumn<BloodRequest, Void> actionsColumn; // Action buttons (approve/reject)
    @FXML private TextField searchField; // Search field to filter requests
    @FXML private ComboBox<String> statusFilter; // ComboBox for filtering requests by status

    // Observable list to hold all the blood requests
    private ObservableList<BloodRequest> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns(); // Set up the columns of the table
        loadRequests(); // Load blood requests from the database
        setupStatusFilter(); // Initialize the status filter dropdown
        setupTableSorting(); // Enable sorting functionality for table columns
    }

    // Set up the columns in the table
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("unitsNeeded"));
        requiredDateColumn.setCellValueFactory(new PropertyValueFactory<>("requiredDate"));
        hospitalColumn.setCellValueFactory(new PropertyValueFactory<>("hospital"));
        emergencyColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyLevel"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionsColumn(); // Set up the action buttons for approve/reject
    }

    // Set up the action column (approve/reject buttons)
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox buttons = new HBox(5, approveBtn, rejectBtn);

            {
                // Style the buttons
                approveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                // Approve button logic
                approveBtn.setOnAction(event -> {
                    BloodRequest request = getTableView().getItems().get(getIndex());
                    if (!"Pending".equals(request.getStatus())) {
                        showAlert("Error", "Can only approve pending requests", Alert.AlertType.ERROR);
                        return;
                    }
                    updateRequestStatus(request, "Approved"); // Approve the request
                });

                // Reject button logic
                rejectBtn.setOnAction(event -> {
                    BloodRequest request = getTableView().getItems().get(getIndex());
                    if (!"Pending".equals(request.getStatus())) {
                        showAlert("Error", "Can only reject pending requests", Alert.AlertType.ERROR);
                        return;
                    }
                    updateRequestStatus(request, "Rejected"); // Reject the request
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // Remove buttons if row is empty
                } else {
                    BloodRequest request = getTableView().getItems().get(getIndex());
                    if ("Pending".equals(request.getStatus())) {
                        setGraphic(buttons); // Show buttons if the request is pending
                    } else {
                        setGraphic(null); // Hide buttons if the request is not pending
                    }
                }
            }
        });
    }

    // Load the requests from the database into the table
    private void loadRequests() {
        requestList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.*, u.full_name FROM requests r " +
                             "JOIN users u ON r.user_id = u.id")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Add each request to the observable list
                requestList.add(new BloodRequest(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("blood_group"),
                        rs.getInt("units_needed"),
                        rs.getDate("required_date").toLocalDate(),
                        rs.getString("hospital"),
                        rs.getString("emergency_level"),
                        rs.getString("status")
                ));
            }
            requestTable.setItems(requestList); // Bind the request list to the table

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load requests", Alert.AlertType.ERROR);
        }
    }

    // Update the status of a request (Approve or Reject)
    private void updateRequestStatus(BloodRequest request, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if ("Approved".equals(newStatus)) {
                    // Check if there are enough units available in inventory
                    PreparedStatement checkStmt = conn.prepareStatement(
                            "SELECT total_units FROM inventory WHERE blood_group = ?");
                    checkStmt.setString(1, request.getBloodGroup());
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next() || rs.getInt("total_units") < request.getUnitsNeeded()) {
                        showAlert("Error", "Not enough units available in inventory", Alert.AlertType.ERROR);
                        return;
                    }

                    // Update the inventory after approval
                    PreparedStatement updateInventoryStmt = conn.prepareStatement(
                            "UPDATE inventory SET total_units = total_units - ? WHERE blood_group = ?");
                    updateInventoryStmt.setInt(1, request.getUnitsNeeded());
                    updateInventoryStmt.setString(2, request.getBloodGroup());
                    updateInventoryStmt.executeUpdate();
                }

                // Update the status of the request in the database
                PreparedStatement updateRequestStmt = conn.prepareStatement(
                        "UPDATE requests SET status = ? WHERE id = ?");
                updateRequestStmt.setString(1, newStatus);
                updateRequestStmt.setInt(2, request.getId());
                updateRequestStmt.executeUpdate();

                // Create a notification for the user
                PreparedStatement notifStmt = conn.prepareStatement(
                        "INSERT INTO notifications (user_id, message, is_read) " +
                                "SELECT user_id, ?, FALSE FROM requests WHERE id = ?");
                String message = String.format("Your blood request for %d units of %s has been %s.",
                        request.getUnitsNeeded(), request.getBloodGroup(), newStatus.toLowerCase());
                notifStmt.setString(1, message);
                notifStmt.setInt(2, request.getId());
                notifStmt.executeUpdate();

                conn.commit();
                request.setStatus(newStatus); // Update the status locally
                requestTable.refresh(); // Refresh the table to reflect the change
                showAlert("Success", "Request " + newStatus.toLowerCase() + " successfully", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                conn.rollback(); // Rollback if there is an error
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update request status", Alert.AlertType.ERROR);
        }
    }

    // Enable sorting on the columns
    private void setupTableSorting() {
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        requestTable.getSortOrder().add(idColumn); // Sort by ID column by default

        // Sort by required date
        requiredDateColumn.setComparator((d1, d2) -> {
            if (d1 == null || d2 == null) return 0;
            return d1.compareTo(d2);
        });

        // Sort by emergency level
        emergencyColumn.setComparator((e1, e2) -> {
            if (e1 == null || e2 == null) return 0;
            return switch (e1) {
                case "High" -> e2.equals("High") ? 0 : 1;
                case "Medium" -> e2.equals("High") ? -1 : e2.equals("Medium") ? 0 : 1;
                case "Low" -> e2.equals("Low") ? 0 : -1;
                default -> 0;
            };
        });
    }

    // Setup the status filter combobox (All, Pending, Approved, Rejected)
    private void setupStatusFilter() {
        statusFilter.getItems().addAll("All", "Pending", "Approved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> handleSearch()); // Refresh the search when filter changes
    }

    // Handle search by filtering the requests based on text and selected status
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

    // Handle back button to return to the admin panel
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
            Stage stage = (Stage) requestTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700)); // Load the AdminPanel scene
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard", Alert.AlertType.ERROR);
        }
    }

    // Show an alert dialog with a given message and type
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
