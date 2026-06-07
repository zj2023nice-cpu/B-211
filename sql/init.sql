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

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `type` varchar(20) NOT NULL DEFAULT 'NOTICE' COMMENT '公告类型：IMPORTANT-重要, NOTICE-通知, INFO-消息',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-下线, 1-上线',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of announcements
-- ----------------------------
INSERT INTO `announcements` VALUES (1, '关于本学期期末考试成绩录入的通知', '请各位老师在规定时间内完成期末考试成绩的录入工作，逾期将关闭系统。', 'IMPORTANT', 1, 1, 1, NOW(), NOW());
INSERT INTO `announcements` VALUES (2, '系统维护升级公告 (v1.2.0)', '系统将于本周六凌晨进行维护升级，届时系统将暂停服务，请提前做好相关安排。', 'NOTICE', 1, 2, 1, NOW(), NOW());
INSERT INTO `announcements` VALUES (3, '欢迎新同学加入成绩管理系统', '欢迎各位新同学使用成绩管理系统，如有问题请联系管理员。', 'INFO', 1, 3, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for course_classes
-- ----------------------------
DROP TABLE IF EXISTS `course_classes`;
CREATE TABLE `course_classes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL,
  `class_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_class` (`course_id`, `class_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of course_classes
-- ----------------------------
INSERT INTO `course_classes` VALUES (1, 1, '三年二班');
INSERT INTO `course_classes` VALUES (2, 2, '三年二班');

-- ----------------------------
-- Table structure for terms
-- ----------------------------
DROP TABLE IF EXISTS `terms`;
CREATE TABLE `terms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '学期名称',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_term_name` (`name`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of terms
-- ----------------------------
INSERT INTO `terms` VALUES (1, '2023-Fall', '2023-09-01', '2024-01-15', 1, 1, NOW(), NOW());
INSERT INTO `terms` VALUES (2, '2024-Spring', '2024-02-26', '2024-07-10', 1, 2, NOW(), NOW());
INSERT INTO `terms` VALUES (3, '2024-Fall', '2024-09-02', '2025-01-17', 1, 3, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
