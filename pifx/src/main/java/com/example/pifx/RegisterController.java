package com.example.pifx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField age;

    @FXML
    private TextField phoneNumber;

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private void onRegisterButtonClick() {
        if (validateInput()) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "INSERT INTO users (first_name, last_name, age, phone_number, email, password) VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, firstName.getText());
                pstmt.setString(2, lastName.getText());
                pstmt.setInt(3, Integer.parseInt(age.getText()));
                pstmt.setString(4, phoneNumber.getText());
                pstmt.setString(5, email.getText());
                pstmt.setString(6, password.getText());

                pstmt.executeUpdate();
                showAlert("Success", "User registered successfully!");
                navigateToHelloView();
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                showAlert("Registration Error", "Failed to register user. Please try again.");
            }
        } else {
            showAlert("Validation Error", "Please fill in all fields correctly.");
        }
    }

    @FXML
    private void onViewUsersClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pifx/users-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) firstName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registered Users");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Users View.");
        }
    }

    private boolean validateInput() {
        return !firstName.getText().isEmpty() &&
                !lastName.getText().isEmpty() &&
                !age.getText().isEmpty() &&
                !phoneNumber.getText().isEmpty() &&
                !email.getText().isEmpty() &&
                !password.getText().isEmpty();
    }

    private void navigateToHelloView() {

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}