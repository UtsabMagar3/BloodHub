package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    // Database URL, including the MySQL server location and database name
    private static final String URL = "jdbc:mysql://localhost:3306/javafx";

    // MySQL database username
    private static final String USER = "root";

    // MySQL database password (ensure this is correct)
    private static final String PASSWORD = "93889"; // Make sure this matches your MySQL root password

    // Method to establish and return a connection to the database
    public static Connection getConnection() throws Exception {
        // Load the MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Return a connection to the MySQL database
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
