package com.example.pifx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class JustView implements Initializable {
    public Button navBVutton;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;

    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label statusLabel;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        initializeComboBoxes();
        loadUsers();
        setupTableSelectionListener();
        initializeButtons();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void initializeComboBoxes() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "User", "Manager"));
        statusComboBox.setItems(FXCollections.observableArrayList("Active", "Inactive", "Pending"));
    }

    private void setupTableSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getFirstName() + " " + newSelection.getLastName());
                emailField.setText(newSelection.getEmail());
                roleComboBox.setValue(newSelection.getRole());
                statusComboBox.setValue(newSelection.getStatus());
            }
        });
    }

    private void initializeButtons() {
        addButton.setOnAction(e -> addUser());
        updateButton.setOnAction(e -> updateUser());
        deleteButton.setOnAction(e -> deleteUser());
        clearButton.setOnAction(e -> clearForm());
    }

    private void loadUsers() {
        userList.clear();
        String query = "SELECT * FROM users";

        if (!searchField.getText().isEmpty()) {
            query += " WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (!searchField.getText().isEmpty()) {
                String searchTerm = "%" + searchField.getText() + "%";
                pstmt.setString(1, searchTerm);
                pstmt.setString(2, searchTerm);
                pstmt.setString(3, searchTerm);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("role")
                ));
            }
            userTable.setItems(userList);
        } catch (SQLException e) {
            showStatus("Error loading users: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSearch() {
        loadUsers();
    }

    @FXML
    private void addUser() {
        if (!validateInput()) return;

        String[] names = nameField.getText().split(" ", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "";

        String sql = "INSERT INTO users (first_name, last_name, email, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, roleComboBox.getValue());
            pstmt.setString(5, statusComboBox.getValue());

            pstmt.executeUpdate();
            showStatus("User added successfully!", false);
            loadUsers();
            clearForm();
        } catch (SQLException e) {
            showStatus("Error adding user: " + e.getMessage(), true);
        }
    }

    @FXML
    private void updateUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showStatus("Please select a user to update", true);
            return;
        }
        if (!validateInput()) return;

        String[] names = nameField.getText().split(" ", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "";

        String sql = "UPDATE users SET first_name=?, last_name=?, email=?, role=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, roleComboBox.getValue());
            pstmt.setString(5, statusComboBox.getValue());
            pstmt.setInt(6, selectedUser.getId());

            pstmt.executeUpdate();
            showStatus("User updated successfully!", false);
            loadUsers();
            clearForm();
        } catch (SQLException e) {
            showStatus("Error updating user: " + e.getMessage(), true);
        }
    }

    @FXML
    private void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showStatus("Please select a user to delete", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id=?")) {

                    pstmt.setInt(1, selectedUser.getId());
                    pstmt.executeUpdate();
                    showStatus("User deleted successfully!", false);
                    loadUsers();
                    clearForm();
                } catch (SQLException e) {
                    showStatus("Error deleting user: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void clearForm() {
        nameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        userTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                roleComboBox.getValue() == null ||
                statusComboBox.getValue() == null) {

            showStatus("Please fill in all fields", true);
            return false;
        }

        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showStatus("Invalid email format", true);
            return false;
        }

        return true;
    }

    @FXML
    private void navigateToLoginPage() {
        try {
            // Load the login page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pifx/hello-view.fxml"));
            Parent root = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) userTable.getScene().getWindow();

            // Set the new scene with the login page
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Error navigating to login page: " + e.getMessage(), true);
        }
    }


    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ?
                "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;" :
                "-fx-background-color: #E8F5E9; -fx-text-fill: #388E3C;");
    }
}