package com.grade.system.service;

import com.grade.system.dto.GradeImportResult;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.entity.Grade;
import com.grade.system.entity.User;
import com.grade.system.repository.CourseRepository;
import com.grade.system.repository.GradeRepository;
import com.grade.system.repository.UserRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TermService termService;

    @InjectMocks
    private GradeService gradeService;

    private Grade testGrade;
    private Course testCourse;
    private User testStudent;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        lenient().when(termService.getEnabledTermNames()).thenReturn(Arrays.asList("2023-2024-1", "2023-2024-2"));

        testGrade = new Grade();
        testGrade.setId(1L);
        testGrade.setStudentId(1L);
        testGrade.setCourseId(1L);
        testGrade.setScore(85.0);
        testGrade.setMakeupScore(null);
        testGrade.setTerm("2023-2024-1");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("数学");
        testCourse.setTeacherId(2L);

        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("student1");
        testStudent.setPassword("123456");
        testStudent.setRole("STUDENT");
        testStudent.setName("张三");
        testStudent.setClassName("计算机1班");

        testTeacher = new User();
        testTeacher.setId(2L);
        testTeacher.setUsername("teacher1");
        testTeacher.setPassword("123456");
        testTeacher.setRole("TEACHER");
        testTeacher.setName("李老师");
    }

    @Test
    @DisplayName("测试获取所有成绩")
    void testGetAllGrades() {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeRepository.findAll()).thenReturn(grades);

        List<Grade> result = gradeService.getAllGrades();

        assertEquals(1, result.size());
        assertEquals(testGrade.getId(), result.get(0).getId());
        verify(gradeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试分页获取成绩")
    void testGetGradesPage() {
        List<Grade> grades = Arrays.asList(testGrade);
        Page<Grade> gradePage = new PageImpl<>(grades);
        when(gradeRepository.findAll(any(Pageable.class))).thenReturn(gradePage);

        PageResponse<Grade> result = gradeService.getGradesPage(0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getTotalPages());
        verify(gradeRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("测试根据学生ID获取成绩")
    void testGetGradesByStudent() {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);

        List<Grade> result = gradeService.getGradesByStudent(1L);

        assertEquals(1, result.size());
        assertEquals(testGrade.getStudentId(), result.get(0).getStudentId());
        verify(gradeRepository, times(1)).findByStudentId(1L);
    }

    @Test
    @DisplayName("测试根据教师ID获取成绩")
    void testGetGradesByTeacher() {
        List<Course> courses = Arrays.asList(testCourse);
        List<Long> courseIds = Arrays.asList(1L);
        List<Grade> grades = Arrays.asList(testGrade);

        when(courseRepository.findByTeacherId(2L)).thenReturn(courses);
        when(gradeRepository.findByCourseIdIn(courseIds)).thenReturn(grades);

        List<Grade> result = gradeService.getGradesByTeacher(2L);

        assertEquals(1, result.size());
        verify(courseRepository, times(1)).findByTeacherId(2L);
        verify(gradeRepository, times(1)).findByCourseIdIn(courseIds);
    }

    @Test
    @DisplayName("测试根据班级获取成绩")
    void testGetGradesByClass() {
        List<User> students = Arrays.asList(testStudent);
        List<Long> studentIds = Arrays.asList(1L);
        List<Grade> grades = Arrays.asList(testGrade);

        when(userRepository.findByClassName("计算机1班")).thenReturn(students);
        when(gradeRepository.findByStudentIdIn(studentIds)).thenReturn(grades);

        List<Grade> result = gradeService.getGradesByClass("计算机1班");

        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByClassName("计算机1班");
        verify(gradeRepository, times(1)).findByStudentIdIn(studentIds);
    }

    @Test
    @DisplayName("测试保存成绩 - 成功")
    void testSaveGrade_Success() {
        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(
                testGrade.getStudentId(), testGrade.getCourseId(), testGrade.getTerm()))
                .thenReturn(false);
        when(gradeRepository.save(testGrade)).thenReturn(testGrade);

        Grade result = gradeService.saveGrade(testGrade);

        assertNotNull(result);
        assertEquals(testGrade.getId(), result.getId());
        verify(gradeRepository, times(1)).existsByStudentIdAndCourseIdAndTerm(
                testGrade.getStudentId(), testGrade.getCourseId(), testGrade.getTerm());
        verify(gradeRepository, times(1)).save(testGrade);
    }

    @Test
    @DisplayName("测试保存成绩 - 重复记录")
    void testSaveGrade_Duplicate() {
        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(
                testGrade.getStudentId(), testGrade.getCourseId(), testGrade.getTerm()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gradeService.saveGrade(testGrade);
        });

        assertEquals("该学生在该学期已存在此课程的成绩记录", exception.getMessage());
        verify(gradeRepository, times(1)).existsByStudentIdAndCourseIdAndTerm(
                testGrade.getStudentId(), testGrade.getCourseId(), testGrade.getTerm());
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试更新成绩 - 成功")
    void testUpdateGrade_Success() {
        Grade updatedGrade = new Grade();
        updatedGrade.setScore(90.0);
        updatedGrade.setMakeupScore(80.0);
        updatedGrade.setTerm("2023-2024-2");

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));
        when(gradeRepository.existsByStudentIdAndCourseIdAndTermAndIdNot(
                testGrade.getStudentId(), testGrade.getCourseId(), updatedGrade.getTerm(), 1L))
                .thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);

        Grade result = gradeService.updateGrade(1L, updatedGrade);

        assertNotNull(result);
        assertEquals(90.0, result.getScore());
        assertEquals(80.0, result.getMakeupScore());
        assertEquals("2023-2024-2", result.getTerm());
        verify(gradeRepository, times(1)).findById(1L);
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试更新成绩 - 未找到")
    void testUpdateGrade_NotFound() {
        Grade updatedGrade = new Grade();
        updatedGrade.setScore(90.0);

        when(gradeRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gradeService.updateGrade(1L, updatedGrade);
        });

        assertEquals("成绩记录不存在", exception.getMessage());
        verify(gradeRepository, times(1)).findById(1L);
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试删除成绩")
    void testDeleteGrade() {
        doNothing().when(gradeRepository).deleteById(1L);

        gradeService.deleteGrade(1L);

        verify(gradeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("测试带过滤条件的分页查询")
    void testGetGradesPageWithFilter() {
        List<Grade> grades = Arrays.asList(testGrade);
        List<User> students = Arrays.asList(testStudent);

        when(gradeRepository.findAll()).thenReturn(grades);
        when(userRepository.findAll()).thenReturn(students);

        PageResponse<Grade> result = gradeService.getGradesPageWithFilter(
                null, null, "2023-2024-1", 1L, "张三", 0, 10);

        assertNotNull(result);
        verify(gradeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 成功")
    void testImportGradesFromCsv_Success() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,85,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        List<Course> courses = Arrays.asList(testCourse);
        List<User> students = Arrays.asList(testStudent);

        when(courseRepository.findByName("数学")).thenReturn(courses);
        when(userRepository.findByNameAndClassName("张三", "计算机1班")).thenReturn(students);
        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(1L, 1L, "2023-2024-1")).thenReturn(false);
        when(gradeRepository.saveAll(anyList())).thenReturn(Arrays.asList(testGrade));

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        verify(courseRepository, times(1)).findByName("数学");
        verify(userRepository, times(1)).findByNameAndClassName("张三", "计算机1班");
        verify(gradeRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 空文件")
    void testImportGradesFromCsv_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", new byte[0]);

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(0, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 无效数据")
    void testImportGradesFromCsv_InvalidData() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,abc,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("测试保存成绩 - 非启用学期")
    void testSaveGrade_InvalidManagedTerm() {
        testGrade.setTerm("2025-2026-1");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gradeService.saveGrade(testGrade));

        assertEquals("学期必须从已启用的学期中选择", exception.getMessage());
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试更新成绩 - 修改为非启用学期")
    void testUpdateGrade_InvalidManagedTerm() {
        Grade updatedGrade = new Grade();
        updatedGrade.setScore(90.0);
        updatedGrade.setMakeupScore(80.0);
        updatedGrade.setTerm("2025-2026-1");

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gradeService.updateGrade(1L, updatedGrade));

        assertEquals("学期必须从已启用的学期中选择", exception.getMessage());
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试更新成绩 - 历史学期不变时允许保存")
    void testUpdateGrade_LegacyTermUnchanged() {
        testGrade.setTerm("2020-2021-1");

        Grade updatedGrade = new Grade();
        updatedGrade.setScore(91.0);
        updatedGrade.setMakeupScore(81.0);
        updatedGrade.setTerm("2020-2021-1");

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));
        when(gradeRepository.existsByStudentIdAndCourseIdAndTermAndIdNot(1L, 1L, "2020-2021-1", 1L))
                .thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Grade result = gradeService.updateGrade(1L, updatedGrade);

        assertEquals("2020-2021-1", result.getTerm());
        assertEquals(91.0, result.getScore());
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 非启用学期")
    void testImportGradesFromCsv_InvalidManagedTerm() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2025-2026-1,数学,张三,计算机1班,85,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("学期必须从已启用的学期中选择", result.getErrors().get(0).getErrorMessage());
        verify(gradeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("测试更新成绩 - 修改学期后与其他记录重复")
    void testUpdateGrade_ChangeTermCausesDuplicate() {
        Grade anotherGrade = new Grade();
        anotherGrade.setId(2L);
        anotherGrade.setStudentId(1L);
        anotherGrade.setCourseId(1L);
        anotherGrade.setScore(75.0);
        anotherGrade.setTerm("2023-2024-2");

        Grade updatedGrade = new Grade();
        updatedGrade.setScore(90.0);
        updatedGrade.setTerm("2023-2024-2");

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));
        when(gradeRepository.existsByStudentIdAndCourseIdAndTermAndIdNot(
                1L, 1L, "2023-2024-2", 1L))
                .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gradeService.updateGrade(1L, updatedGrade);
        });

        assertEquals("该学生在该学期已存在此课程的成绩记录", exception.getMessage());
        verify(gradeRepository, times(1)).existsByStudentIdAndCourseIdAndTermAndIdNot(
                1L, 1L, "2023-2024-2", 1L);
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试更新成绩 - 相同学期不触发重复校验")
    void testUpdateGrade_SameTermDoesNotTriggerDuplicateCheckAgainstSelf() {
        Grade updatedGrade = new Grade();
        updatedGrade.setScore(95.0);
        updatedGrade.setTerm("2023-2024-1");

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));
        when(gradeRepository.existsByStudentIdAndCourseIdAndTermAndIdNot(
                1L, 1L, "2023-2024-1", 1L))
                .thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Grade result = gradeService.updateGrade(1L, updatedGrade);

        assertNotNull(result);
        assertEquals(95.0, result.getScore());
        assertEquals("2023-2024-1", result.getTerm());
        verify(gradeRepository, times(1)).existsByStudentIdAndCourseIdAndTermAndIdNot(
                1L, 1L, "2023-2024-1", 1L);
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    @DisplayName("测试保存成绩 - 重复校验是针对学生+课程+学期三元组")
    void testSaveGrade_DuplicateCheckUsesAllThreeFields() {
        Grade differentStudent = new Grade();
        differentStudent.setStudentId(99L);
        differentStudent.setCourseId(testGrade.getCourseId());
        differentStudent.setTerm(testGrade.getTerm());
        differentStudent.setScore(80.0);

        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(
                99L, testGrade.getCourseId(), testGrade.getTerm()))
                .thenReturn(false);
        when(gradeRepository.save(differentStudent)).thenReturn(differentStudent);

        Grade result = gradeService.saveGrade(differentStudent);

        assertNotNull(result);
        verify(gradeRepository, times(1)).existsByStudentIdAndCourseIdAndTerm(
                99L, testGrade.getCourseId(), testGrade.getTerm());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 文件内存在重复行")
    void testImportGradesFromCsv_DuplicateRowInFile() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,85,-\n" +
                "2023-2024-1,数学,张三,计算机1班,90,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        List<Course> courses = Arrays.asList(testCourse);
        List<User> students = Arrays.asList(testStudent);

        when(courseRepository.findByName("数学")).thenReturn(courses);
        when(userRepository.findByNameAndClassName("张三", "计算机1班")).thenReturn(students);
        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(1L, 1L, "2023-2024-1")).thenReturn(false);
        when(gradeRepository.saveAll(anyList())).thenReturn(Arrays.asList(testGrade));

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(2, result.getTotal());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("文件中已存在该学生在该学期的此课程成绩记录"));
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 课程不存在")
    void testImportGradesFromCsv_CourseNotFound() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,不存在的课程,张三,计算机1班,85,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        when(courseRepository.findByName("不存在的课程")).thenReturn(new ArrayList<>());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("不存在名为 '不存在的课程' 的课程"));
        verify(gradeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 学生不存在")
    void testImportGradesFromCsv_StudentNotFound() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,不存在的学生,计算机1班,85,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByName("数学")).thenReturn(courses);
        when(userRepository.findByNameAndClassName("不存在的学生", "计算机1班")).thenReturn(new ArrayList<>());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("不存在姓名为 '不存在的学生' 且班级为 '计算机1班' 的学生"));
        verify(gradeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 成绩超出范围")
    void testImportGradesFromCsv_ScoreOutOfRange() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,105,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("成绩必须在0-100之间"));
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 成绩为空")
    void testImportGradesFromCsv_ScoreEmpty() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,-,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("成绩不能为空"));
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 数据库中已存在重复记录")
    void testImportGradesFromCsv_DuplicateInDatabase() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,85,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        List<Course> courses = Arrays.asList(testCourse);
        List<User> students = Arrays.asList(testStudent);

        when(courseRepository.findByName("数学")).thenReturn(courses);
        when(userRepository.findByNameAndClassName("张三", "计算机1班")).thenReturn(students);
        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(1L, 1L, "2023-2024-1")).thenReturn(true);

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("数据库中已存在该学生在该学期的此课程成绩记录"));
        verify(gradeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 混合成功与失败")
    void testImportGradesFromCsv_MixedSuccessAndFailure() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,85,-\n" +
                "2023-2024-1,数学,不存在的学生,计算机1班,90,-\n" +
                "2023-2024-1,数学,张三,计算机1班,abc,-";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        List<Course> courses = Arrays.asList(testCourse);
        List<User> students = Arrays.asList(testStudent);

        when(courseRepository.findByName("数学")).thenReturn(courses);
        when(userRepository.findByNameAndClassName("张三", "计算机1班")).thenReturn(students);
        when(userRepository.findByNameAndClassName("不存在的学生", "计算机1班")).thenReturn(new ArrayList<>());
        when(gradeRepository.existsByStudentIdAndCourseIdAndTerm(1L, 1L, "2023-2024-1")).thenReturn(false);
        when(gradeRepository.saveAll(anyList())).thenReturn(Arrays.asList(testGrade));

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(3, result.getTotal());
        assertEquals(1, result.getSuccessCount());
        assertEquals(2, result.getFailCount());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 补考成绩格式错误")
    void testImportGradesFromCsv_InvalidMakeupScore() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学,张三,计算机1班,85,abc";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("补考成绩格式不正确"));
    }

    @Test
    @DisplayName("测试导入CSV成绩 - 行数据不完整")
    void testImportGradesFromCsv_IncompleteRow() throws Exception {
        String csvContent = "学期,课程名称,学生姓名,班级,成绩,补考成绩\n" +
                "2023-2024-1,数学";

        MockMultipartFile file = new MockMultipartFile(
                "file", "grades.csv", "text/csv", csvContent.getBytes());

        GradeImportResult result = gradeService.importGradesFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("行数据不完整"));
    }
}
