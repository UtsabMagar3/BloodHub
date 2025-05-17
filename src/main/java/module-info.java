module bloodhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens bloodhub to javafx.fxml;
    opens controller to javafx.fxml;

    exports bloodhub;
    exports controller;
    exports model;
}