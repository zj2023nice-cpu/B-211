package com.grade.system.repository;

import com.grade.system.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long>, JpaSpecificationExecutor<Grade> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByCourseId(Long courseId);
    List<Grade> findByCourseIdIn(List<Long> courseIds);
    List<Grade> findByStudentIdIn(List<Long> studentIds);
    List<Grade> findByTerm(String term);
    List<Grade> findByTermAndStudentIdIn(String term, List<Long> studentIds);

    boolean existsByStudentIdAndCourseIdAndTerm(Long studentId, Long courseId, String term);

    boolean existsByStudentIdAndCourseIdAndTermAndIdNot(Long studentId, Long courseId, String term, Long id);
    
    boolean existsByCourseId(Long courseId);
    
    long countByCourseId(Long courseId);

    @Query("SELECT DISTINCT g.term FROM Grade g ORDER BY g.term DESC")
    List<String> findDistinctTerms();
}
