// File: src/main/java/module-info.java
module com.dsa.visualizer {
    // Required JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Optional: Remove these if you're not using them
    // requires javafx.web;
    // requires org.controlsfx.controls;
    // requires com.dlsc.formsfx;
    // requires net.synedra.validatorfx;
    // requires org.kordamp.ikonli.javafx;
    // requires org.kordamp.bootstrapfx.core;
    // requires eu.hansolo.tilesfx;
    // requires com.almasb.fxgl.all;

    // Open packages to JavaFX for reflection (required for FXML)
    opens com.dsa.visualizer to javafx.fxml;
    opens com.dsa.visualizer.controllers to javafx.fxml;

    // Export packages
    exports com.dsa.visualizer;
    exports com.dsa.visualizer.controllers;
    exports com.dsa.visualizer.algorithms.sorting;
    exports com.dsa.visualizer.algorithms.searching;
//    exports com.dsa.visualizer.algorithms.pathfinding;
    exports com.dsa.visualizer.algorithms.graph;
    exports com.dsa.visualizer.models;
}