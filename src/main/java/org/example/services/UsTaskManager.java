package org.example.services;

import org.example.services.database.UsTaskDB;
import org.example.services.repo.UsTaskI;
import org.springframework.beans.factory.annotation.Autowired;

public class UsTaskManager {
    UsTaskI usTaskI;

    public void getTaskNum(int userId) {
        UsTaskDB usTaskDB = usTaskI.getByUserId(Long.valueOf(userId));
    }
}
