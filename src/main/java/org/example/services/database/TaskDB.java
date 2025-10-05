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

    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(name = "task_name", nullable = false, columnDefinition = "LONGTEXT")
    private String taskName;

    @Lob
    @Column(name = "finish_code", columnDefinition = "LONGTEXT")
    private String finishCode;

    @Lob
    @Column(name = "comments", columnDefinition = "LONGTEXT")
    private String comments;

    @Column(name = "completed", nullable = false)
    private int completed;

    public TaskDB() {}
    
}
