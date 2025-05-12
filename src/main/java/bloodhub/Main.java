package bloodhub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main class that starts the JavaFX application
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the login screen layout from the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));

        // Create a scene using the loaded layout
        Scene scene = new Scene(loader.load());

        // Set the title of the application window
        stage.setTitle("BloodHub");

        // Set the scene to the stage (window)
        stage.setScene(scene);

        // Display the stage to the user
        stage.show();
    }

    // Entry point of the application
    public static void main(String[] args) {
        launch(args);
    }
}
