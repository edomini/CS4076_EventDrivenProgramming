module com.project.server {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.project.server to javafx.fxml;
    exports com.project.server;
}
