package org.example.services.database;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tasks")
@Data
public class TaskDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "template", nullable = false)
    private String template;

    @Column(name = "taskName", nullable = false)
    private String taskName;

    @Column(name = "finishCode")
    private String finishCode;

    @Column(name = "comments")
    private String comments;

    @Column(name = "completed", nullable = false)
    private int completed;

    public TaskDB() {}
    
}
