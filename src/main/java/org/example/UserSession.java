package org.example;

import java.io.*;

public class UserSession {
    public static void saveUserData(String username, String userId) {
        long currentTime = System.currentTimeMillis();  // Время в миллисекундах

        try (PrintWriter writer = new PrintWriter(new FileWriter("user_session.txt"))) {  // Перезаписываем файл
            writer.println(username + ";" + userId + ";" + currentTime);  // Сохраняем имя пользователя, user_id и время
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadUserData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("user_session.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                return line.split(";");  // Разделяем строку на username, user_id и время
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  // Если данных нет
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

