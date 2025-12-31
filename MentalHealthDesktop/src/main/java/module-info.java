module com.example.mentalhealthdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires com.google.gson;

    opens com.example.mentalhealthdesktop.model to com.google.gson, javafx.base;
    opens com.example.mentalhealthdesktop.view to com.google.gson;


    opens com.example.mentalhealthdesktop to javafx.fxml;
    exports com.example.mentalhealthdesktop;
    exports com.example.mentalhealthdesktop.controller;
    opens com.example.mentalhealthdesktop.controller to javafx.fxml;
    exports com.example.mentalhealthdesktop.model;
}