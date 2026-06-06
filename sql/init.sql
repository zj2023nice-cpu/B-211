SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL COMMENT 'ADMIN, TEACHER, HEAD_TEACHER, STUDENT',
  `name` varchar(50) DEFAULT NULL,
  `contact` varchar(100) DEFAULT NULL,
  `class_name` varchar(50) DEFAULT NULL COMMENT 'For students and head teachers',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `teacher_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for grades
-- ----------------------------
DROP TABLE IF EXISTS `grades`;
CREATE TABLE `grades` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  `score` double DEFAULT NULL,
  `makeup_score` double DEFAULT NULL,
  `term` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', '管理员', '13800138000', NULL);
INSERT INTO `users` VALUES (2, 'teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'TEACHER', '张老师', '13900139000', NULL);
INSERT INTO `users` VALUES (3, 'student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'STUDENT', '李同学', '13700137000', '三年二班');
INSERT INTO `users` VALUES (4, 'student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'STUDENT', '王同学', '13600136000', '三年二班');
INSERT INTO `users` VALUES (5, 'teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'HEAD_TEACHER', '赵班主任', '13500135000', '三年二班');

-- ----------------------------
-- Records of courses
-- ----------------------------
INSERT INTO `courses` VALUES (1, '高等数学', 2);
INSERT INTO `courses` VALUES (2, '大学英语', 2);
INSERT INTO `courses` VALUES (3, '物理', 5);

-- ----------------------------
-- Records of grades
-- ----------------------------
INSERT INTO `grades` VALUES (1, 3, 1, 85, NULL, '2023-Fall');
INSERT INTO `grades` VALUES (2, 3, 2, 78, NULL, '2023-Fall');
INSERT INTO `grades` VALUES (3, 4, 1, 58, 62, '2023-Fall');
INSERT INTO `grades` VALUES (4, 3, 3, 90, NULL, '2023-Fall');
INSERT INTO `grades` VALUES (5, 4, 3, 88, NULL, '2023-Fall');

SET FOREIGN_KEY_CHECKS = 1;
