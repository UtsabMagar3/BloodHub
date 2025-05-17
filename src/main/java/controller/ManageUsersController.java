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
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ManageUsersController {

    // FXML injected controls
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> bloodGroupColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private TextField searchField;

    // Observable list to store and manage users
    private ObservableList<User> userList = FXCollections.observableArrayList();

    // Initialize the controller
    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
    }

    // Set up table columns and their cell factories
    private void setupTableColumns() {
        // Bind table columns to User properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Configure actions column with edit and delete buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

                // Set button actions
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEdit(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDelete(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    // Load users from database
    private void loadUsers() {
        userList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, full_name, email, blood_group, role FROM users")) {

            while (rs.next()) {
                userList.add(new User(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("blood_group"),
                    rs.getString("role")
                ));
            }
            userTable.setItems(userList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load users");
        }
    }

    // Handle search functionality
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        userTable.setItems(userList.filtered(user ->
            user.getFullName().toLowerCase().contains(searchText) ||
            user.getEmail().toLowerCase().contains(searchText) ||
            user.getBloodGroup().toLowerCase().contains(searchText) ||
            user.getRole().toLowerCase().contains(searchText)
        ));
    }

    // Handle edit user action
    private void handleEdit(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditUser.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaveClicked()) {
                loadUsers();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open edit dialog");
        }
    }

    // Handle delete user action
    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete user: " + user.getFullName() + "?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    deleteUser(user.getId());
                    userList.remove(user);
                    showAlert("Success", "User deleted successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete user");
                }
            }
        });
    }

    // Delete user from database
    private void deleteUser(int userId) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // Start transaction

        try {
            // Delete related requests first
            PreparedStatement deleteRequests = conn.prepareStatement(
                "DELETE FROM requests WHERE user_id = ?"
            );
            deleteRequests.setInt(1, userId);
            deleteRequests.executeUpdate();

            // Delete related notifications
            PreparedStatement deleteNotifications = conn.prepareStatement(
                "DELETE FROM notifications WHERE user_id = ?"
            );
            deleteNotifications.setInt(1, userId);
            deleteNotifications.executeUpdate();

            // Delete related donations
            PreparedStatement deleteDonations = conn.prepareStatement(
                "DELETE FROM donations WHERE user_id = ?"
            );
            deleteDonations.setInt(1, userId);
            deleteDonations.executeUpdate();

            // Finally delete the user
            PreparedStatement deleteUser = conn.prepareStatement(
                "DELETE FROM users WHERE id = ?"
            );
            deleteUser.setInt(1, userId);
            deleteUser.executeUpdate();

            conn.commit(); // Commit transaction if all operations succeed
        } catch (Exception e) {
            conn.rollback(); // Rollback on error
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    // Handle back button action
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard");
        }
    }

    // Utility method to show alerts
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}