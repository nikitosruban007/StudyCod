package org.example.services.database;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "lang", nullable = false)
    private String lang;

    @Column(name = "difus", nullable = false)
    private double difus;

    @Column(name = "topicsJava", columnDefinition = "LONGTEXT")
    private String topicsJava;

    @Column(name = "topicsPython", columnDefinition = "LONGTEXT")
    private String topicsPython;

    @Transient
    private boolean isAuthorized;
}
