package org.example.services.repo;

import org.example.services.database.TaskDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskI extends JpaRepository<TaskDB, Long> {
    TaskDB findByUserIdAndDescription(Integer userId, String description);
    TaskDB findByUserId(Integer userId);
    List<TaskDB> findAllByUserId(Integer userId);

    // Language-aware
    List<TaskDB> findAllByUserIdAndLang(Integer userId, String lang);
    TaskDB findByUserIdAndDescriptionAndLang(Integer userId, String description, String lang);

    // For uniqueness handling
    boolean existsByUserIdAndDescriptionAndLang(Integer userId, String description, String lang);
    TaskDB findTopByUserIdAndDescriptionAndLangOrderByTaskIdDesc(Integer userId, String description, String lang);
}