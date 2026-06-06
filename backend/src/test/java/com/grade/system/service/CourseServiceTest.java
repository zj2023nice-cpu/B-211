package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

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
    @DisplayName("测试获取所有课程")
    void testGetAllCourses() {
        List<Course> courses = Arrays.asList(testCourse1, testCourse2);
        when(courseRepository.findAll()).thenReturn(courses);

        List<Course> result = courseService.getAllCourses();

        assertEquals(2, result.size());
        assertEquals("数学", result.get(0).getName());
        assertEquals("英语", result.get(1).getName());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试分页获取课程")
    void testGetCoursesPage() {
        List<Course> courses = Arrays.asList(testCourse1, testCourse2);
        Page<Course> coursePage = new PageImpl<>(courses);
        when(courseRepository.findAll(any(Pageable.class))).thenReturn(coursePage);

        PageResponse<Course> result = courseService.getCoursesPage(0, 10);

        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        verify(courseRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("测试创建课程")
    void testCreateCourse() {
        when(courseRepository.save(testCourse1)).thenReturn(testCourse1);

        Course result = courseService.createCourse(testCourse1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("数学", result.getName());
        assertEquals(2L, result.getTeacherId());
        verify(courseRepository, times(1)).save(testCourse1);
    }

    @Test
    @DisplayName("测试更新课程 - 成功")
    void testUpdateCourse_Success() {
        Course updatedCourse = new Course();
        updatedCourse.setName("高等数学");
        updatedCourse.setTeacherId(4L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse1));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse1);

        Course result = courseService.updateCourse(1L, updatedCourse);

        assertNotNull(result);
        assertEquals("高等数学", result.getName());
        assertEquals(4L, result.getTeacherId());
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("测试更新课程 - 未找到")
    void testUpdateCourse_NotFound() {
        Course updatedCourse = new Course();
        updatedCourse.setName("高等数学");

        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.updateCourse(1L, updatedCourse);
        });

        assertEquals("课程不存在", exception.getMessage());
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("测试删除课程")
    void testDeleteCourse() {
        doNothing().when(courseRepository).deleteById(1L);

        courseService.deleteCourse(1L);

        verify(courseRepository, times(1)).deleteById(1L);
    }
}
