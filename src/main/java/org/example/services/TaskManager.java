package org.example.services;

import org.example.User;
import org.example.services.database.TaskDB;
import org.example.services.repo.TaskI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskManager {

    private final TaskI taskI;

    @Autowired
    public TaskManager(TaskI taskI) {
        this.taskI = taskI;
    }

    public void saveTaskToDB(String tasktext, String lang) {
        Integer userId = Math.toIntExact(User.user().getId());
        if (!taskI.existsByUserIdAndDescriptionAndLang(userId, tasktext, lang)) {
            TaskDB taskDB = new TaskDB();
            taskDB.setUserId(userId);
            taskDB.setLang(lang);
            taskDB.setDescription(tasktext);
            taskDB.setTaskName(tasktext);
            taskDB.setCompleted(0);
            taskDB.setComments("");
            taskI.save(taskDB);
        }
    }

    public void updateTask(String tasktext, String finishCode, String comments, String lang) {
        Integer userId = Math.toIntExact(User.user().getId());
        TaskDB taskDB = taskI.findTopByUserIdAndDescriptionAndLangOrderByTaskIdDesc(userId, tasktext, lang);
        if (taskDB != null) {
            taskDB.setFinishCode(finishCode);
            taskDB.setComments(comments);
            taskDB.setCompleted(1);
            taskI.save(taskDB);
        }
    }

    public List<String> getTaskByUserId(int userId, String lang) {
        if (User.user().isAuthorized()) {
            List<String> data = new ArrayList<>();
            List<TaskDB> tasks = taskI.findAllByUserIdAndLang(userId, lang);
            for (TaskDB task : tasks) {
                if (task.getTaskName() != null) {
                    data.add(task.getTaskName());
                }
            }
            return data;
        } else {
            return new ArrayList<>();
        }
    }
}