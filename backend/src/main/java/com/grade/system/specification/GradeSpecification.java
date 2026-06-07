package com.grade.system.specification;

import com.grade.system.entity.Grade;
import com.grade.system.entity.User;
import com.grade.system.entity.Course;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class GradeSpecification {

    public static Specification<Grade> withFilters(
            Long teacherId,
            String className,
            String term,
            Long courseId,
            String studentName,
            Long studentId) {

        return (Root<Grade> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.isEmpty()) {
                predicates.add(cb.equal(root.get("term"), term));
            }

            if (courseId != null) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }

            if (studentId != null) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }

            if (teacherId != null) {
                Subquery<Long> courseSubquery = query.subquery(Long.class);
                Root<Course> courseRoot = courseSubquery.from(Course.class);
                courseSubquery.select(courseRoot.get("id"))
                        .where(cb.equal(courseRoot.get("teacherId"), teacherId));
                predicates.add(root.get("courseId").in(courseSubquery));
            }

            if (className != null && !className.isEmpty()) {
                Subquery<Long> studentSubquery = query.subquery(Long.class);
                Root<User> userRoot = studentSubquery.from(User.class);
                studentSubquery.select(userRoot.get("id"))
                        .where(
                                cb.equal(userRoot.get("role"), "STUDENT"),
                                cb.equal(userRoot.get("className"), className)
                        );
                predicates.add(root.get("studentId").in(studentSubquery));
            }

            if (studentName != null && !studentName.isEmpty()) {
                Subquery<Long> studentSubquery = query.subquery(Long.class);
                Root<User> userRoot = studentSubquery.from(User.class);
                studentSubquery.select(userRoot.get("id"))
                        .where(
                                cb.equal(userRoot.get("role"), "STUDENT"),
                                cb.like(cb.lower(userRoot.get("name")), "%" + studentName.toLowerCase() + "%")
                        );
                predicates.add(root.get("studentId").in(studentSubquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
