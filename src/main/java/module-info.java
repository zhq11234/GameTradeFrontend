module com.database.gametradefrontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.database.gametradefrontend to javafx.fxml;
    exports com.database.gametradefrontend;
    exports com.database.gametradefrontend.controller;
    exports com.database.gametradefrontend.model;
    exports com.database.gametradefrontend.service;
    exports com.database.gametradefrontend.client;
    opens com.database.gametradefrontend.controller to javafx.fxml;
}