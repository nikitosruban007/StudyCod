package org.example;

import java.util.prefs.Preferences;

public class UserSession {
    private static final String USERNAME_KEY = "username";
    private static final String USER_ID_KEY = "userId";

    public static void saveUserData(String username, String userId) {
        Preferences prefs = Preferences.userRoot().node("StudyCodApp");
        prefs.put(USERNAME_KEY, username);
        prefs.put(USER_ID_KEY, userId);
    }

    public static void loadUserData(User user) {
        Preferences prefs = Preferences.userRoot().node("StudyCodApp");
        String username = prefs.get(USERNAME_KEY, null);
        String userId = prefs.get(USER_ID_KEY, null);

        if (username != null && userId != null) {
            user.setUsername(username);
            user.setId(Long.parseLong(userId));
            user.setAuthorized(true);
        }
    }

    public static void eraseData() {
        Preferences prefs = Preferences.userRoot().node("StudyCodApp");
        prefs.remove(USERNAME_KEY);
        prefs.remove(USER_ID_KEY);
    }
}