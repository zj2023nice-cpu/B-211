package com.grade.system.repository;

import com.grade.system.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByCourseId(Long courseId);
    List<Grade> findByCourseIdIn(List<Long> courseIds);
    List<Grade> findByStudentIdIn(List<Long> studentIds);

    boolean existsByStudentIdAndCourseIdAndTerm(Long studentId, Long courseId, String term);

    boolean existsByStudentIdAndCourseIdAndTermAndIdNot(Long studentId, Long courseId, String term, Long id);
}
