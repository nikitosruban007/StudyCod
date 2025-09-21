package org.example.services.repo;

import org.example.services.database.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserI extends JpaRepository<UserDB, Long> {
    UserDB findByUsernameAndPassword(String username, String password);
    UserDB findByUsername(String username);

}
