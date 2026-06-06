package com.grade.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.config.TestSecurityConfig;
import com.grade.system.dto.GradeImportResult;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Grade;
import com.grade.system.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GradeController.class)
@Import(TestSecurityConfig.class)
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradeService gradeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Grade testGrade;

    @BeforeEach
    void setUp() {
        testGrade = new Grade();
        testGrade.setId(1L);
        testGrade.setStudentId(1L);
        testGrade.setCourseId(1L);
        testGrade.setScore(85.0);
        testGrade.setMakeupScore(null);
        testGrade.setTerm("2023-2024-1");
    }

    @Test
    @DisplayName("测试获取所有成绩 - 不分页")
    void testGetAllGrades_NoPagination() throws Exception {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeService.getAllGrades()).thenReturn(grades);

        mockMvc.perform(get("/api/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(gradeService, times(1)).getAllGrades();
    }

    @Test
    @DisplayName("测试获取所有成绩 - 分页")
    void testGetAllGrades_WithPagination() throws Exception {
        PageResponse<Grade> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testGrade));
        pageResponse.setPageNumber(0);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);

        when(gradeService.getGradesPageWithFilter(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/grades")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(gradeService, times(1)).getGradesPageWithFilter(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("测试根据学生ID获取成绩")
    void testGetGradesByStudent() throws Exception {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeService.getGradesByStudent(1L)).thenReturn(grades);

        mockMvc.perform(get("/api/grades/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(gradeService, times(1)).getGradesByStudent(1L);
    }

    @Test
    @DisplayName("测试根据教师ID获取成绩")
    void testGetGradesByTeacher() throws Exception {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeService.getGradesByTeacher(2L)).thenReturn(grades);

        mockMvc.perform(get("/api/grades/teacher/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(gradeService, times(1)).getGradesByTeacher(2L);
    }

    @Test
    @DisplayName("测试根据班级获取成绩")
    void testGetGradesByClass() throws Exception {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeService.getGradesByClass("计算机1班")).thenReturn(grades);

        mockMvc.perform(get("/api/grades/class/计算机1班"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(gradeService, times(1)).getGradesByClass("计算机1班");
    }

    @Test
    @DisplayName("测试创建成绩")
    void testCreateGrade() throws Exception {
        when(gradeService.saveGrade(any(Grade.class))).thenReturn(testGrade);

        mockMvc.perform(post("/api/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGrade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成绩创建成功"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(gradeService, times(1)).saveGrade(any(Grade.class));
    }

    @Test
    @DisplayName("测试更新成绩")
    void testUpdateGrade() throws Exception {
        when(gradeService.updateGrade(eq(1L), any(Grade.class))).thenReturn(testGrade);

        mockMvc.perform(put("/api/grades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGrade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成绩更新成功"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(gradeService, times(1)).updateGrade(eq(1L), any(Grade.class));
    }

    @Test
    @DisplayName("测试删除成绩")
    void testDeleteGrade() throws Exception {
        doNothing().when(gradeService).deleteGrade(1L);

        mockMvc.perform(delete("/api/grades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成绩删除成功"));

        verify(gradeService, times(1)).deleteGrade(1L);
    }

    @Test
    @DisplayName("测试导入成绩 - 空文件")
    void testImportGrades_EmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "grades.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/grades/import")
                        .file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请选择要上传的文件"));

        verify(gradeService, never()).importGradesFromCsv(any());
    }

    @Test
    @DisplayName("测试导入成绩 - 非CSV文件")
    void testImportGrades_InvalidFileType() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "grades.txt", "text/plain", "test content".getBytes());

        mockMvc.perform(multipart("/api/grades/import")
                        .file(invalidFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("只支持CSV格式的文件"));

        verify(gradeService, never()).importGradesFromCsv(any());
    }

    @Test
    @DisplayName("测试导入成绩 - 成功")
    void testImportGrades_Success() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,85,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = new GradeImportResult();
        result.setTotal(1);
        result.setSuccessCount(1);
        result.setFailCount(0);

        when(gradeService.importGradesFromCsv(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/grades/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功导入 1 条成绩记录"));

        verify(gradeService, times(1)).importGradesFromCsv(any());
    }
}
