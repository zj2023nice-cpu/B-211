package com.grade.system.repository;

import com.grade.system.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByName(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
    
    @Query("SELECT c FROM Course c WHERE " +
           "(:name IS NULL OR c.name LIKE %:name%) AND " +
           "(:teacherId IS NULL OR c.teacherId = :teacherId)")
    Page<Course> findByConditions(
            @Param("name") String name,
            @Param("teacherId") Long teacherId,
            Pageable pageable);
}
