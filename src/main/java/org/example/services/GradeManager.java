package org.example.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.TaskDetails;
import org.example.User;
import org.example.services.ai.AiRequest;
import org.example.services.database.GradeDB;
import org.example.services.repo.GradeI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeManager {

    public String GradeforI(String code, String ttext) {
        return AiRequest.requestToAI("Оціни код на його роботу, тобто він взагалі працює та завдання виконується? Тільки число, макс. бал - 5., мін 0 балів. Код: " + code + ", а також тримай завдання за яким треба оцінити код: " + ttext);
    }

    public String GradeforII(String code, String ttext) {
        return AiRequest.requestToAI("Оціни код за його оптимізацією, тобто якщо його не можна оптимізувати (макс. оптимізація) - 4 бали, повністю не оптимізований, дуже погано працює - 0 балів. Тільки число. Код: %s" + code + ", а також тримай завдання за яким треба оцінити код: " + ttext);
    }

    public String GradeforIII(String code, String ttext) {
        return AiRequest.requestToAI("Оціни код за його антиплагіатністю, тобто якщо його плагіатність від 0% до 35% - 3 бали, від 35% до 70% - 2 бали, від 70% до 87% - 1 бал, від 87% до 100% - 0 балів. Тільки число. Код: " + code + ", а також тримай завдання за яким треба оцінити код: " + ttext);
    }

    @Autowired
    private GradeI gradeI;

    public ObservableList<TaskDetails> getTaskDetailsList(int userId) {
        ObservableList<TaskDetails> taskDetailsList = FXCollections.observableArrayList();
        if (User.user().isAuthorized()) {
            List<GradeDB> gradeDBList = gradeI.findAllByUserId(userId);
            for (GradeDB gradeDB : gradeDBList) {
                if (gradeDB != null) {
                    taskDetailsList.add(new TaskDetails(
                            gradeDB.getTask_name(),
                            gradeDB.getGrade(),
                            gradeDB.getComments()
                    ));
                }
            }
        }
        return taskDetailsList;
    }

    public String getTaskDetailsString(int userId, String taskName) {
        GradeDB gradeDB = gradeI.findByUserIdAndTaskName(userId, taskName);
        if (gradeDB == null) {
            return "Інформація відсутня";
        }
        return "Оцінка: " + gradeDB.getGrade() + "\nКоментар: " + gradeDB.getComments();
    }

    public void saveGradeToDB(String task, String grade, String comments) {
        try {
            GradeDB gdb = new GradeDB();
            gdb.setTask_name(task);
            gdb.setUserId(Math.toIntExact(User.user().getId()));
            gdb.setComments(comments);
            gdb.setGrade(Integer.parseInt(grade));
            gradeI.save(gdb);
        } catch (NumberFormatException e) {
            System.err.println("Помилка перетворення оцінки в число: " + grade);
        }
    }
}