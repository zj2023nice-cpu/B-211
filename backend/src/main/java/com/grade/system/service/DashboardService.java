package com.grade.system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.dto.ClassProfileDTO;
import com.grade.system.dto.DashboardStatsDTO;
import com.grade.system.dto.TeacherCourseOverviewDTO;
import com.grade.system.entity.AuditLog;
import com.grade.system.entity.Course;
import com.grade.system.entity.Grade;
import com.grade.system.entity.User;
import com.grade.system.repository.AuditLogRepository;
import com.grade.system.repository.CourseRepository;
import com.grade.system.repository.GradeRepository;
import com.grade.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public DashboardStatsDTO getDashboardStats(Long userId, String username, String role, String className) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setLastLoginTime(getLastLoginTime(username));

        List<Grade> relevantGrades = getRelevantGrades(userId, role, className);
        List<Course> relevantCourses = getRelevantCourses(userId, role);

        List<DashboardStatsDTO.CourseStatDTO> courseStats = calculateCourseStats(relevantGrades, relevantCourses);
        stats.setCourseStats(courseStats);

        stats.setTotalStudents(countStudents(role, className));
        stats.setTotalCourses(relevantCourses.size());
        stats.setTotalGrades(relevantGrades.size());
        stats.setOverallAverage(calculateOverallAverage(relevantGrades));

        return stats;
    }

    private String getLastLoginTime(String username) {
        if (username == null || username.isEmpty()) {
            return "暂无记录";
        }

        try {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<AuditLog> loginLogs = auditLogRepository.findByConditions(
                    username, "认证", "登录", true, null, null, pageable);

            List<AuditLog> logs = loginLogs.getContent();
            if (logs.size() > 1) {
                return logs.get(1).getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (logs.size() == 1) {
                return "首次登录";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "暂无记录";
    }

    private List<Grade> getRelevantGrades(Long userId, String role, String className) {
        if (userId == null) {
            return gradeRepository.findAll();
        }

        switch (role) {
            case "STUDENT":
                return gradeRepository.findByStudentId(userId);
            case "TEACHER":
                List<Course> teacherCourses = courseRepository.findByTeacherId(userId);
                if (teacherCourses.isEmpty()) {
                    return new ArrayList<>();
                }
                List<Long> courseIds = teacherCourses.stream()
                        .map(Course::getId)
                        .collect(Collectors.toList());
                return gradeRepository.findByCourseIdIn(courseIds);
            case "HEAD_TEACHER":
                if (className != null && !className.isEmpty()) {
                    List<User> classStudents = userRepository.findByClassName(className).stream()
                            .filter(u -> "STUDENT".equals(u.getRole()))
                            .collect(Collectors.toList());
                    if (classStudents.isEmpty()) {
                        return new ArrayList<>();
                    }
                    List<Long> studentIds = classStudents.stream()
                            .map(User::getId)
                            .collect(Collectors.toList());
                    return gradeRepository.findByStudentIdIn(studentIds);
                }
                return gradeRepository.findAll();
            case "ADMIN":
            default:
                return gradeRepository.findAll();
        }
    }

    private List<Course> getRelevantCourses(Long userId, String role) {
        if (userId == null) {
            return courseRepository.findAll();
        }

        if ("TEACHER".equals(role)) {
            List<Course> courses = courseRepository.findByTeacherId(userId);
            if (!courses.isEmpty()) {
                return courses;
            }
        }
        return courseRepository.findAll();
    }

    private List<DashboardStatsDTO.CourseStatDTO> calculateCourseStats(
            List<Grade> grades, List<Course> courses) {

        Map<Long, String> courseNameMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));

        Map<Long, List<Grade>> gradesByCourse = grades.stream()
                .collect(Collectors.groupingBy(Grade::getCourseId));

        List<DashboardStatsDTO.CourseStatDTO> courseStats = new ArrayList<>();

        for (Map.Entry<Long, List<Grade>> entry : gradesByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<Grade> courseGrades = entry.getValue();

            DashboardStatsDTO.CourseStatDTO stat = new DashboardStatsDTO.CourseStatDTO();
            stat.setCourseId(courseId);
            stat.setCourseName(courseNameMap.getOrDefault(courseId, "未知课程"));

            List<Double> scores = courseGrades.stream()
                    .map(g -> g.getMakeupScore() != null ? g.getMakeupScore() : g.getScore())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!scores.isEmpty()) {
                double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                stat.setAverageScore(Math.round(avg * 100.0) / 100.0);
                stat.setMaxScore(Collections.max(scores));
                stat.setMinScore(Collections.min(scores));
            } else {
                stat.setAverageScore(0.0);
                stat.setMaxScore(0.0);
                stat.setMinScore(0.0);
            }

            stat.setStudentCount(scores.size());
            long passCount = scores.stream().filter(s -> s >= 60).count();
            stat.setPassCount((int) passCount);
            stat.setFailCount(scores.size() - (int) passCount);

            courseStats.add(stat);
        }

        return courseStats;
    }

    private Integer countStudents(String role, String className) {
        if ("HEAD_TEACHER".equals(role) && className != null && !className.isEmpty()) {
            return (int) userRepository.findByClassName(className).stream()
                    .filter(u -> "STUDENT".equals(u.getRole()))
                    .count();
        }
        return (int) userRepository.findByRole("STUDENT").size();
    }

    private Double calculateOverallAverage(List<Grade> grades) {
        if (grades.isEmpty()) {
            return 0.0;
        }

        List<Double> scores = grades.stream()
                .map(g -> g.getMakeupScore() != null ? g.getMakeupScore() : g.getScore())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (scores.isEmpty()) {
            return 0.0;
        }

        double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return Math.round(avg * 100.0) / 100.0;
    }

    public List<String> getAllClassNames() {
        return userRepository.findDistinctClassNames();
    }

    public ClassProfileDTO getClassProfile(String className, String term) {
        ClassProfileDTO profile = new ClassProfileDTO();
        profile.setClassName(className);

        List<User> classStudents = userRepository.findByClassName(className).stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .collect(Collectors.toList());
        profile.setStudentCount(classStudents.size());

        if (classStudents.isEmpty()) {
            profile.setCourseCount(0);
            profile.setAverageScore(0.0);
            profile.setPassRate(0.0);
            profile.setExcellentRate(0.0);
            profile.setCourseAverages(new ArrayList<>());
            return profile;
        }

        List<Long> studentIds = classStudents.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<Grade> classGrades;
        if (term != null && !term.isEmpty()) {
            classGrades = gradeRepository.findByTermAndStudentIdIn(term, studentIds);
        } else {
            classGrades = gradeRepository.findByStudentIdIn(studentIds);
        }

        List<Double> allScores = classGrades.stream()
                .map(g -> g.getMakeupScore() != null ? g.getMakeupScore() : g.getScore())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!allScores.isEmpty()) {
            double avg = allScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            profile.setAverageScore(Math.round(avg * 100.0) / 100.0);

            long passCount = allScores.stream().filter(s -> s >= 60).count();
            profile.setPassRate(Math.round((passCount * 100.0 / allScores.size()) * 100.0) / 100.0);

            long excellentCount = allScores.stream().filter(s -> s >= 90).count();
            profile.setExcellentRate(Math.round((excellentCount * 100.0 / allScores.size()) * 100.0) / 100.0);
        } else {
            profile.setAverageScore(0.0);
            profile.setPassRate(0.0);
            profile.setExcellentRate(0.0);
        }

        List<Course> allCourses = courseRepository.findAll();
        Map<Long, String> courseNameMap = allCourses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));

        Map<Long, List<Grade>> gradesByCourse = classGrades.stream()
                .collect(Collectors.groupingBy(Grade::getCourseId));

        List<ClassProfileDTO.CourseAverageDTO> courseAverages = new ArrayList<>();
        for (Map.Entry<Long, List<Grade>> entry : gradesByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<Grade> courseGrades = entry.getValue();

            ClassProfileDTO.CourseAverageDTO courseAvg = new ClassProfileDTO.CourseAverageDTO();
            courseAvg.setCourseId(courseId);
            courseAvg.setCourseName(courseNameMap.getOrDefault(courseId, "未知课程"));

            List<Double> scores = courseGrades.stream()
                    .map(g -> g.getMakeupScore() != null ? g.getMakeupScore() : g.getScore())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!scores.isEmpty()) {
                double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                courseAvg.setAverageScore(Math.round(avg * 100.0) / 100.0);
                courseAvg.setMaxScore(Collections.max(scores));
                courseAvg.setMinScore(Collections.min(scores));
                courseAvg.setStudentCount(scores.size());
                courseAvg.setPassCount((int) scores.stream().filter(s -> s >= 60).count());
                courseAvg.setExcellentCount((int) scores.stream().filter(s -> s >= 90).count());
            } else {
                courseAvg.setAverageScore(0.0);
                courseAvg.setMaxScore(0.0);
                courseAvg.setMinScore(0.0);
                courseAvg.setStudentCount(0);
                courseAvg.setPassCount(0);
                courseAvg.setExcellentCount(0);
            }

            courseAverages.add(courseAvg);
        }

        profile.setCourseAverages(courseAverages);
        profile.setCourseCount(courseAverages.size());

        return profile;
    }

    public List<TeacherCourseOverviewDTO> getTeacherCourseOverviews() {
        List<User> teachers = userRepository.findByRole("TEACHER");
        List<User> headTeachers = userRepository.findByRole("HEAD_TEACHER");
        List<User> allTeachers = new ArrayList<>();
        allTeachers.addAll(teachers);
        allTeachers.addAll(headTeachers);

        List<Course> allCourses = courseRepository.findAll();
        List<Grade> allGrades = gradeRepository.findAll();
        List<AuditLog> allGradeLogs = auditLogRepository.findAllGradeLogs();

        Map<Long, User> userMap = allTeachers.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, List<Course>> coursesByTeacher = allCourses.stream()
                .filter(c -> c.getTeacherId() != null && userMap.containsKey(c.getTeacherId()))
                .collect(Collectors.groupingBy(Course::getTeacherId));

        Map<Long, List<Grade>> gradesByCourse = allGrades.stream()
                .collect(Collectors.groupingBy(Grade::getCourseId));
        Map<Long, LocalDateTime> latestGradeChangeByCourse = buildLatestGradeChangeByCourse(allGradeLogs);

        List<TeacherCourseOverviewDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Course>> entry : coursesByTeacher.entrySet()) {
            Long teacherId = entry.getKey();
            List<Course> teacherCourses = entry.getValue();
            User teacher = userMap.get(teacherId);

            TeacherCourseOverviewDTO overview = new TeacherCourseOverviewDTO();
            overview.setTeacherId(teacherId);
            overview.setTeacherName(teacher != null ? teacher.getName() : "未知教师");
            overview.setCourseCount(teacherCourses.size());

            List<TeacherCourseOverviewDTO.CourseProgressDTO> courseProgressList = new ArrayList<>();
            for (Course course : teacherCourses) {
                TeacherCourseOverviewDTO.CourseProgressDTO progress = new TeacherCourseOverviewDTO.CourseProgressDTO();
                progress.setCourseId(course.getId());
                progress.setCourseName(course.getName());
                progress.setProgressDescription("基于已存在成绩记录的学生计算，不代表课程应录总人数");
                progress.setLastGradeChangeDescription("仅统计可通过审计日志归因到本课程的新增/修改时间");

                List<Grade> courseGrades = gradesByCourse.getOrDefault(course.getId(), Collections.emptyList());

                int totalStudents = (int) courseGrades.stream()
                        .map(Grade::getStudentId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();

                List<Grade> enteredGrades = courseGrades.stream()
                        .filter(g -> g.getScore() != null || g.getMakeupScore() != null)
                        .collect(Collectors.toList());
                int enteredCount = (int) enteredGrades.stream()
                        .map(Grade::getStudentId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                int unscoredCount = Math.max(totalStudents - enteredCount, 0);

                progress.setTotalStudents(totalStudents);
                progress.setEnteredCount(enteredCount);
                progress.setUnscoredCount(unscoredCount);
                if (totalStudents > 0) {
                    progress.setProgressPercent(Math.round((enteredCount * 100.0 / totalStudents) * 100.0) / 100.0);
                } else {
                    progress.setProgressPercent(0.0);
                }

                List<Double> scores = enteredGrades.stream()
                        .map(g -> g.getMakeupScore() != null ? g.getMakeupScore() : g.getScore())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!scores.isEmpty()) {
                    double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    progress.setAverageScore(Math.round(avg * 100.0) / 100.0);
                    long failCount = scores.stream().filter(s -> s < 60).count();
                    progress.setFailCount((int) failCount);
                } else {
                    progress.setAverageScore(0.0);
                    progress.setFailCount(0);
                }

                LocalDateTime latestChangeTime = latestGradeChangeByCourse.get(course.getId());
                if (latestChangeTime != null) {
                    progress.setLastGradeChangeTime(latestChangeTime
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                } else {
                    progress.setLastGradeChangeTime("暂无可归因记录");
                }

                courseProgressList.add(progress);
            }
            overview.setCourseProgressList(courseProgressList);
            result.add(overview);
        }

        result.sort(Comparator.comparing(TeacherCourseOverviewDTO::getTeacherName));
        return result;
    }

    private Map<Long, LocalDateTime> buildLatestGradeChangeByCourse(List<AuditLog> gradeLogs) {
        Map<Long, LocalDateTime> latestGradeChangeByCourse = new HashMap<>();
        for (AuditLog log : gradeLogs) {
            Long courseId = extractCourseIdFromRequestParams(log.getRequestParams());
            if (courseId == null || log.getCreatedAt() == null) {
                continue;
            }
            latestGradeChangeByCourse.merge(courseId, log.getCreatedAt(),
                    (existing, current) -> current.isAfter(existing) ? current : existing);
        }
        return latestGradeChangeByCourse;
    }

    private Long extractCourseIdFromRequestParams(String requestParams) {
        if (requestParams == null || requestParams.isEmpty()) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(requestParams);
            Long courseId = extractCourseId(root);
            if (courseId != null) {
                return courseId;
            }

            if (root.isObject()) {
                Iterator<JsonNode> elements = root.elements();
                while (elements.hasNext()) {
                    courseId = extractCourseId(elements.next());
                    if (courseId != null) {
                        return courseId;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private Long extractCourseId(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.hasNonNull("courseId")) {
            JsonNode courseIdNode = node.get("courseId");
            if (courseIdNode.canConvertToLong()) {
                return courseIdNode.longValue();
            }
            if (courseIdNode.isTextual()) {
                try {
                    return Long.parseLong(courseIdNode.asText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        if (node.isObject()) {
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                Long courseId = extractCourseId(elements.next());
                if (courseId != null) {
                    return courseId;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                Long courseId = extractCourseId(item);
                if (courseId != null) {
                    return courseId;
                }
            }
        }

        return null;
    }
}
