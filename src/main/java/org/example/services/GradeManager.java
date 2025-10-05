package org.example.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.TaskDetails;
import org.example.User;
import org.example.services.ai.AiRequest;
import org.example.services.database.GradeDB;
import org.example.services.database.UserDB;
import org.example.services.repo.GradeI;
import org.example.services.repo.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeManager {

    public String GradeforI(String code, String ttext) {
        return AiRequest.requestToAI(
                "Оціни, наскільки код виконує завдання: якщо завдання повністю виконано — 5, частково — від 4 до 1, зовсім не виконано — 0. " +
                        "Відповідь надай тільки числом від 0 до 5. Не додавай тексту. " +
                        "Завдання: " + ttext + "\n" +
                        "Код:\n" + code
        );
    }

    public String GradeforII(String code, String ttext) {
        return AiRequest.requestToAI(
                "Оціни код за його оптимізацію: 4 — повністю оптимізований, 0 — дуже погано оптимізований. " +
                        "Відповідь — тільки число від 0 до 4. Завдання: " + ttext + "\nКод:\n" + code
        );
    }

    public String GradeforIII(String code, String ttext) {
        return AiRequest.requestToAI(
                "Оціни код за антиплагіатністю: 3 — плагіатність 0-35%, 2 — 35-70%, 1 — 70-87%, 0 — 87-100%. " +
                        "Відповідь — тільки число від 0 до 3. Завдання: " + ttext + "\nКод:\n" + code
        );
    }



    @Autowired
    private GradeI gradeI;

    @Autowired
    private UserI userI;

    private String resolveUserLang(long userId) {
        Optional<UserDB> u = userI.findById(userId);
        return u.map(UserDB::getLang).orElse("Java");
    }

    public int intermediateGrade(Long userId) {
        return (int) Math.round(
                gradeI.getGrades(userId)
                        .subList(Math.max(gradeI.getGrades(userId).size() - 5, 0), gradeI.getGrades(userId).size())
                        .stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0)
        );
    }

    public ObservableList<TaskDetails> getTaskDetailsList(int userId) {
        ObservableList<TaskDetails> taskDetailsList = FXCollections.observableArrayList();
        if (User.user().isAuthorized()) {
            String lang = resolveUserLang(User.user().getId());
            List<GradeDB> gradeDBList = gradeI.findAllByUserIdAndLang(userId, lang);
            for (GradeDB gradeDB : gradeDBList) {
                if (gradeDB != null) {
                    taskDetailsList.add(new TaskDetails(
                            gradeDB.getTaskName(),
                            gradeDB.getGrade(),
                            gradeDB.getComments()
                    ));
                }
            }
        }
        return taskDetailsList;
    }

    public String getTaskDetailsString(int userId, String taskName) {
        String lang = resolveUserLang(User.user().getId());
        GradeDB gradeDB = gradeI.findByUserIdAndTaskNameAndLang(userId, taskName, lang);
        if (gradeDB == null) {
            return "Інформація відсутня";
        }
        return "Оцінка: " + gradeDB.getGrade() + "\nКоментар: " + gradeDB.getComments();
    }

    public void saveGradeToDB(String task, String grade, String comments) {
        try {
            String lang = resolveUserLang(User.user().getId());
            GradeDB gdb = new GradeDB();
            gdb.setTaskName(task);
            gdb.setLang(lang);
            gdb.setUserId(Math.toIntExact(User.user().getId()));
            gdb.setComments(comments);
            gdb.setGrade(Integer.parseInt(grade));
            gradeI.save(gdb);
        } catch (NumberFormatException e) {
            System.err.println("Помилка перетворення оцінки в число: " + grade);
        }
    }
}