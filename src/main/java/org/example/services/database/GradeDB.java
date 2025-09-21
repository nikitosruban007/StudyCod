package org.example.services.database;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "grades")
@Data
public class GradeDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "taskName", nullable = false)
    private String task_name;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Column(name = "comments", nullable = false)
    private String comments;

}
