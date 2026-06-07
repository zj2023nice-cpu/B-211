package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.enums.ErrorCode;
import com.grade.system.exception.DuplicateResourceException;
import com.grade.system.exception.ResourceNotFoundException;
import com.grade.system.repository.CourseRepository;
import com.grade.system.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public PageResponse<Course> getCoursesPage(int page, int size) {
        return getCoursesPage(page, size, null, null);
    }

    public PageResponse<Course> getCoursesPage(int page, int size, String name, Long teacherId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String searchName = StringUtils.hasText(name) ? name.trim() : null;
        Page<Course> coursePage = courseRepository.findByConditions(searchName, teacherId, pageable);
        
        PageResponse<Course> response = new PageResponse<>();
        response.setContent(coursePage.getContent());
        response.setPageNumber(coursePage.getNumber());
        response.setPageSize(coursePage.getSize());
        response.setTotalElements(coursePage.getTotalElements());
        response.setTotalPages(coursePage.getTotalPages());
        response.setFirst(coursePage.isFirst());
        response.setLast(coursePage.isLast());
        return response;
    }

    public Course createCourse(Course course) {
        if (courseRepository.existsByName(course.getName())) {
            throw new DuplicateResourceException(ErrorCode.COURSE_ALREADY_EXISTS);
        }
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(ErrorCode.COURSE_NOT_FOUND));
        
        if (courseRepository.existsByNameAndIdNot(courseDetails.getName(), id)) {
            throw new DuplicateResourceException(ErrorCode.COURSE_ALREADY_EXISTS);
        }
        
        course.setName(courseDetails.getName());
        course.setTeacherId(courseDetails.getTeacherId());
        return courseRepository.save(course);
    }

    public Map<String, Object> getCourseDeletionImpact(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(ErrorCode.COURSE_NOT_FOUND));
        
        long gradeCount = gradeRepository.countByCourseId(id);
        
        Map<String, Object> impact = new HashMap<>();
        impact.put("courseId", id);
        impact.put("courseName", course.getName());
        impact.put("gradeCount", gradeCount);
        impact.put("canDelete", gradeCount == 0);
        return impact;
    }

    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(ErrorCode.COURSE_NOT_FOUND));
        
        if (gradeRepository.existsByCourseId(id)) {
            throw new DuplicateResourceException(ErrorCode.COURSE_HAS_GRADES);
        }
        
        courseRepository.deleteById(id);
    }
}
