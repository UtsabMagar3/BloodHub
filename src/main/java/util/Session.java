package util;

import javafx.stage.Stage;
import model.User;

public class Session {
    private static User currentUser;
    private static Stage stage;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setStage(Stage s) {
        stage = s;
    }

    public static Stage getStage() {
        return stage;
    }
}
