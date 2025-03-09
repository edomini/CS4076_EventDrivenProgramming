module org.example.timetable {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    exports org.example.timetable;
    opens org.example.timetable to javafx.fxml;

    exports org.example.timetable.Controllers;
    opens org.example.timetable.Controllers to javafx.fxml;
}
