package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping
    public ApiResponse<?> getAllCourses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<Course> coursePage = courseService.getCoursesPage(page, size);
            return ApiResponse.success(coursePage);
        } else {
            List<Course> courses = courseService.getAllCourses();
            return ApiResponse.success(courses);
        }
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
}
