package org.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.util.List;

public class TaskDetails {
    private final StringProperty taskName;
    private final IntegerProperty grade;
    private final StringProperty comments;

    public TaskDetails(String taskName, int grade, String comments) {
        this.taskName = new SimpleStringProperty(taskName);
        this.grade = new SimpleIntegerProperty(grade);
        this.comments = new SimpleStringProperty(comments);
    }

    public String getTaskName() {
        return taskName.get();
    }

    public StringProperty taskNameProperty() {
        return taskName;
    }

    public int getGrade() {
        return grade.get();
    }

    public IntegerProperty gradeProperty() {
        return grade;
    }

    public String getComments() {
        return comments.get();
    }

    public StringProperty commentsProperty() {
        return comments;
    }
}
