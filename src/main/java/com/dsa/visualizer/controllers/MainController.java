//// File: src/main/java/com/dsa/visualizer/controllers/MainController.java
//package com.dsa.visualizer.controllers;
//import java.util.List;
//
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.layout.StackPane;
//
//public class MainController {
//
//    @FXML
//    private StackPane contentArea;
//
//    @FXML
//    private void showSorting() {
//        loadView("/fxml/sorting.fxml");
//    }
//
//    @FXML
//    private void showSearching() {
//        loadView("/fxml/searching.fxml");
//    }
//
//    @FXML
//    private void showPathfinder() {
//        loadView("/fxml/pathfinder.fxml");
//    }
//
//    @FXML
//    private void showGraph() {
//        loadView("/fxml/graph.fxml");
//    }
//
//    @FXML
//    private void showTree() {
//        loadView("/fxml/tree.fxml");
//    }
//
//    @FXML
//    private void showLinkedList() {
//        loadView("/fxml/linkedlist.fxml");
//    }
//
//    @FXML
//    private void showAbout() {
//        loadView("/fxml/about.fxml");
//    }
//
//    @FXML
//    private void showStackQueue() {
//        loadView("/fxml/stackqueue.fxml");
//    }
//
//
//    private void loadView(String fxmlPath) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            Parent view = loader.load();
//            contentArea.getChildren().clear();
//            contentArea.getChildren().add(view);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Error loading view: " + fxmlPath);
//        }
//    }
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        // ... your existing code ...
//
//        List<Button> buttons = List.of(
//                btnSorting, btnSearching, btnPathfinder, btnGraph,
//                btnTree, btnStackQueue, btnLinkedList, btnAbout
//        );
//        for (Button btn : buttons) {
//            addButtonHoverEffect(btn);
//        }
//    }
//
//    private void addButtonHoverEffect(Button btn) {
//        String originalStyle = btn.getStyle();
//
//        btn.setOnMouseEntered(e -> {
//            btn.setStyle(originalStyle + "; -fx-opacity: 0.75;");
//            btn.setScaleX(1.05);
//            btn.setScaleY(1.05);
//        });
//
//        btn.setOnMouseExited(e -> {
//            btn.setStyle(originalStyle);
//            btn.setScaleX(1.0);
//            btn.setScaleY(1.0);
//        });
//
//        btn.setOnMousePressed(e -> {
//            btn.setScaleX(0.97);
//            btn.setScaleY(0.97);
//        });
//
//        btn.setOnMouseReleased(e -> {
//            btn.setScaleX(1.05);
//            btn.setScaleY(1.05);
//        });
//    }
//}
package com.dsa.visualizer.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    @FXML private Button btnSorting;
    @FXML private Button btnSearching;
    @FXML private Button btnPathfinder;
    @FXML private Button btnGraph;
    @FXML private Button btnTree;
    @FXML private Button btnStackQueue;
    @FXML private Button btnLinkedList;
    @FXML private Button btnHeap;
    @FXML private Button btnAbout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Button> buttons = List.of(
                        btnSorting, btnSearching, btnPathfinder, btnGraph,
                btnTree, btnStackQueue, btnLinkedList,btnHeap, btnAbout
        );
        for (Button btn : buttons) {
            addButtonHoverEffect(btn);
        }
    }

    private void addButtonHoverEffect(Button btn) {
        String originalStyle = btn.getStyle();

        btn.setOnMouseEntered(e -> {

            btn.setStyle(originalStyle + "; -fx-background-color: #E74C3C;");
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(originalStyle);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });

        btn.setOnMousePressed(e -> {
            btn.setScaleX(0.97);
            btn.setScaleY(0.97);
        });

        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });
    }

    @FXML private void showSorting()    { loadView("/fxml/sorting.fxml"); }
    @FXML private void showSearching()  { loadView("/fxml/searching.fxml"); }
    @FXML private void showPathfinder() { loadView("/fxml/pathfinder.fxml"); }
    @FXML private void showGraph()      { loadView("/fxml/graph.fxml"); }
    @FXML private void showTree()       { loadView("/fxml/tree.fxml"); }
    @FXML private void showStackQueue() { loadView("/fxml/stackqueue.fxml"); }
    @FXML private void showLinkedList() { loadView("/fxml/linkedlist.fxml"); }
    @FXML private void showHeap() { loadView("/fxml/heap.fxml"); }
    @FXML private void showAbout()      { loadView("/fxml/about.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath);
        }
    }
}