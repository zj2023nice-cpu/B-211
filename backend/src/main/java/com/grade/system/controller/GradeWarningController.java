package com.grade.system.controller;

import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.GradeWarningDTO;
import com.grade.system.dto.PageResponse;
import com.grade.system.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grade-warnings")
public class GradeWarningController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ApiResponse<PageResponse<GradeWarningDTO>> getGradeWarnings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String className) {

        PageResponse<GradeWarningDTO> warningPage = gradeService.getGradeWarnings(
                term, courseId, className, page, size);
        return ApiResponse.success(warningPage);
    }
}
