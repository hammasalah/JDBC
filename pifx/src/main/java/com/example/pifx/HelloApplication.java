package com.example.pifx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pifx/just_view.fxml"));
            Parent root = loader.load();

            // Create scene with automatic sizing based on content
            Scene scene = new Scene(root);

            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Set stage properties
            stage.setTitle("User Management System");
            stage.setScene(scene);

//            // Center window on screen
//            stage.centerOnScreen();
//
//            // Set reasonable window size (80% of screen width/height)
//            stage.setWidth(screenBounds.getWidth() * 0.8);
//            stage.setHeight(screenBounds.getHeight() * 0.8);

            // Ensure window stays centered when resized
            stage.setOnShown(event -> {
                stage.centerOnScreen();
            });

            stage.show();

        } catch (Exception e) {
            System.err.println("Error loading FXML file:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}