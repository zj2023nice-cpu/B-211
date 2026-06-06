package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.enums.ErrorCode;
import com.grade.system.exception.ResourceNotFoundException;
import com.grade.system.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public PageResponse<Course> getCoursesPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Course> coursePage = courseRepository.findAll(pageable);
        
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
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(ErrorCode.COURSE_NOT_FOUND));
        course.setName(courseDetails.getName());
        course.setTeacherId(courseDetails.getTeacherId());
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}
