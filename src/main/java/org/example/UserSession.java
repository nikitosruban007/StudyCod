package org.example;

import java.io.*;

public class UserSession {
    public static void saveUserData(String username, String userId) {
        long currentTime = System.currentTimeMillis();

        try (PrintWriter writer = new PrintWriter(new FileWriter("user_session.txt"))) {
            writer.println(username + ";" + userId + ";" + currentTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadUserData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("user_session.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                return line.split(";");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void eraseData() {
        File file = new File("user_session.txt");
        if (file.exists()) {
            if (file.delete()) {
            } else {
            }
        } else {
        }
    }

}

