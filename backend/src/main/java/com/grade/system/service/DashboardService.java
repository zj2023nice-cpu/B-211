package com.grade.system.service;

import com.grade.system.dto.ClassProfileDTO;
import com.grade.system.dto.DashboardStatsDTO;
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
}
