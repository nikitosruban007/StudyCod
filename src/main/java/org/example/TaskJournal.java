package org.example;

public class TaskJournal {
    private String description;
    private String finishCode;
    private String comments;

    public TaskJournal(String description, String finishCode, String comments) {
        this.description = description;
        this.finishCode = finishCode;
        this.comments = comments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFinishCode() {
        return finishCode;
    }

    public void setFinishCode(String finishCode) {
        this.finishCode = finishCode;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
