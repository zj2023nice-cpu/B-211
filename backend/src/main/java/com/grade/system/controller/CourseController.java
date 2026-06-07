package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.entity.CourseClass;
import com.grade.system.repository.CourseClassRepository;
import com.grade.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseClassRepository courseClassRepository;

    @GetMapping
    public ApiResponse<?> getAllCourses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long teacherId) {
        if (page != null && size != null) {
            PageResponse<Course> coursePage = courseService.getCoursesPage(page, size, name, teacherId);
            return ApiResponse.success(coursePage);
        } else {
            List<Course> courses = courseService.getAllCourses();
            return ApiResponse.success(courses);
        }
    }

    @GetMapping("/{id}/deletion-impact")
    public ApiResponse<Map<String, Object>> getCourseDeletionImpact(@PathVariable Long id) {
        Map<String, Object> impact = courseService.getCourseDeletionImpact(id);
        return ApiResponse.success(impact);
    }

    @AuditLog(module = "课程管理", action = "新增", description = "新增课程")
    @PostMapping
    public ApiResponse<Course> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return ApiResponse.success("课程创建成功", createdCourse);
    }

    @AuditLog(module = "课程管理", action = "修改", description = "修改课程信息")
    @PutMapping("/{id}")
    public ApiResponse<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Course updatedCourse = courseService.updateCourse(id, course);
        return ApiResponse.success("课程更新成功", updatedCourse);
    }

    @AuditLog(module = "课程管理", action = "删除", description = "删除课程")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.success("课程删除成功", null);
    }

    @GetMapping("/{courseId}/classes")
    public ApiResponse<List<String>> getCourseClasses(@PathVariable Long courseId) {
        List<CourseClass> courseClasses = courseClassRepository.findByCourseId(courseId);
        List<String> classNames = courseClasses.stream()
                .map(CourseClass::getClassName)
                .collect(Collectors.toList());
        return ApiResponse.success(classNames);
    }

    @AuditLog(module = "课程管理", action = "配置授课班级", description = "配置课程授课班级范围")
    @PostMapping("/{courseId}/classes")
    public ApiResponse<Void> setCourseClasses(
            @PathVariable Long courseId,
            @RequestBody List<String> classNames) {
        courseClassRepository.deleteByCourseId(courseId);
        if (classNames != null && !classNames.isEmpty()) {
            for (String className : classNames) {
                if (className != null && !className.trim().isEmpty()) {
                    CourseClass courseClass = new CourseClass();
                    courseClass.setCourseId(courseId);
                    courseClass.setClassName(className.trim());
                    courseClassRepository.save(courseClass);
                }
            }
        }
        return ApiResponse.success("授课班级配置成功", null);
    }
}
