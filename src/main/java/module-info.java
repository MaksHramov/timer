module org.example.timeout {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens org.example.timeout to javafx.fxml;
    exports org.example.timeout;
    exports org.example.timeout.subject;
    exports org.example.timeout.observer;
}
