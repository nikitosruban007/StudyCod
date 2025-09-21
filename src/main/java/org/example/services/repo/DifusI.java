package org.example.services.repo;

import org.example.services.database.DifusDB;
import org.example.services.database.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DifusI extends JpaRepository<DifusDB, Long> {
    DifusDB findByUserId(Long userId);
}

