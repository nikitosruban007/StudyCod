package org.example.services.repo;

import org.example.services.database.UsTaskDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsTaskI extends JpaRepository<UsTaskDB, Long> {
    UsTaskDB getByUserId(Long userId);
}