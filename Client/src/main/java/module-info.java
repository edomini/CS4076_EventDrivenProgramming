module com.project.client {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.project.client.controllers to javafx.fxml;
    exports com.project.client;
}
