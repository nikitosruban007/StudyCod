package org.example.services.repo;

import org.example.services.database.GradeDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeI extends JpaRepository<GradeDB, Long> {
    List<GradeDB> findAllByUserId(int userId);
    GradeDB findByUserIdAndTaskName(int userId, String taskName);
    GradeDB findByUserId(int userId);
}