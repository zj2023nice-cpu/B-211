package com.grade.system.repository;

import com.grade.system.entity.Term;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByEnabledTrueOrderBySortOrderDescCreatedAtDesc();

    List<Term> findAllByOrderBySortOrderDescCreatedAtDesc();

    Page<Term> findAllByOrderBySortOrderDescCreatedAtDesc(Pageable pageable);

    Optional<Term> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT t.name FROM Term t WHERE t.enabled = true ORDER BY t.sortOrder DESC, t.createdAt DESC")
    List<String> findEnabledTermNames();

    @Query("SELECT t FROM Term t WHERE " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:enabled IS NULL OR t.enabled = :enabled) " +
           "ORDER BY t.sortOrder DESC, t.createdAt DESC")
    Page<Term> findByConditions(
            @Param("name") String name,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}
