package org.example.services.database;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tasks")
@Data
public class TaskDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "lang", nullable = false)
    private String lang;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "finish_code")
    private String finishCode;

    @Lob
    @Column(name = "comments", columnDefinition = "LONGTEXT")
    private String comments;

    @Column(name = "completed", nullable = false)
    private int completed;

    public TaskDB() {}
    
}
