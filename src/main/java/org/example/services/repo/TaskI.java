package org.example.services.repo;

import org.example.services.database.TaskDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskI extends JpaRepository<TaskDB, Long> {
    TaskDB findByUserIdAndDescription(Integer userId, String description);
    TaskDB findByUserId(Integer userId);
    List<TaskDB> findAllByUserId(Integer userId); // Добавляем новый метод
}