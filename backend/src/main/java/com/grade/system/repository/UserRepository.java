package com.grade.system.repository;

import com.grade.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    List<User> findByClassName(String className);
    List<User> findByNameAndClassName(String name, String className);

    @Query("SELECT DISTINCT u.className FROM User u WHERE u.className IS NOT NULL AND u.className <> '' ORDER BY u.className")
    List<String> findDistinctClassNames();
}
