package util;

import model.User;

public class Session {

    // Static variable to hold the current logged-in user
    private static User currentUser;

    // Getter method to retrieve the current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Setter method to set the current logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
