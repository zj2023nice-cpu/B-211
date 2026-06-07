package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.ClassRankingDTO;
import com.grade.system.dto.GradeImportResult;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.entity.Grade;
import com.grade.system.entity.User;
import com.grade.system.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {
    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ApiResponse<?> getAllGrades(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String className) {
        if (page != null && size != null) {
            PageResponse<Grade> gradePage = gradeService.getGradesPageWithFilter(
                    null, className, term, courseId, studentName, null, page, size);
            return ApiResponse.success(gradePage);
        } else {
            List<Grade> grades = gradeService.getGradesWithFilter(
                    null, className, term, courseId, studentName, null);
            return ApiResponse.success(grades);
        }
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<?> getGradesByStudent(
            @PathVariable Long studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long courseId) {
        if (page != null && size != null) {
            PageResponse<Grade> gradePage = gradeService.getGradesPageWithFilter(
                    null, null, term, courseId, null, studentId, page, size);
            return ApiResponse.success(gradePage);
        } else {
            List<Grade> grades = gradeService.getGradesWithFilter(
                    null, null, term, courseId, null, studentId);
            return ApiResponse.success(grades);
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<?> getGradesByTeacher(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String className) {
        if (page != null && size != null) {
            PageResponse<Grade> gradePage = gradeService.getGradesPageWithFilter(
                    teacherId, className, term, courseId, studentName, null, page, size);
            return ApiResponse.success(gradePage);
        } else {
            List<Grade> grades = gradeService.getGradesWithFilter(
                    teacherId, className, term, courseId, studentName, null);
            return ApiResponse.success(grades);
        }
    }

    @GetMapping("/class/{className}")
    public ApiResponse<?> getGradesByClass(
            @PathVariable String className,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String studentName) {
        if (page != null && size != null) {
            PageResponse<Grade> gradePage = gradeService.getGradesPageWithFilter(
                    null, className, term, courseId, studentName, null, page, size);
            return ApiResponse.success(gradePage);
        } else {
            List<Grade> grades = gradeService.getGradesWithFilter(
                    null, className, term, courseId, studentName, null);
            return ApiResponse.success(grades);
        }
    }

    @GetMapping("/filter/terms")
    public ApiResponse<List<String>> getFilterTerms(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long studentId) {
        List<String> terms = gradeService.getFilterTerms(teacherId, className, studentId);
        return ApiResponse.success(terms);
    }

    @GetMapping("/filter/courses")
    public ApiResponse<List<Course>> getFilterCourses(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String term) {
        List<Course> courses = gradeService.getFilterCourses(teacherId, className, studentId, term);
        return ApiResponse.success(courses);
    }

    @GetMapping("/filter/classes")
    public ApiResponse<List<String>> getFilterClasses(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String term) {
        List<String> classes = gradeService.getFilterClasses(teacherId, studentId, term);
        return ApiResponse.success(classes);
    }

    @AuditLog(module = "成绩管理", action = "新增", description = "新增成绩记录")
    @PostMapping
    public ApiResponse<Grade> createGrade(@RequestBody Grade grade) {
        Grade createdGrade = gradeService.saveGrade(grade);
        return ApiResponse.success("成绩创建成功", createdGrade);
    }

    @AuditLog(module = "成绩管理", action = "修改", description = "修改成绩记录")
    @PutMapping("/{id}")
    public ApiResponse<Grade> updateGrade(@PathVariable Long id, @RequestBody Grade grade) {
        Grade updatedGrade = gradeService.updateGrade(id, grade);
        return ApiResponse.success("成绩更新成功", updatedGrade);
    }

    @AuditLog(module = "成绩管理", action = "删除", description = "删除成绩记录")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ApiResponse.success("成绩删除成功", null);
    }

    @AuditLog(module = "成绩管理", action = "导入", description = "批量导入成绩")
    @PostMapping("/import")
    public ApiResponse<GradeImportResult> importGrades(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            return ApiResponse.error("只支持CSV格式的文件");
        }
        
        try {
            GradeImportResult result = gradeService.importGradesFromCsv(file);
            if (result.getFailCount() > 0) {
                return ApiResponse.success("导入完成，成功 " + result.getSuccessCount() + " 条，失败 " + result.getFailCount() + " 条", result);
            }
            return ApiResponse.success("成功导入 " + result.getSuccessCount() + " 条成绩记录", result);
        } catch (Exception e) {
            return ApiResponse.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/ranking/terms")
    public ApiResponse<List<String>> getAllTerms() {
        List<String> terms = gradeService.getAllTerms();
        return ApiResponse.success(terms);
    }

    @GetMapping("/ranking/classes")
    public ApiResponse<List<String>> getRankingClasses() {
        List<String> classes = gradeService.getRankingClasses();
        return ApiResponse.success(classes);
    }

    @GetMapping("/ranking")
    public ApiResponse<List<ClassRankingDTO>> getClassRanking(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String className) {
        List<ClassRankingDTO> ranking = gradeService.getClassRanking(term, className);
        return ApiResponse.success(ranking);
    }

    @GetMapping("/available-students")
    public ApiResponse<List<User>> getAvailableStudentsForCourse(
            @RequestParam(required = false) Long courseId) {
        List<User> students = gradeService.getAvailableStudentsForCourse(courseId);
        return ApiResponse.success(students);
    }
}
