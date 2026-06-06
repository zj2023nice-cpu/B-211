package com.grade.system.repository;

import com.grade.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    List<User> findByClassName(String className);
    List<User> findByNameAndClassName(String name, String className);
}
