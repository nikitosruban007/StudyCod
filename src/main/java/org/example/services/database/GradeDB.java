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

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "taskName", nullable = false)
    private String taskName;

    @Column(name = "lang", nullable = false)
    private String lang;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Lob
    @Column(name = "comments", nullable = false, columnDefinition = "LONGTEXT")
    private String comments;

}
