package org.example.services;

import jakarta.transaction.Transactional;
import org.example.User;
import org.example.UserSession;
import org.example.services.database.DifusDB;
import org.example.services.database.UserDB;
import org.example.services.repo.DifusI;
import org.example.services.repo.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserManager {

    private final UserI userI;
    private final DifusI difusI;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserManager(UserI userI, DifusI difusI) {
        this.userI = userI;
        this.difusI = difusI;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public boolean registerUser(String username, String password, String language) {
        if (userI.findByUsername(username) != null) {
            return false;
        }

        UserDB newUser = new UserDB();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Хешируем пароль
        newUser.setLang(language);
        newUser.setDifus(0.0);
        userI.save(newUser);

        DifusDB difus = new DifusDB();
        difus.setUserId(newUser.getId());
        difus.setDifficult(0);
        difusI.save(difus);

        return true;
    }

    public void updateUser(UserDB user) {
        userI.save(user);
    }

    public static void logout(User user) {
        UserSession.eraseData();
        user.setUsername(null);
        user.setId(null);
        user.setDifus(0);
        user.setAuthorized(false);
    }

    public boolean authenticateUser(String username, String password) {
        UserDB user = userI.findByUsername(username);
        User u = User.user();

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            UserSession.saveUserData(user.getUsername(), String.valueOf(user.getId()));
            u.setUsername(username);
            u.setId(user.getId());
            u.setDifus(user.getDifus());
            u.setAuthorized(true);
            return true;
        }
        return false;
    }

    public UserDB getUserById(Long id) {
        return userI.findById(id).orElse(null);
    }
}