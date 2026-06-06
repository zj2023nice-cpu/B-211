package com.grade.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.config.TestSecurityConfig;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(TestSecurityConfig.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course testCourse1;
    private Course testCourse2;

    @BeforeEach
    void setUp() {
        testCourse1 = new Course();
        testCourse1.setId(1L);
        testCourse1.setName("数学");
        testCourse1.setTeacherId(2L);

        testCourse2 = new Course();
        testCourse2.setId(2L);
        testCourse2.setName("英语");
        testCourse2.setTeacherId(3L);
    }

    @Test
    @DisplayName("测试获取所有课程 - 不分页")
    void testGetAllCourses_NoPagination() throws Exception {
        List<Course> courses = Arrays.asList(testCourse1, testCourse2);
        when(courseService.getAllCourses()).thenReturn(courses);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(courseService, times(1)).getAllCourses();
    }

    @Test
    @DisplayName("测试获取所有课程 - 分页")
    void testGetAllCourses_WithPagination() throws Exception {
        PageResponse<Course> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testCourse1, testCourse2));
        pageResponse.setPageNumber(0);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);

        when(courseService.getCoursesPage(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/courses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(courseService, times(1)).getCoursesPage(0, 10);
    }

    @Test
    @DisplayName("测试创建课程")
    void testCreateCourse() throws Exception {
        when(courseService.createCourse(any(Course.class))).thenReturn(testCourse1);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCourse1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("课程创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("数学"));

        verify(courseService, times(1)).createCourse(any(Course.class));
    }

    @Test
    @DisplayName("测试更新课程")
    void testUpdateCourse() throws Exception {
        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(testCourse1);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCourse1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("课程更新成功"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(courseService, times(1)).updateCourse(eq(1L), any(Course.class));
    }

    @Test
    @DisplayName("测试删除课程")
    void testDeleteCourse() throws Exception {
        doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("课程删除成功"));

        verify(courseService, times(1)).deleteCourse(1L);
    }
}
