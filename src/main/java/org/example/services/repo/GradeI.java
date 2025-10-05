package org.example.services.repo;

import org.example.services.database.GradeDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeI extends JpaRepository<GradeDB, Long> {
    List<GradeDB> findAllByUserId(int userId);
    GradeDB findByUserIdAndTaskName(int userId, String taskName);
    GradeDB findByUserId(int userId);

    List<GradeDB> findAllByUserIdAndLang(int userId, String lang);
    GradeDB findByUserIdAndTaskNameAndLang(int userId, String taskName, String lang);

    @Query("SELECT g.grade FROM GradeDB g WHERE g.userId = :userId")
    List<Integer> getGrades(Long userId);
}