package com.grade.system.repository;

import com.grade.system.entity.CourseClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseClassRepository extends JpaRepository<CourseClass, Long> {
    List<CourseClass> findByCourseId(Long courseId);
    List<CourseClass> findByClassName(String className);
    void deleteByCourseId(Long courseId);
}
