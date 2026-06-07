package com.grade.system.service;

import com.grade.system.dto.ClassRankingDTO;
import com.grade.system.dto.GradeImportResult;
import com.grade.system.dto.GradeWarningDTO;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Course;
import com.grade.system.entity.Grade;
import com.grade.system.entity.User;
import com.grade.system.enums.ErrorCode;
import com.grade.system.exception.BusinessException;
import com.grade.system.exception.DuplicateResourceException;
import com.grade.system.exception.ResourceNotFoundException;
import com.grade.system.repository.CourseRepository;
import com.grade.system.repository.GradeRepository;
import com.grade.system.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TermService termService;

    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    public PageResponse<Grade> getGradesPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Grade> gradePage = gradeRepository.findAll(pageable);
        
        PageResponse<Grade> response = new PageResponse<>();
        response.setContent(gradePage.getContent());
        response.setPageNumber(gradePage.getNumber());
        response.setPageSize(gradePage.getSize());
        response.setTotalElements(gradePage.getTotalElements());
        response.setTotalPages(gradePage.getTotalPages());
        response.setFirst(gradePage.isFirst());
        response.setLast(gradePage.isLast());
        return response;
    }

    public PageResponse<Grade> getGradesPageWithFilter(
            Long teacherId,
            String className,
            String term,
            Long courseId,
            String studentName,
            int page,
            int size) {
        
        List<Grade> allGrades = new ArrayList<>();
        
        if (teacherId != null) {
            allGrades = getGradesByTeacher(teacherId);
        } else if (className != null && !className.isEmpty()) {
            allGrades = getGradesByClass(className);
        } else {
            allGrades = gradeRepository.findAll();
        }
        
        List<Grade> filteredGrades = allGrades;
        
        if (term != null && !term.isEmpty()) {
            final String finalTerm = term;
            filteredGrades = filteredGrades.stream()
                    .filter(g -> finalTerm.equals(g.getTerm()))
                    .collect(Collectors.toList());
        }
        
        if (courseId != null) {
            filteredGrades = filteredGrades.stream()
                    .filter(g -> courseId.equals(g.getCourseId()))
                    .collect(Collectors.toList());
        }
        
        if (studentName != null && !studentName.isEmpty()) {
            final String finalStudentName = studentName.toLowerCase();
            List<User> students = userRepository.findAll().stream()
                    .filter(u -> "STUDENT".equals(u.getRole()))
                    .filter(u -> u.getName() != null && u.getName().toLowerCase().contains(finalStudentName))
                    .collect(Collectors.toList());
            List<Long> matchedStudentIds = students.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            filteredGrades = filteredGrades.stream()
                    .filter(g -> matchedStudentIds.contains(g.getStudentId()))
                    .collect(Collectors.toList());
        }
        
        return createPageResponse(filteredGrades, page, size);
    }

    private PageResponse<Grade> createPageResponse(List<Grade> allGrades, int page, int size) {
        allGrades.sort(Comparator.comparing(Grade::getId).reversed());
        
        int totalElements = allGrades.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<Grade> content = start < totalElements ? allGrades.subList(start, end) : new ArrayList<>();
        
        PageResponse<Grade> response = new PageResponse<>();
        response.setContent(content);
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setFirst(page == 0);
        response.setLast(page >= totalPages - 1);
        return response;
    }

    public List<Grade> getGradesByStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    public PageResponse<Grade> getGradesByStudentPage(Long studentId, int page, int size) {
        List<Grade> allGrades = gradeRepository.findByStudentId(studentId);
        return createPageResponse(allGrades, page, size);
    }

    public List<Grade> getGradesByTeacher(Long teacherId) {
        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        return gradeRepository.findByCourseIdIn(courseIds);
    }

    public PageResponse<Grade> getGradesByTeacherPage(Long teacherId, int page, int size) {
        List<Grade> allGrades = getGradesByTeacher(teacherId);
        return createPageResponse(allGrades, page, size);
    }

    public List<Grade> getGradesByClass(String className) {
        List<User> students = userRepository.findByClassName(className);
        List<Long> studentIds = students.stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .map(User::getId)
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return gradeRepository.findByStudentIdIn(studentIds);
    }

    public PageResponse<Grade> getGradesByClassPage(String className, int page, int size) {
        List<Grade> allGrades = getGradesByClass(className);
        return createPageResponse(allGrades, page, size);
    }

    public Grade saveGrade(Grade grade) {
        String normalizedTerm = normalizeTerm(grade.getTerm());
        validateManagedTerm(normalizedTerm);
        grade.setTerm(normalizedTerm);
        if (gradeRepository.existsByStudentIdAndCourseIdAndTerm(
                grade.getStudentId(), grade.getCourseId(), normalizedTerm)) {
            throw new DuplicateResourceException(ErrorCode.GRADE_ALREADY_EXISTS);
        }
        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Long id, Grade gradeDetails) {
        Grade grade = gradeRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException(ErrorCode.GRADE_NOT_FOUND));

        String originalTerm = normalizeTerm(grade.getTerm());
        String targetTerm = normalizeTerm(gradeDetails.getTerm());
        if (!java.util.Objects.equals(originalTerm, targetTerm)) {
            validateManagedTerm(targetTerm);
        }

        if (gradeRepository.existsByStudentIdAndCourseIdAndTermAndIdNot(
                grade.getStudentId(), grade.getCourseId(), targetTerm, id)) {
            throw new DuplicateResourceException(ErrorCode.GRADE_ALREADY_EXISTS);
        }

        grade.setScore(gradeDetails.getScore());
        grade.setMakeupScore(gradeDetails.getMakeupScore());
        grade.setTerm(targetTerm);
        return gradeRepository.save(grade);
    }

    public void deleteGrade(Long id) {
        gradeRepository.deleteById(id);
    }

    @Transactional
    public GradeImportResult importGradesFromCsv(MultipartFile file) {
        GradeImportResult result = new GradeImportResult();
        List<Grade> gradesToSave = new ArrayList<>();
        java.util.Set<String> processedKeys = new java.util.HashSet<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> allRows = reader.readAll();
            
            if (allRows.isEmpty()) {
                result.setTotal(0);
                result.setSuccessCount(0);
                result.setFailCount(0);
                result.getErrors().add(new GradeImportResult.ImportError(1, "", "", "", "CSV文件为空"));
                return result;
            }
            
            String[] header = allRows.get(0);
            List<String[]> dataRows = allRows.subList(1, allRows.size());
            
            result.setTotal(dataRows.size());
            
            for (int i = 0; i < dataRows.size(); i++) {
                String[] row = dataRows.get(i);
                int rowNumber = i + 2;
                
                try {
                    if (row.length < 5) {
                        throw new IllegalArgumentException("行数据不完整，至少需要5列");
                    }
                    
                    String term = normalizeTerm(row[0]);
                    String courseName = row[1].trim();
                    String studentName = row[2].trim();
                    String className = row[3].trim();
                    String scoreStr = row[4].trim();
                    String makeupScoreStr = row.length > 5 ? row[5].trim() : "";

                    validateManagedTerm(term);
                    if (courseName.isEmpty()) {
                        throw new IllegalArgumentException("课程名称不能为空");
                    }
                    if (studentName.isEmpty()) {
                        throw new IllegalArgumentException("学生姓名不能为空");
                    }
                    if (scoreStr.isEmpty() || "-".equals(scoreStr)) {
                        throw new IllegalArgumentException("成绩不能为空");
                    }
                    
                    Double score;
                    try {
                        score = Double.parseDouble(scoreStr);
                        if (score < 0 || score > 100) {
                            throw new IllegalArgumentException("成绩必须在0-100之间");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("成绩格式不正确，必须是数字");
                    }
                    
                    Double makeupScore = null;
                    if (!makeupScoreStr.isEmpty() && !"-".equals(makeupScoreStr)) {
                        try {
                            makeupScore = Double.parseDouble(makeupScoreStr);
                            if (makeupScore < 0 || makeupScore > 100) {
                                throw new IllegalArgumentException("补考成绩必须在0-100之间");
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("补考成绩格式不正确，必须是数字");
                        }
                    }
                    
                    List<Course> courses = courseRepository.findByName(courseName);
                    if (courses.isEmpty()) {
                        throw new IllegalArgumentException("不存在名为 '" + courseName + "' 的课程");
                    }
                    if (courses.size() > 1) {
                        throw new IllegalArgumentException("存在多个名为 '" + courseName + "' 的课程，请使用课程ID");
                    }
                    Course course = courses.get(0);
                    
                    List<User> students = userRepository.findByNameAndClassName(studentName, className);
                    if (students.isEmpty()) {
                        throw new IllegalArgumentException("不存在姓名为 '" + studentName + "' 且班级为 '" + className + "' 的学生");
                    }
                    if (students.size() > 1) {
                        throw new IllegalArgumentException("存在多个姓名为 '" + studentName + "' 且班级为 '" + className + "' 的学生");
                    }
                    User student = students.get(0);
                    
                    String uniqueKey = student.getId() + "_" + course.getId() + "_" + term;
                    
                    if (processedKeys.contains(uniqueKey)) {
                        throw new IllegalArgumentException("文件中已存在该学生在该学期的此课程成绩记录（重复行）");
                    }
                    
                    if (gradeRepository.existsByStudentIdAndCourseIdAndTerm(student.getId(), course.getId(), term)) {
                        throw new IllegalArgumentException("数据库中已存在该学生在该学期的此课程成绩记录");
                    }
                    
                    processedKeys.add(uniqueKey);
                    
                    Grade grade = new Grade();
                    grade.setStudentId(student.getId());
                    grade.setCourseId(course.getId());
                    grade.setScore(score);
                    grade.setMakeupScore(makeupScore);
                    grade.setTerm(term);
                    
                    gradesToSave.add(grade);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    String studentNameVal = row.length > 2 ? row[2].trim() : "";
                    String courseNameVal = row.length > 1 ? row[1].trim() : "";
                    String termVal = row.length > 0 ? row[0].trim() : "";
                    result.getErrors().add(new GradeImportResult.ImportError(
                        rowNumber, studentNameVal, courseNameVal, termVal, e.getMessage()
                    ));
                }
            }
            
            if (!gradesToSave.isEmpty()) {
                gradeRepository.saveAll(gradesToSave);
            }
            
        } catch (IOException | CsvException e) {
            result.setFailCount(result.getFailCount() + 1);
            result.getErrors().add(new GradeImportResult.ImportError(
                0, "", "", "", "文件读取失败：" + e.getMessage()
            ));
        }
        
        return result;
    }

    public PageResponse<GradeWarningDTO> getGradeWarnings(
            String term,
            Long courseId,
            String className,
            Long teacherId,
            int page,
            int size) {

        List<GradeWarningDTO> allWarnings = new ArrayList<>();

        List<Grade> allGrades;
        if (teacherId != null) {
            List<Course> teacherCourses = courseRepository.findByTeacherId(teacherId);
            List<Long> teacherCourseIds = teacherCourses.stream().map(Course::getId).collect(Collectors.toList());
            if (teacherCourseIds.isEmpty()) {
                allGrades = new ArrayList<>();
            } else {
                allGrades = gradeRepository.findByCourseIdIn(teacherCourseIds);
            }
        } else {
            allGrades = gradeRepository.findAll();
        }

        for (Grade grade : allGrades) {
            if (term != null && !term.isEmpty() && !term.equals(grade.getTerm())) {
                continue;
            }
            if (courseId != null && !courseId.equals(grade.getCourseId())) {
                continue;
            }

            User student = userRepository.findById(grade.getStudentId()).orElse(null);
            if (student == null) {
                continue;
            }

            if (className != null && !className.isEmpty() && !className.equals(student.getClassName())) {
                continue;
            }

            Course course = courseRepository.findById(grade.getCourseId()).orElse(null);
            if (course == null) {
                continue;
            }

            String warningLevel = getWarningLevel(grade.getScore(), grade.getMakeupScore());
            if (warningLevel != null) {
                GradeWarningDTO dto = new GradeWarningDTO();
                dto.setStudentName(student.getName());
                dto.setClassName(student.getClassName());
                dto.setCourseName(course.getName());
                dto.setScore(grade.getScore());
                dto.setMakeupScore(grade.getMakeupScore());
                dto.setTerm(grade.getTerm());
                dto.setWarningLevel(warningLevel);
                allWarnings.add(dto);
            }
        }

        allWarnings.sort(Comparator.comparing(GradeWarningDTO::getWarningLevel)
                .thenComparing(GradeWarningDTO::getScore, Comparator.nullsLast(Comparator.naturalOrder())));

        return createWarningPageResponse(allWarnings, page, size);
    }

    private String getWarningLevel(Double score, Double makeupScore) {
        if (score == null) {
            return null;
        }

        if (score < 60) {
            if (makeupScore != null && makeupScore >= 60) {
                return "MAKEUP_PASS";
            }
            return "FAIL";
        }

        if (score >= 60 && score < 70) {
            return "BORDERLINE";
        }

        return null;
    }

    private PageResponse<GradeWarningDTO> createWarningPageResponse(List<GradeWarningDTO> allItems, int page, int size) {
        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<GradeWarningDTO> content = start < totalElements ? allItems.subList(start, end) : new ArrayList<>();

        PageResponse<GradeWarningDTO> response = new PageResponse<>();
        response.setContent(content);
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setFirst(page == 0);
        response.setLast(page >= totalPages - 1);
        return response;
    }

    private String normalizeTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            throw new BusinessException("学期不能为空");
        }
        return term.trim();
    }

    private void validateManagedTerm(String term) {
        String normalizedTerm = normalizeTerm(term);
        List<String> enabledTerms = termService.getEnabledTermNames();
        if (!enabledTerms.contains(normalizedTerm)) {
            throw new BusinessException("学期必须从已启用的学期中选择");
        }
    }

    public List<String> getAllTerms() {
        return termService.getAllTermNames();
    }

    public List<String> getRankingClasses() {
        List<User> students = userRepository.findByRole("STUDENT");
        List<Long> studentIds = students.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Grade> grades = gradeRepository.findByStudentIdIn(studentIds);
        List<Long> studentsWithGrades = grades.stream()
                .map(Grade::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        return students.stream()
                .filter(s -> studentsWithGrades.contains(s.getId()))
                .map(User::getClassName)
                .filter(className -> className != null && !className.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<ClassRankingDTO> getClassRanking(String term, String className) {
        List<Grade> grades;
        if (term != null && !term.isEmpty()) {
            grades = gradeRepository.findByTerm(term);
        } else {
            grades = gradeRepository.findAll();
        }

        List<User> students = userRepository.findByRole("STUDENT");
        if (className != null && !className.isEmpty()) {
            students = students.stream()
                    .filter(s -> className.equals(s.getClassName()))
                    .collect(Collectors.toList());
        }

        List<Long> studentIds = students.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        java.util.Map<Long, List<Grade>> studentGradesMap = grades.stream()
                .filter(g -> studentIds.contains(g.getStudentId()))
                .collect(Collectors.groupingBy(Grade::getStudentId));

        List<ClassRankingDTO> rankingList = new ArrayList<>();
        for (User student : students) {
            List<Grade> studentGrades = studentGradesMap.getOrDefault(student.getId(), new ArrayList<>());

            double totalScore = 0.0;
            int courseCount = 0;

            for (Grade grade : studentGrades) {
                Double effectiveScore = grade.getMakeupScore() != null ? grade.getMakeupScore() : grade.getScore();
                if (effectiveScore != null) {
                    totalScore += effectiveScore;
                    courseCount++;
                }
            }

            if (courseCount > 0) {
                ClassRankingDTO dto = new ClassRankingDTO();
                dto.setStudentId(student.getId());
                dto.setStudentName(student.getName());
                dto.setClassName(student.getClassName());
                dto.setTotalScore(Math.round(totalScore * 100.0) / 100.0);
                dto.setAverageScore(Math.round((totalScore / courseCount) * 100.0) / 100.0);
                dto.setCourseCount(courseCount);
                rankingList.add(dto);
            }
        }

        rankingList.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

        int currentRank = 1;
        for (int i = 0; i < rankingList.size(); i++) {
            if (i > 0) {
                if (rankingList.get(i).getTotalScore().equals(rankingList.get(i - 1).getTotalScore())) {
                    rankingList.get(i).setRank(rankingList.get(i - 1).getRank());
                    rankingList.get(i).setIsTied(true);
                    rankingList.get(i - 1).setIsTied(true);
                } else {
                    currentRank = i + 1;
                    rankingList.get(i).setRank(currentRank);
                    rankingList.get(i).setIsTied(false);
                }
            } else {
                rankingList.get(i).setRank(currentRank);
                rankingList.get(i).setIsTied(false);
            }
        }

        return rankingList;
    }
}
