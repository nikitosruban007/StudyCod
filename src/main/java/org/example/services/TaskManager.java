package org.example.services;

import org.example.User;
import org.example.services.ai.AiRequest;
import org.example.services.database.TaskDB;
import org.example.services.repo.TaskI;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskManager {

    private final TaskI taskI;

    @Autowired
    public TaskManager(TaskI taskI) {
        this.taskI = taskI;
    }

    public void saveTaskToDB(String tasktext, String template) {
        TaskDB taskDB = new TaskDB();
        taskDB.setUserId(Math.toIntExact(User.user().getId()));
        taskDB.setDescription(tasktext);
        taskDB.setTemplate(template);
        taskDB.setTaskName(tasktext);
        taskI.save(taskDB);
    }

    public void updateTask(String tasktext, String finishCode, String comments) {
        TaskDB taskDB = taskI.findByUserIdAndDescription(Math.toIntExact(User.user().getId()), tasktext);
        if (taskDB != null) {
            taskDB.setFinishCode(finishCode);
            taskDB.setComments(comments);
            taskDB.setCompleted(1);
            taskI.save(taskDB);
        }
    }

    public List<String> getTaskByUserId(int userId) {
        if (User.user().isAuthorized()) {
            List<String> data = new ArrayList<>();
            List<TaskDB> tasks = taskI.findAllByUserId(userId);
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

    public String generateUniqueTask(double difus) {

        return AiRequest.requestToAI("Згенеруй завдання для учня на мові програмування Java, просто текст завдання, без форматування без нічого тільки текст і коротко, і ще орієнтуйся на КСЗ (коефіцієнт складності завдань), де 0 - це взагалі початківець, 1 - це бог програмування, КСЗ: " + difus);
    }


    public String generateControlTask(double difus, String topics) {
        return AiRequest.requestToAI("Згенеруй завдання контролю знань для учня на мові програмування Java за темами: " + topics + ", " +
                "просто текст завдання, без форматування без нічого тільки текст і коротко, і ще орієнтуйся на КСЗ " +
                "(коефіцієнт складності завдань), де 0 - це взагалі початківець, 1 - це бог програмування, КСЗ: " + difus);
    }
}