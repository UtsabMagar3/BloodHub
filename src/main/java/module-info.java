module bloodhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens bloodhub to javafx.fxml;
    opens controller to javafx.fxml;

    exports bloodhub;
    exports controller;
    exports model;
    exports util;
}