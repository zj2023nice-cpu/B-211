package com.grade.system.repository;

import com.grade.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    List<User> findByClassName(String className);
    List<User> findByNameAndClassName(String name, String className);

    @Query("SELECT DISTINCT u.className FROM User u WHERE u.className IS NOT NULL AND u.className <> '' ORDER BY u.className")
    List<String> findDistinctClassNames();

    @Query("SELECT DISTINCT u.className FROM User u WHERE u.id IN :studentIds AND u.className IS NOT NULL")
    List<String> findDistinctClassNamesByStudentIds(@Param("studentIds") List<Long> studentIds);

    List<User> findByClassNameInAndRole(List<String> classNames, String role);

    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR :username = '' OR u.username LIKE CONCAT('%', :username, '%')) AND " +
           "(:name IS NULL OR :name = '' OR u.name LIKE CONCAT('%', :name, '%')) AND " +
           "(:role IS NULL OR :role = '' OR u.role = :role) AND " +
           "(:className IS NULL OR :className = '' OR u.className LIKE CONCAT('%', :className, '%'))")
    Page<User> findByFilters(
            @Param("username") String username,
            @Param("name") String name,
            @Param("role") String role,
            @Param("className") String className,
            Pageable pageable);
}
