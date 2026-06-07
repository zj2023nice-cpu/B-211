package com.grade.system.repository;

import com.grade.system.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long>, JpaSpecificationExecutor<Grade> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByCourseId(Long courseId);
    List<Grade> findByCourseIdIn(List<Long> courseIds);
    List<Grade> findByStudentIdIn(List<Long> studentIds);
    List<Grade> findByTerm(String term);
    List<Grade> findByTermAndStudentIdIn(String term, List<Long> studentIds);

    List<Grade> findByCreatedAtAfter(LocalDateTime dateTime);
    List<Grade> findByStudentIdAndCreatedAtAfter(Long studentId, LocalDateTime dateTime);
    List<Grade> findByCourseIdInAndCreatedAtAfter(List<Long> courseIds, LocalDateTime dateTime);
    List<Grade> findByStudentIdInAndCreatedAtAfter(List<Long> studentIds, LocalDateTime dateTime);

    boolean existsByStudentIdAndCourseIdAndTerm(Long studentId, Long courseId, String term);

    boolean existsByStudentIdAndCourseIdAndTermAndIdNot(Long studentId, Long courseId, String term, Long id);
    
    boolean existsByCourseId(Long courseId);
    
    long countByCourseId(Long courseId);

    @Query("SELECT DISTINCT g.term FROM Grade g ORDER BY g.term DESC")
    List<String> findDistinctTerms();

    @Query("SELECT DISTINCT TRIM(g.term) FROM Grade g WHERE g.term IS NOT NULL AND TRIM(g.term) <> '' ORDER BY TRIM(g.term) DESC")
    List<String> findDistinctNormalizedTerms();
}
