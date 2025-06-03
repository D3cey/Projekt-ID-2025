package org.example.util;

import org.example.model.User;

public class CurrentUserSession {
    private static User loggedInUser = null;

    public static void loginUser(User user) {
        loggedInUser = user;
    }

    public static void logoutUser() {
        loggedInUser = null;
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static boolean isAdmin() {
        return loggedInUser != null && loggedInUser.isAdmin();
    }
}