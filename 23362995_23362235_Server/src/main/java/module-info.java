module com.project.server {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.media; 
    requires transitive javafx.graphics;   

    opens com.project.server to javafx.fxml;
    opens controllers to javafx.fxml;
    exports com.project.server;
}

