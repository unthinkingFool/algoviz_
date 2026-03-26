package com.dsa.visualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);

            primaryStage.setTitle("AlgoViz - Interactive Algorithm Visualization");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading main.fxml: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}