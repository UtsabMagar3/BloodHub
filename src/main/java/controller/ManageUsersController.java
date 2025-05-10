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

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> bloodGroupColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private TextField searchField;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

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

    private void loadUsers() {
        userList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, full_name, email, blood_group, role FROM users")) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("blood_group"),
                        rs.getString("role")
                );
                userList.add(user);
            }
            userTable.setItems(userList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<User> filteredList = userList.filtered(user ->
                user.getFullName().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText) ||
                        user.getBloodGroup().toLowerCase().contains(searchText)
        );
        userTable.setItems(filteredList);
    }

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
                loadUsers(); // Refresh the table
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open edit dialog");
        }
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete user: " + user.getFullName() + "?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                deleteUser(user);
            }
        });
    }

    private void deleteUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

            stmt.setInt(1, user.getId());
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                userList.remove(user);
                showAlert("Success", "User deleted successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete user");
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminPanel.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}