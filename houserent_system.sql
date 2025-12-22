/*
 Navicat Premium Data Transfer

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 80043
 Source Host           : localhost:3306
 Source Schema         : houserent_system

 Target Server Type    : MySQL
 Target Server Version : 80043
 File Encoding         : 65001

 Date: 22/12/2025 21:19:51
*/


-- 1. 创建数据库（如果不存在），指定默认字符集和排序规则（推荐utf8mb4兼容emoji）
CREATE DATABASE IF NOT EXISTS `houserent_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 使用该数据库（切换到目标数据库）
USE `houserent_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_permissions
-- ----------------------------
DROP TABLE IF EXISTS `admin_permissions`;
CREATE TABLE `admin_permissions`  (
  `permission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限配置ID',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `max_house_audit` int NULL DEFAULT 50 COMMENT '每日房源审核上限',
  `max_user_audit` int NULL DEFAULT 30 COMMENT '每日用户审核上限',
  `max_contract_audit` int NULL DEFAULT 20 COMMENT '每日合同审核上限',
  `can_manage_facilities` tinyint(1) NULL DEFAULT 1 COMMENT '是否可管理配套设施',
  `can_handle_reports` tinyint(1) NULL DEFAULT 1 COMMENT '是否可处理举报',
  `can_handle_violations` tinyint(1) NULL DEFAULT 1 COMMENT '是否可处理违规',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态: active/inactive',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`permission_id`) USING BTREE,
  UNIQUE INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员权限配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `admin_type` enum('platform','community') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员类型',
  `community_id` bigint NULL DEFAULT NULL COMMENT '负责的小区ID(仅小区管理员)',
  `permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '权限配置(JSON格式)',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_community`(`community_id` ASC) USING BTREE,
  CONSTRAINT `admins_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `admins_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (1, 5, 'community', NULL, '{\"house_audit\": true, \"user_verify\": true, \"contract_audit\": true}', 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `admins` VALUES (2, 6, 'platform', NULL, '{\"system_manage\": true, \"global_audit\": true, \"data_stats\": true}', 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `admins` VALUES (3, 5, 'community', NULL, '{\"house_audit\": true, \"user_verify\": true, \"contract_audit\": true}', 'active', '2025-12-18 01:56:36', '2025-12-18 01:56:36');
INSERT INTO `admins` VALUES (4, 6, 'platform', NULL, '{\"system_manage\": true, \"global_audit\": true, \"data_stats\": true}', 'active', '2025-12-18 01:56:36', '2025-12-18 01:56:36');
INSERT INTO `admins` VALUES (5, 5, 'community', NULL, '{\"house_audit\": true, \"user_verify\": true, \"contract_audit\": true}', 'active', '2025-12-18 01:56:57', '2025-12-18 01:56:57');
INSERT INTO `admins` VALUES (6, 6, 'platform', NULL, '{\"system_manage\": true, \"global_audit\": true, \"data_stats\": true}', 'active', '2025-12-18 01:56:57', '2025-12-18 01:56:57');
INSERT INTO `admins` VALUES (7, 5, 'community', NULL, '{\"house_audit\": true, \"user_verify\": true, \"contract_audit\": true}', 'active', '2025-12-18 01:57:07', '2025-12-18 01:57:07');
INSERT INTO `admins` VALUES (8, 6, 'platform', NULL, '{\"system_manage\": true, \"global_audit\": true, \"data_stats\": true}', 'active', '2025-12-18 01:57:07', '2025-12-18 01:57:07');
INSERT INTO `admins` VALUES (12, 18, 'platform', NULL, '{}', 'active', '2025-12-18 02:56:20', '2025-12-18 02:56:20');
INSERT INTO `admins` VALUES (13, 19, 'community', 1, '{}', 'active', '2025-12-18 03:00:17', '2025-12-18 03:00:17');

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID(由两个用户ID组成)',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `house_id` bigint NULL DEFAULT NULL COMMENT '关联房源ID',
  `message_type` enum('text','image','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '消息类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `filtered_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '过滤后的内容（用于显示）',
  `has_sensitive` tinyint(1) NULL DEFAULT 0 COMMENT '是否包含敏感词',
  `sensitive_words` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检测到的敏感词列表（JSON数组）',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` timestamp NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_session`(`session_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_chat_user`(`sender_id` ASC, `receiver_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_3` FOREIGN KEY (`house_id`) REFERENCES `houses` (`house_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 'session_24_25', 24, 25, 3, 'text', '您好，我想咨询一下碧水豪庭那套房子', NULL, 0, NULL, 1, '2025-12-18 10:05:00', '2025-12-18 10:00:00');
INSERT INTO `chat_messages` VALUES (2, 'session_24_25', 25, 24, 3, 'text', '您好，欢迎咨询，有什么问题可以问我', NULL, 0, NULL, 1, '2025-12-22 00:27:42', '2025-12-18 10:08:00');
INSERT INTO `chat_messages` VALUES (3, 'session_24_25', 24, 25, 3, 'text', '请问这套房子可以短租吗？', NULL, 0, NULL, 1, '2025-12-18 10:15:00', '2025-12-18 10:12:00');
INSERT INTO `chat_messages` VALUES (4, 'session_24_25', 25, 24, 3, 'text', '不好意思，这套只接受长租，至少一年起租', NULL, 0, NULL, 1, '2025-12-22 00:27:42', '2025-12-18 10:18:00');
INSERT INTO `chat_messages` VALUES (5, 'session_27_24', 27, 24, 1, 'text', '你好，看到您在阳光花园的房子，还有吗？', NULL, 0, NULL, 1, '2025-12-22 00:35:01', '2025-12-18 14:00:00');
INSERT INTO `chat_messages` VALUES (6, 'session_27_24', 24, 27, 1, 'text', '您好，房子还在，欢迎预约看房', NULL, 0, NULL, 1, '2025-12-22 19:42:34', '2025-12-18 14:08:00');
INSERT INTO `chat_messages` VALUES (7, 'session_27_24', 27, 24, 1, 'text', '周日下午方便看房吗？', NULL, 0, NULL, 1, '2025-12-22 00:35:01', '2025-12-18 16:30:00');
INSERT INTO `chat_messages` VALUES (8, '24_27', 27, 24, NULL, 'text', '你好', NULL, 0, NULL, 1, '2025-12-22 00:35:01', '2025-12-20 21:00:52');
INSERT INTO `chat_messages` VALUES (9, '24_27', 24, 27, NULL, 'text', '1', NULL, 0, NULL, 1, '2025-12-22 19:42:34', '2025-12-22 00:33:30');
INSERT INTO `chat_messages` VALUES (10, '24_27', 27, 24, NULL, 'text', '林健', NULL, 0, NULL, 1, '2025-12-22 00:35:01', '2025-12-22 00:35:01');
INSERT INTO `chat_messages` VALUES (11, '24_27', 27, 24, NULL, 'text', '我操你妈个逼', '*****逼', 1, '[\"我操你\",\"妈个\"]', 0, NULL, '2025-12-22 18:45:10');
INSERT INTO `chat_messages` VALUES (12, '24_27', 27, 24, NULL, 'text', '去你妈的', '****', 1, '[\"去你妈的\"]', 0, NULL, '2025-12-22 18:48:50');
INSERT INTO `chat_messages` VALUES (13, '24_27', 27, 24, NULL, 'text', '操你妈', '***', 1, '[\"操你妈\"]', 0, NULL, '2025-12-22 19:42:40');

-- ----------------------------
-- Table structure for communities
-- ----------------------------
DROP TABLE IF EXISTS `communities`;
CREATE TABLE `communities`  (
  `community_id` bigint NOT NULL AUTO_INCREMENT COMMENT '小区ID',
  `community_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '小区名称',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '小区地址',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `property_company` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物业公司',
  `property_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物业电话',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '小区描述',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`community_id`) USING BTREE,
  INDEX `idx_city`(`city` ASC, `district` ASC) USING BTREE,
  INDEX `idx_name`(`community_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '小区表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of communities
-- ----------------------------
INSERT INTO `communities` VALUES (1, '阳光花园', '北京市朝阳区望京街道阳光大道88号', '北京市', '北京市', '朝阳区', NULL, NULL, '阳光物业管理有限公司', '010-12345678', '高档住宅小区，交通便利，配套齐全', 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `communities` VALUES (2, '碧水豪庭', '北京市海淀区中关村南大街168号', '北京市', '北京市', '海淀区', NULL, NULL, '碧水物业服务公司', '010-23456789', '智能化社区，靠近地铁站，周边商业发达', 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `communities` VALUES (3, '绿城春晓', '北京市西城区德胜门外大街256号', '北京市', '北京市', '西城区', NULL, NULL, '绿城物业集团', '010-34567890', '成熟小区，学区房，适合家庭居住', 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');

-- ----------------------------
-- Table structure for community_facilities
-- ----------------------------
DROP TABLE IF EXISTS `community_facilities`;
CREATE TABLE `community_facilities`  (
  `facility_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配套ID',
  `community_id` bigint NOT NULL COMMENT '小区ID',
  `facility_type` enum('internal','surrounding') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配套类型: internal-小区内, surrounding-周边3公里',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类(如: 商超、公交站、快递柜)',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设施名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URL(JSON数组)',
  `contact_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `distance` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '距离(仅周边配套)',
  `location` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置描述',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`facility_id`) USING BTREE,
  INDEX `idx_community`(`community_id` ASC, `facility_type` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  CONSTRAINT `community_facilities_ibfk_1` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '小区配套信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community_facilities
-- ----------------------------
INSERT INTO `community_facilities` VALUES (1, 1, 'internal', '物业', '阳光幼儿园', '小区配套幼儿园，双语教学', NULL, '', '100米', '阳光花园1号楼', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (2, 1, 'internal', '运动', '阳光健身中心', '24小时健身房，配备专业教练', NULL, '010-12340002', '200米', '阳光花园地下一层', NULL, NULL, NULL, 'inactive', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (3, 1, 'surrounding', '交通', '望京地铁站', '地铁14号线、15号线', NULL, NULL, '500米', '望京街', NULL, NULL, NULL, 'inactive', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (4, 1, 'surrounding', '商超', '华联购物中心', '大型综合购物商场', NULL, '010-12340003', '800米', '望京SOHO', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (5, 1, 'surrounding', '医院', '北京协和医院望京分院', '三甲医院分院', NULL, '', '1米', '望京西路', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (6, 2, 'internal', '运动', '碧水游泳馆', '恒温游泳池，儿童戏水区', NULL, '010-23450001', '150米', '碧水豪庭会所', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (7, 2, 'surrounding', '交通', '中关村地铁站', '地铁4号线', NULL, NULL, '300米', '中关村大街', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (8, 2, 'surrounding', '商超', '中关村图书大厦', '大型书店及文化商场', NULL, '010-23450002', '600米', '中关村广场', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (9, 2, 'surrounding', '医疗', '北京大学第三医院', '三甲综合医院', NULL, '010-23450003', '2公里', '花园北路', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (10, 3, 'internal', '娱乐', '绿城儿童乐园', '室外儿童游乐设施', NULL, NULL, '100米', '绿城春晓中心花园', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (11, 3, 'surrounding', '交通', '德胜门地铁站', '地铁2号线', NULL, NULL, '400米', '德胜门外大街', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');
INSERT INTO `community_facilities` VALUES (12, 3, 'surrounding', '商超', '家乐福超市', '大型连锁超市', NULL, '010-34560001', '500米', '德胜门购物广场', NULL, NULL, NULL, 'active', '2025-12-19 15:09:00', '2025-12-19 15:09:00');

-- ----------------------------
-- Table structure for community_verifications
-- ----------------------------
DROP TABLE IF EXISTS `community_verifications`;
CREATE TABLE `community_verifications`  (
  `verification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `community_id` bigint NOT NULL COMMENT '小区ID',
  `proof_type` enum('rental_contract','property_fee','ownership_cert','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证明类型',
  `proof_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '证明材料图片URL(JSON数组)',
  `apply_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申请理由',
  `community_admin_status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '小区管理员审核状态',
  `community_admin_id` bigint NULL DEFAULT NULL COMMENT '小区管理员ID',
  `community_admin_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '小区管理员审核意见',
  `community_admin_time` timestamp NULL DEFAULT NULL COMMENT '小区管理员审核时间',
  `platform_admin_status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '平台管理员审核状态',
  `platform_admin_id` bigint NULL DEFAULT NULL COMMENT '平台管理员ID',
  `platform_admin_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '平台管理员审核意见',
  `platform_admin_time` timestamp NULL DEFAULT NULL COMMENT '平台管理员审核时间',
  `final_status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '最终审核状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`verification_id`) USING BTREE,
  INDEX `community_id`(`community_id` ASC) USING BTREE,
  INDEX `idx_user_community`(`user_id` ASC, `community_id` ASC) USING BTREE,
  INDEX `idx_status`(`final_status` ASC) USING BTREE,
  CONSTRAINT `community_verifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `community_verifications_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '小区认证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community_verifications
-- ----------------------------
INSERT INTO `community_verifications` VALUES (1, 24, 1, 'property_fee', '[\"https://example.com/proofs/community_24_1.jpg\",\"https://example.com/proofs/community_24_2.jpg\"]', NULL, 'approved', NULL, NULL, '2025-12-01 10:00:00', 'approved', NULL, NULL, '2025-12-01 15:00:00', 'approved', '2025-11-30 09:00:00', '2025-12-19 15:54:45');
INSERT INTO `community_verifications` VALUES (2, 25, 2, 'property_fee', '[\"https://example.com/proofs/community_25_1.jpg\"]', NULL, 'approved', NULL, NULL, '2025-11-28 14:00:00', 'approved', NULL, NULL, '2025-11-28 16:00:00', 'approved', '2025-11-27 10:00:00', '2025-12-19 15:54:45');
INSERT INTO `community_verifications` VALUES (3, 26, 3, 'rental_contract', '[\"https://example.com/proofs/community_26_1.jpg\"]', NULL, 'approved', NULL, NULL, '2025-11-25 11:00:00', 'approved', NULL, NULL, '2025-11-25 14:00:00', 'approved', '2025-11-24 15:00:00', '2025-12-19 15:54:45');
INSERT INTO `community_verifications` VALUES (8, 27, 1, 'rental_contract', '[\"http://tmp/DpnIBwyAjdSg1941ed7a736993df592709638e79a4ca.jpg\"]', '阿三大苏打', 'approved', 19, '认证材料真实有效，初审通过', '2025-12-22 15:57:26', 'approved', 18, '平台终审通过', '2025-12-22 16:04:10', 'approved', '2025-12-22 15:56:58', '2025-12-22 15:56:58');

-- ----------------------------
-- Table structure for contract_templates
-- ----------------------------
DROP TABLE IF EXISTS `contract_templates`;
CREATE TABLE `contract_templates`  (
  `template_id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板内容（核心条款，不可修改）',
  `policy_reference` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '政策依据（如2025住房租赁条例）',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1.0' COMMENT '版本号',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`template_id`) USING BTREE,
  INDEX `idx_active`(`is_active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '合同模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contract_templates
-- ----------------------------
INSERT INTO `contract_templates` VALUES (1, '标准住房租赁合同', '【第一条 房屋基本情况】\r\n甲方（出租人）将位于{community_name}{house_address}的房屋出租给乙方（承租人）使用。房屋建筑面积{area}平方米，户型{house_type}。\r\n\r\n【第二条 租赁期限】\r\n租赁期限自{start_date}起至{end_date}止，共{months}个月。\r\n\r\n【第三条 租金及支付方式】\r\n月租金为人民币{rent_price}元整，支付方式为{payment_method}。\r\n\r\n【第四条 房屋用途】\r\n乙方承诺，租赁该房屋仅作为居住使用，不得擅自改变房屋用途，不得利用该房屋从事违法活动。\r\n\r\n【第五条 房屋维护】\r\n1. 甲方应保证房屋及其附属设施处于正常可使用状态。\r\n2. 乙方应合理使用房屋及附属设施，如因乙方使用不当造成损坏，乙方应负责修复或赔偿。\r\n3. 房屋及附属设施的自然损耗，由甲方负责维修。\r\n\r\n【第六条 合同解除】\r\n1. 经双方协商一致，可以解除本合同。\r\n2. 一方违约，另一方有权解除合同并要求赔偿损失。\r\n\r\n【第七条 争议解决】\r\n本合同在履行过程中发生争议，由双方协商解决；协商不成的，可向房屋所在地人民法院起诉。\r\n\r\n【第八条 其他约定】\r\n{custom_terms}\r\n\r\n本合同一式两份，甲乙双方各执一份，具有同等法律效力。', '依据《中华人民共和国民法典》第七百零三条至第七百三十四条及2025年《住房租赁条例》相关规定制定', '1.0', 1, NULL, '2025-12-21 18:22:43', '2025-12-21 18:22:43');

-- ----------------------------
-- Table structure for credit_evaluations
-- ----------------------------
DROP TABLE IF EXISTS `credit_evaluations`;
CREATE TABLE `credit_evaluations`  (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `evaluator_id` bigint NOT NULL COMMENT '评价人ID',
  `evaluated_id` bigint NOT NULL COMMENT '被评价人ID',
  `star_rating` decimal(2, 1) NULL DEFAULT NULL COMMENT '星级评分(0-5)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `contract_performance` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '合同履行情况描述',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价标签(JSON数组)',
  `is_anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  INDEX `evaluator_id`(`evaluator_id` ASC) USING BTREE,
  INDEX `idx_evaluated`(`evaluated_id` ASC) USING BTREE,
  INDEX `idx_contract`(`contract_id` ASC) USING BTREE,
  CONSTRAINT `credit_evaluations_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `rental_contracts` (`contract_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `credit_evaluations_ibfk_2` FOREIGN KEY (`evaluator_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `credit_evaluations_ibfk_3` FOREIGN KEY (`evaluated_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `credit_evaluations_chk_1` CHECK ((`star_rating` >= 0) and (`star_rating` <= 5))
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '信用评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of credit_evaluations
-- ----------------------------
INSERT INTO `credit_evaluations` VALUES (1, 2, 27, 24, 3.0, '', NULL, '及时维修', 0, '2025-12-22 00:56:20');

-- ----------------------------
-- Table structure for facility_feedbacks
-- ----------------------------
DROP TABLE IF EXISTS `facility_feedbacks`;
CREATE TABLE `facility_feedbacks`  (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `community_id` bigint NOT NULL COMMENT '小区ID',
  `facility_id` bigint NULL DEFAULT NULL COMMENT '配套设施ID(可为空)',
  `user_id` bigint NOT NULL COMMENT '反馈用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `status` enum('pending','processed','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '处理状态',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handler_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理回复',
  `handled_at` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `idx_community`(`community_id` ASC) USING BTREE,
  INDEX `idx_facility`(`facility_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `facility_feedbacks_ibfk_1` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `facility_feedbacks_ibfk_2` FOREIGN KEY (`facility_id`) REFERENCES `community_facilities` (`facility_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `facility_feedbacks_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配套设施反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of facility_feedbacks
-- ----------------------------
INSERT INTO `facility_feedbacks` VALUES (1, 1, 1, 24, '1111', 'processed', 19, '', '2025-12-21 16:57:04', '2025-12-21 16:55:47', '2025-12-21 16:55:47');
INSERT INTO `facility_feedbacks` VALUES (2, 2, 7, 24, '假的', 'pending', NULL, NULL, NULL, '2025-12-21 17:03:31', '2025-12-21 17:03:31');
INSERT INTO `facility_feedbacks` VALUES (3, 1, 2, 24, '请描述需要更新或纠正的内容', 'processed', 19, '1111', '2025-12-21 17:04:46', '2025-12-21 17:04:35', '2025-12-21 17:04:35');
INSERT INTO `facility_feedbacks` VALUES (4, 1, 3, 24, '111111111', 'processed', 19, '好的', '2025-12-21 17:20:51', '2025-12-21 17:20:28', '2025-12-21 17:20:28');

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` enum('bug','suggestion','service','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈类型：bug-功能异常，suggestion-功能建议，service-服务问题，other-其他问题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式（手机号或微信号）',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件图片（多张用逗号分隔）',
  `status` enum('pending','processing','resolved') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending-待处理，processing-处理中，resolved-已解决',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '管理员回复',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of feedback
-- ----------------------------
INSERT INTO `feedback` VALUES (1, 24, 'suggestion', '建议增加房源3D看房功能，可以更直观地了解房屋布局', '13800138000', NULL, 'processing', '感谢您的建议，我们会考虑在后续版本中加入此功能', '2025-12-19 15:54:40', '2025-12-19 15:54:40');

-- ----------------------------
-- Table structure for file_uploads
-- ----------------------------
DROP TABLE IF EXISTS `file_uploads`;
CREATE TABLE `file_uploads`  (
  `file_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '访问URL',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型(MIME)',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件分类(avatar,house,proof,contract等)',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC, `related_id` ASC) USING BTREE,
  CONSTRAINT `file_uploads_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件上传表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_uploads
-- ----------------------------
INSERT INTO `file_uploads` VALUES (1, 27, 'fUQ7RzokFnBf50288fea5d1dfc88bb19dd88151a3ec2.jpeg', 'aeedc271c4fc4bfd8ba839709d3e4b59.jpeg', 'aeedc271c4fc4bfd8ba839709d3e4b59.jpeg', 'http://172.31.14.194:9000/avatars/aeedc271c4fc4bfd8ba839709d3e4b59.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20251221%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251221T165234Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0d03c91bbfb245902c0dc25e722ca2f1550afda477b98820aa26ca31bfa8c8b4', 4382, 'image/jpeg', 'avatar', 27, '2025-12-22 00:52:34');
INSERT INTO `file_uploads` VALUES (2, 27, 'LFauEcpWlDgp1941ed7a736993df592709638e79a4ca.jpg', '3feeb1986d7e4c51a4ad4d839408bb00.jpg', '3feeb1986d7e4c51a4ad4d839408bb00.jpg', 'http://172.31.14.194:9000/avatars/3feeb1986d7e4c51a4ad4d839408bb00.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20251221%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251221T165953Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f1721b8e9bd3bb86f2f56ab06af7fa16dc53bc632b947894578e777c44a87f58', 10260, 'image/jpeg', 'avatar', 27, '2025-12-22 00:59:53');

-- ----------------------------
-- Table structure for forum_posts
-- ----------------------------
DROP TABLE IF EXISTS `forum_posts`;
CREATE TABLE `forum_posts`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `community_id` bigint NOT NULL COMMENT '所属小区ID',
  `post_type` enum('rent_need','share_info','discussion') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子类型',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '帖子内容',
  `budget_min` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算下限(仅租房需求)',
  `budget_max` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算上限(仅租房需求)',
  `expected_area` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '期望面积(仅租房需求)',
  `expected_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '期望户型(仅租房需求)',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URL(JSON数组)',
  `is_urgent` tinyint(1) NULL DEFAULT 0 COMMENT '是否紧急',
  `status` enum('active','closed','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '帖子状态',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_community`(`community_id` ASC, `post_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `forum_posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `forum_posts_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛帖子表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_posts
-- ----------------------------

-- ----------------------------
-- Table structure for forum_replies
-- ----------------------------
DROP TABLE IF EXISTS `forum_replies`;
CREATE TABLE `forum_replies`  (
  `reply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '回复用户ID',
  `parent_reply_id` bigint NULL DEFAULT NULL COMMENT '父回复ID(支持嵌套回复)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URL(JSON数组)',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
  PRIMARY KEY (`reply_id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_reply_id` ASC) USING BTREE,
  CONSTRAINT `forum_replies_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`post_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `forum_replies_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `forum_replies_ibfk_3` FOREIGN KEY (`parent_reply_id`) REFERENCES `forum_replies` (`reply_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛回复表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_replies
-- ----------------------------

-- ----------------------------
-- Table structure for house_appointments
-- ----------------------------
DROP TABLE IF EXISTS `house_appointments`;
CREATE TABLE `house_appointments`  (
  `appointment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `tenant_id` bigint NOT NULL COMMENT '租客用户ID',
  `landlord_id` bigint NOT NULL COMMENT '房东用户ID',
  `appointment_time` datetime NOT NULL COMMENT '预约时间',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '留言备注',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '预约状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`appointment_id`) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  CONSTRAINT `house_appointments_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`house_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `house_appointments_ibfk_2` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `house_appointments_ibfk_3` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约看房表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house_appointments
-- ----------------------------
INSERT INTO `house_appointments` VALUES (1, 3, 24, 25, '2025-12-20 14:00:00', '13800138000', '您好，我对这套房子很感兴趣，想预约周五下午看房，方便吗？', 'confirmed', '2025-12-19 15:55:46', '2025-12-19 15:55:46');
INSERT INTO `house_appointments` VALUES (2, 4, 24, 26, '2025-12-21 10:00:00', '13800138000', '我想看房，周六上午有空吗？', 'pending', '2025-12-19 15:55:46', '2025-12-19 15:55:46');
INSERT INTO `house_appointments` VALUES (3, 1, 27, 24, '2025-12-22 15:00:00', '13600136000', '想看房，下周日下午方便吗？', 'completed', '2025-12-19 15:55:46', '2025-12-21 21:19:20');
INSERT INTO `house_appointments` VALUES (4, 2, 28, 24, '2025-12-19 18:00:00', '13500135000', '今晚6点能看房吗？急租', 'completed', '2025-12-19 15:55:46', '2025-12-19 15:55:46');

-- ----------------------------
-- Table structure for house_favorites
-- ----------------------------
DROP TABLE IF EXISTS `house_favorites`;
CREATE TABLE `house_favorites`  (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `uk_user_house`(`user_id` ASC, `house_id` ASC) USING BTREE,
  INDEX `house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `house_favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `house_favorites_ibfk_2` FOREIGN KEY (`house_id`) REFERENCES `houses` (`house_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house_favorites
-- ----------------------------
INSERT INTO `house_favorites` VALUES (3, 27, 1, '2025-12-19 15:12:10');
INSERT INTO `house_favorites` VALUES (4, 28, 1, '2025-12-19 15:12:10');
INSERT INTO `house_favorites` VALUES (5, 28, 2, '2025-12-19 15:12:10');
INSERT INTO `house_favorites` VALUES (8, 24, 2, '2025-12-19 15:27:57');
INSERT INTO `house_favorites` VALUES (11, 27, 6, '2025-12-21 15:16:15');

-- ----------------------------
-- Table structure for house_images
-- ----------------------------
DROP TABLE IF EXISTS `house_images`;
CREATE TABLE `house_images`  (
  `image_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `image_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片原始名称',
  `image_size` bigint NULL DEFAULT NULL COMMENT '图片大小(字节)',
  `image_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片类型(MIME)',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `is_cover` tinyint(1) NULL DEFAULT 0 COMMENT '是否为封面图',
  `upload_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`image_id`) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  CONSTRAINT `house_images_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`house_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源图片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house_images
-- ----------------------------
INSERT INTO `house_images` VALUES (1, 1, 'http://localhost:9000/houserent/houses/2025/12/20/d7d6f5ec-594a-4cd9-8a3e-87d87b5c0fb0.jpg', 'h70Li7YtHayH1941ed7a736993df592709638e79a4ca.jpg', 10260, 'image/jpeg', 1, 1, '2025-12-20 02:40:48', '2025-12-20 02:40:48', '2025-12-20 02:40:48');
INSERT INTO `house_images` VALUES (2, 1, 'http://localhost:9000/houserent/houses/2025/12/20/7f109446-fa81-447e-a2ff-8c0f25238755.jpg', 'PhdxxoK3Z7NH1941ed7a736993df592709638e79a4ca.jpg', 10260, 'image/jpeg', 2, 0, '2025-12-20 02:40:48', '2025-12-20 02:40:48', '2025-12-20 02:40:48');
INSERT INTO `house_images` VALUES (3, 1, 'http://localhost:9000/houserent/houses/2025/12/20/3114c3df-246f-48ee-9f51-48672fcaeba7.jpg', 'aPhXZ0R8ngdH8ae59ecd7ada91bcb905b8d26458665d.jpg', 123330, 'image/jpeg', 3, 0, '2025-12-20 02:40:48', '2025-12-20 02:40:48', '2025-12-20 02:40:48');
INSERT INTO `house_images` VALUES (4, 1, 'http://localhost:9000/houserent/houses/2025/12/20/89d34589-5b2c-453d-90d2-4535290cc1d5.jpg', 'h70Li7YtHayH1941ed7a736993df592709638e79a4ca.jpg', 10260, 'image/jpeg', 4, 0, '2025-12-20 03:00:14', '2025-12-20 03:00:14', '2025-12-20 03:00:14');
INSERT INTO `house_images` VALUES (5, 1, 'http://localhost:9000/houserent/houses/2025/12/20/2dae63a4-5180-4b32-b412-cc778d15dca0.jpg', 'PhdxxoK3Z7NH1941ed7a736993df592709638e79a4ca.jpg', 10260, 'image/jpeg', 5, 0, '2025-12-20 03:00:14', '2025-12-20 03:00:14', '2025-12-20 03:00:14');
INSERT INTO `house_images` VALUES (6, 1, 'http://localhost:9000/houserent/houses/2025/12/20/d28f3cfa-954e-4d40-b916-af4f527f7f4f.jpg', 'mmkBw7u0PYa88ae59ecd7ada91bcb905b8d26458665d.jpg', 123330, 'image/jpeg', 6, 0, '2025-12-20 03:00:14', '2025-12-20 03:00:14', '2025-12-20 03:00:14');
INSERT INTO `house_images` VALUES (7, 5, 'http://localhost:9000/houserent/houses/2025/12/20/acc9a575-68b4-4ed1-87ac-6ca21475c090.png', 'YbrkP1WkpQsM56f7ac1ad943fe7e515293988174c4d3.png', 2501048, 'image/png', 1, 1, '2025-12-20 03:23:33', '2025-12-20 03:23:33', '2025-12-20 03:23:33');
INSERT INTO `house_images` VALUES (9, 2, 'http://localhost:9000/houserent/houses/2025/12/20/c4fd05b9-c52c-4a97-9821-243915f0deed.png', 'LpQtxMaK88u456f7ac1ad943fe7e515293988174c4d3.png', 2501048, 'image/png', 1, 1, '2025-12-20 16:57:43', '2025-12-20 16:57:43', '2025-12-20 16:57:43');

-- ----------------------------
-- Table structure for houses
-- ----------------------------
DROP TABLE IF EXISTS `houses`;
CREATE TABLE `houses`  (
  `house_id` bigint NOT NULL AUTO_INCREMENT COMMENT '房源ID',
  `landlord_id` bigint NOT NULL COMMENT '房东用户ID',
  `community_id` bigint NOT NULL COMMENT '所属小区ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房源标题',
  `house_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '户型(如: 2室1厅1卫)',
  `area` decimal(8, 2) NULL DEFAULT NULL COMMENT '面积(平方米)',
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层',
  `total_floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '总楼层',
  `orientation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '朝向',
  `decoration` enum('rough','simple','fine','luxury') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '装修程度',
  `rent_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '租金(元/月)',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式(如: 押一付一)',
  `rent_period` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租赁期限',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '房屋描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '房屋图片URL(JSON数组)',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `facilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配套设施(JSON数组)',
  `audit_status` enum('draft','pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft' COMMENT '审核状态',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `audit_time` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `publish_status` enum('online','offline','rented') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'offline' COMMENT '发布状态',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` int NULL DEFAULT 0 COMMENT '收藏次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`house_id`) USING BTREE,
  INDEX `idx_landlord`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_community`(`community_id` ASC) USING BTREE,
  INDEX `idx_status`(`audit_status` ASC, `publish_status` ASC) USING BTREE,
  INDEX `idx_price`(`rent_price` ASC) USING BTREE,
  INDEX `idx_house_search`(`community_id` ASC, `publish_status` ASC, `rent_price` ASC, `area` ASC) USING BTREE,
  CONSTRAINT `houses_ibfk_1` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `houses_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of houses
-- ----------------------------
INSERT INTO `houses` VALUES (1, 24, 1, '阳光花园精装两居室，采光好交通便利', '1', 85.50, '10', '18', '南', 'fine', 6500.00, '押一付一', '长期', '房屋位于望京核心区域，精装修，家具家电齐全。主卧朝南，次卧朝北，客厅宽敞明亮。小区环境优美，物业管理规范。周边地铁、商场、医院等配套设施完善，交通便利。适合年轻白领或小家庭居住。', '[\"http://tmp/h70Li7YtHayH1941ed7a736993df592709638e79a4ca.jpg\",\"http://tmp/PhdxxoK3Z7NH1941ed7a736993df592709638e79a4ca.jpg\"]', '13800138000', NULL, 'approved', 19, '房源信息完整，符合发布要求，审核通过', '2025-12-21 20:57:54', 'online', 171, 2, '2025-12-19 15:12:10', '2025-12-22 01:00:08');
INSERT INTO `houses` VALUES (2, 24, 1, '阳光花园舒适一居室，拎包入住', '1室1厅1卫', 52.00, '5', '18', '东', 'simple', 4200.00, '押一付三', '短期', '温馨一居室，适合单身白领。房间干净整洁，基础装修，配备床、衣柜、空调、冰箱、洗衣机等必要家电。交通方便，楼下就是公交站，步行5分钟到地铁站。', NULL, '13800138000', NULL, 'pending', NULL, NULL, NULL, 'offline', 89, 2, '2025-12-19 15:12:10', '2025-12-20 16:57:35');
INSERT INTO `houses` VALUES (3, 25, 2, '碧水豪庭高端三居室，智能家居', '3室2厅2卫', 128.00, '15', '20', '南', 'luxury', 12000.00, '押一付一', '长期', '顶级豪华装修，全套智能家居系统。三间卧室均朝南，主卧带独立卫浴。客厅餐厅连通，视野开阔。厨房配备进口家电。小区智能化管理，24小时安保。地铁口200米，购物中心楼下。', NULL, '13900139000', NULL, 'approved', NULL, NULL, NULL, 'online', 263, 0, '2025-12-19 15:12:10', '2025-12-21 23:32:19');
INSERT INTO `houses` VALUES (4, 26, 3, '绿城春晓学区房两居室，近重点小学', '2室1厅1卫', 75.00, '3', '6', '南', 'simple', 5500.00, '押一付一', '长期', '学区房，对口重点小学。简单装修，采光良好。楼层低，适合有老人或小孩的家庭。小区成熟安静，物业负责。周边生活配套齐全，菜市场、超市步行可达。', NULL, '13700137000', NULL, 'approved', NULL, NULL, NULL, 'online', 189, 0, '2025-12-19 15:12:10', '2025-12-19 15:12:10');
INSERT INTO `houses` VALUES (5, 24, 1, '111', '111', 111.00, '11', NULL, NULL, NULL, 111.00, '押一付一', NULL, '111', NULL, NULL, NULL, 'approved', 19, '房源信息完整，符合发布要求，审核通过', '2025-12-21 15:23:33', 'online', 20, 0, '2025-12-20 03:23:32', '2025-12-22 20:08:48');
INSERT INTO `houses` VALUES (6, 24, 1, '11', '1', 11.00, '1', NULL, NULL, NULL, 11.00, '押一付一', NULL, '', NULL, NULL, NULL, 'pending', 19, '2', '2025-12-21 16:11:52', 'offline', 24, 1, '2025-12-20 22:18:26', '2025-12-22 00:22:40');

-- ----------------------------
-- Table structure for landlord_verification
-- ----------------------------
DROP TABLE IF EXISTS `landlord_verification`;
CREATE TABLE `landlord_verification`  (
  `verification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号',
  `id_card_front` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证正面照片URL',
  `id_card_back` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证反面照片URL',
  `property_proof` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房产证明照片URL（房产证或租赁合同）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `community_id` bigint NULL DEFAULT NULL COMMENT '所属小区ID',
  `status` enum('pending','community_审核中','platform_审核中','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending-待审核，community_审核中-小区管理员审核中，platform_审核中-平台管理员审核中，approved-已通过，rejected-已拒绝',
  `community_audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '小区管理员审核意见',
  `community_auditor_id` bigint NULL DEFAULT NULL COMMENT '小区管理员审核人ID',
  `community_audit_time` timestamp NULL DEFAULT NULL COMMENT '小区管理员审核时间',
  `platform_audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '平台管理员审核意见',
  `platform_auditor_id` bigint NULL DEFAULT NULL COMMENT '平台管理员审核人ID',
  `platform_audit_time` timestamp NULL DEFAULT NULL COMMENT '平台管理员审核时间',
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '拒绝原因',
  `apply_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`verification_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_community`(`community_id` ASC) USING BTREE,
  CONSTRAINT `landlord_verification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `landlord_verification_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房东认证审核表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of landlord_verification
-- ----------------------------
INSERT INTO `landlord_verification` VALUES (1, 24, '张三', '110101199001011234', 'https://example.com/images/idcard_front_24.jpg', 'https://example.com/images/idcard_back_24.jpg', 'https://example.com/images/property_proof_24.jpg', '13800138000', 1, 'approved', '材料真实，本小区业主，审核通过', NULL, '2025-12-15 10:30:00', '资质符合要求，平台审核通过', NULL, '2025-12-16 14:20:00', NULL, '2025-12-14 09:00:00', '2025-12-19 15:09:00');

-- ----------------------------
-- Table structure for rent_payment_records
-- ----------------------------
DROP TABLE IF EXISTS `rent_payment_records`;
CREATE TABLE `rent_payment_records`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `transaction_id` bigint NOT NULL COMMENT '关联交易ID',
  `contract_id` bigint NOT NULL COMMENT '关联合同ID',
  `landlord_id` bigint NOT NULL COMMENT '房东ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `payment_type` enum('rent','deposit','utility','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'rent' COMMENT '支付类型：rent-租金,deposit-押金,utility-水电费,other-其他',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额',
  `payment_period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付周期（如2024-01表示2024年1月）',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式（现金/微信/支付宝/银行转账）',
  `tenant_confirmed` tinyint(1) NULL DEFAULT 0 COMMENT '租客确认已支付',
  `tenant_confirm_time` timestamp NULL DEFAULT NULL COMMENT '租客确认时间',
  `landlord_confirmed` tinyint(1) NULL DEFAULT 0 COMMENT '房东确认已收款',
  `landlord_confirm_time` timestamp NULL DEFAULT NULL COMMENT '房东确认时间',
  `status` enum('pending','confirmed','disputed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending-待确认,confirmed-已确认,disputed-有争议',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_transaction`(`transaction_id` ASC) USING BTREE,
  INDEX `idx_landlord`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_period`(`payment_period` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租金支付记录表（线下支付线上确认）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rent_payment_records
-- ----------------------------

-- ----------------------------
-- Table structure for rental_contracts
-- ----------------------------
DROP TABLE IF EXISTS `rental_contracts`;
CREATE TABLE `rental_contracts`  (
  `contract_id` bigint NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `contract_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合同编号',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `landlord_id` bigint NOT NULL COMMENT '房东ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `community_id` bigint NOT NULL COMMENT '小区ID',
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '标准条款内容',
  `custom_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '补充条款内容',
  `rent_price` decimal(10, 2) NOT NULL COMMENT '租金',
  `deposit` decimal(10, 2) NULL DEFAULT NULL COMMENT '押金金额',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `start_date` date NOT NULL COMMENT '租赁开始日期',
  `end_date` date NOT NULL COMMENT '租赁结束日期',
  `landlord_signature` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '房东签名(Base64)',
  `landlord_sign_time` timestamp NULL DEFAULT NULL COMMENT '房东签名时间',
  `tenant_signature` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '租户签名(Base64)',
  `tenant_sign_time` timestamp NULL DEFAULT NULL COMMENT '租户签名时间',
  `contract_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合同哈希值(防篡改)',
  `audit_status` enum('draft','pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft' COMMENT '审核状态',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `audit_time` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `contract_status` enum('draft','signed','effective','terminated','expired') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft',
  `pdf_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合同PDF文件URL',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `termination_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `termination_requested_by` bigint NULL DEFAULT NULL,
  `termination_request_time` timestamp NULL DEFAULT NULL,
  `termination_status` enum('none','pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'none',
  PRIMARY KEY (`contract_id`) USING BTREE,
  UNIQUE INDEX `contract_no`(`contract_no` ASC) USING BTREE,
  INDEX `community_id`(`community_id` ASC) USING BTREE,
  INDEX `idx_house`(`house_id` ASC) USING BTREE,
  INDEX `idx_landlord`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`contract_status` ASC) USING BTREE,
  INDEX `idx_contract_date`(`start_date` ASC, `end_date` ASC, `contract_status` ASC) USING BTREE,
  CONSTRAINT `rental_contracts_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`house_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_contracts_ibfk_2` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_contracts_ibfk_3` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_contracts_ibfk_4` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租赁合同表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rental_contracts
-- ----------------------------
INSERT INTO `rental_contracts` VALUES (1, 'CONTRACT_20251221_F2D98736', 5, 24, 27, 1, '房屋租赁合同\n\n甲方（出租方）：%s\n乙方（承租方）：%s\n\n根据《中华人民共和国合同法》及相关法律法规，甲乙双方在平等、自愿、公平和诚实信用的基础上，\n就乙方承租甲方房屋事宜，为明确双方权利义务关系，经协商一致，订立本合同。\n\n第一条 房屋基本情况\n甲方房屋位于：%s\n房屋类型：%s\n建筑面积：%.2f平方米\n房屋朝向：%s\n装修情况：%s\n\n第二条 租赁期限\n租赁期限自%s至%s，共计%d个月。\n\n第三条 租金\n月租金为人民币%s元（大写：%s）。\n租金支付方式：%s\n\n第四条 押金\n乙方应于签订本合同时向甲方支付房屋押金，押金数额为%s元。\n\n第五条 甲方权利和义务\n1. 甲方应当按时将房屋及附属设施交付乙方使用。\n2. 甲方保证房屋的建筑结构和设备设施符合建筑、消防、治安、卫生等方面的安全条件。\n3. 租赁期间，甲方不得随意进入出租房屋。\n\n第六条 乙方权利和义务\n1. 乙方应当按照约定的用途使用房屋，不得擅自改变使用用途。\n2. 乙方应当爱护并合理使用房屋及其附属设施。\n3. 乙方应当按时支付租金。\n\n第七条 补充条款\n%s\n\n第八条 违约责任\n甲乙双方应当严格履行本合同约定的义务，如一方违约，应承担相应的违约责任。\n\n第九条 争议解决\n本合同履行过程中发生的争议，由双方协商解决；协商不成的，可向房屋所在地人民法院起诉。\n\n第十条 合同生效\n本合同自甲乙双方签字盖章后生效。\n\n甲方（出租方）签名：_________________ 日期：_________\n\n乙方（承租方）签名：_________________ 日期：_________\n\n小区管理员审核：_________________ 日期：_________\n', '', 111.00, 222.00, '押一付三', '2025-12-21', '2028-12-20', 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAARIAAACACAYAAADH7ubfAAAAAXNSR0IArs4c6QAAFZdJREFUeF7tnQvQldP3x3dElFsmFLk0mqmMSyHMEIrKtYSZKLeIpqhojEuUyzSUxpAMKU2hIjRKNYgiZaRQMqNyKWVyS5JCRfrNZ//t5787nfOe5zz3/Zy1Zt55b8++ffc+32fttddeq9aOHTt2KBFBQBAQBEIgUEuIJAR6UlQQEAQ0AkIkshAEAUEgNAJCJKEhlAoEAUFAiETWgCAgCIRGQIgkNIRSgSAgCAiRyBoQBASB0AgIkYSGUCoQBAQBIRJZA4KAIBAaASGS0BBKBYKAICBEImtAEBAEQiMgRBIaQqlAEBAEhEhkDQgCgkBoBIRIQkMoFQgCgoATRLJkyRI9Uy1btpQZEwQEgQwikHkiGT9+vOrRo4c64IAD1LvvvitkksFFJF0SBDJPJPfff7964IEHPI1k8eLFMmuCgCCQMQQyTyTffvut1kI2btyooXvsscfUrbfemjEYpTuCQHUjkHkiYXpsrYQtDlrJUUcdVd0zJ6MXBDKEgBNEYgytn332mYbukksuUa+99lqGYJSuCALVjYAzRPLee++ptm3berMFkUAoIoKAIJA+As4QCVBhGxkxYoRGja0NWxy2OiKCgCCQLgJOEclvv/2mCcQYXvv3768ef/zxdBGU1jOLAOvFGOsz28mcdMwpIgHzqVOnqi5dunjwo5WIo1pOVmOEw4BEmjRpovguL5wIgS1RlXNEwjjOPvtsNXfuXD0kSER8S+JfKK61YNvUzjrrLMXvIvEh4CSRoK7ytjEiviXxLRBXa7ZdBkQjiX8WnSQSYCn0LVm1apUYXuNfL860cN1116nnnntO91deNPFPm7NEAjQYXlevXq1REt+S+BeLSy3Y21/uaPG7SHwIOE0khb4lY8aMUT179owPLanZGQRq1arl9XXDhg2ircY8c04TCdh069ZNvfjiixomFg8/d+3aNWbYpPosI0DYiVatWukuHnnkkfoIWCReBJwnEo73DjzwQLVjxw6NVMOGDdUPP/wQL2pSe6YRsF0E5MQmmalynkiAady4cer6668XVTaZNZP5VmxD/H333acN8yLxIpALIgGiY445Ri1btkyjBbFgtRepTgQwvE+bNk3WQoLTnxsiwVX+tttu09DJcV+CKyiDTdmneeL5nMwE5YZIgItLfSwiCXyUzOLJaiv2iY2xnWW1r3npV66IJC+TIuMIjoDtEnDCCScoEzg8eI1S0g8CQiR+UJJnnEHA3uJ27txZX/IUiR8BIZL4MZYWEkTAjlkjJzbJAS9EkhzW0lICCNiGVnGNTwDw/5oQIkkOa2kpAQTq1Kmjtm3bpltasGCBOvXUUxNoVZoQIpE1kCsECL1pIuitXbtWHXroobkaX1YHI0SS1ZmRfgVC4LDDDlPff/+9LpsFIuEUCf8mnOSw2eRVnCES24iW18nI+rjOOOMMNW/evEx3M2tEYtts8nyK5AyR2CprpldyjjuHo9e///6b6RFmgUjeeecd9eyzzyrsNZMnT1Zbt27VmNWuXVv9/fffmcYvaOecIRIuXuEjYPa/QQcs5YIjsNtuu6nt27cHryCBkmkSCeEKWKcmMlvhcNu1a6dmz56dAArJN+EMkSQPTfW0yD6eS258L+YJCoHsscce6pprrlGjR4/ONDBpEAkEQqL78ePHF8UGTa5NmzZewPJMAxiwc0IkAYFzuRiEQRR+vD5rciGHPAYOHKjvLrmSiGy//fZTmzZtit3YCm5gOGrUKLV8+fJdlkOjRo3UXnvtpSA2skI2aNDA5SVTtu9CJGUhcv+BpUuXqjvvvFNt3rxZzZ8/v+yA2rdvr+rVq6fDMbhCIGZQ2CHM9uvTTz/1IqWVHXSZB9A6Jk6cqObMmaNWrlxZMuoagZTY3lRbjFghkrArLOPlsSsNGDDAiyBXrLu8OYnf0rFjR+fzKUehkUAaJKxH6zDbPSLx1SRoHhMmTKg6AjGYCJFknAiCdo8PQ48ePYomhiKOKW9M88URZV4kiI0EkiDW79ChQ3WYTj8nK/vvv78mXTDku2uaW9TzLUQSNaIZqA8tBOOf/RY95JBDVJ8+fbTBNE/EUQi3XyIhjcnzzz+v2P74uSEMceyzzz7q2GOP1YQjaWJ3Rl6IJAMf/Ki6UEoLqaZbsLjEm+DfhZ6tbFU4nXr55ZfVF198URL2unXrqtatW2ttA8LgK8/kG8X6EyKJAsWU6+ADMnz4cDVlyhTP+YkuEdiHI8lqenty0vTPP//oGenevbt2oPvkk0+0u/wff/xRcqbAqlOnTqpDhw4KD16RyhDIFZGgymMXQIjbmte3COM0x7cYA4vlbakmLcTYOAYPHqx++eUX358A7BokVLv55ptzu1Z8gxHywVwRCW9fQyR5+yDxYUEtf/TRR9Xnn39ectpR7WfOnJkbLQSi5AQFsnzrrbe8TAFB1r0YSIOg5q9Mrogkb/lM+BDhu/DNN98ogvSUEj4gp512mmrcuLG+4+GiQJTmyJWtmvkKM5Zhw4Zpn5IVK1bobcull14apjopWwMCQiQRLg80IrYc/fv3r1gj4IN05ZVXel6S5dJMkl2wb9+++ujRNRsIY+PUxPhoQBrlxltumtimgMfTTz/tbW+yEEagXL/z8v+qIRL2znzoeOuZG6y8yXGn3n333fXfeHtxU5O7JThp2bJlyxb9DDc6ed78zs+UI+2BfTOWOo444ghdBQsa4WgS+fnnn/V3nuFvtLlmzZqyN2sxCOI4xjhctP+gMY4YMWKnY+lyHyTGzFghS3OKUpPPht/j33Ltyv8rQyBXRGJnWON+A78bGTJkiBo0aFBl6KT8dIsWLTTJEC7woYcecpI8DIRoHE2aNKkRUdzLDWkY4qh0CoRIKkUsmudzRSS8sdhaIIWBf2fMmKEuvvjiaFCLqRa0IC7Ide3a1bntih9ITJAfNEHjn2F/91NHuWfwAfnrr788TVBCLZZDLJr/Vw2RANeqVavUokWL1EEHHaTD8TVv3lwbMvmdq95ffvml/jsLnUtrtvz5559qw4YN3vbE/p1tCbc7TcpQynHvgvoRczvU/p03508//aSaNm2qt1c4UWEjybugmcS5LbMv7WF7YWtUqVCOmCKQ3LXXXltp8ap8vqqIJO4ZtqO4QTrVfv8ibryL1V+/fn3PBhPU2GpvkWUe/c1irogk7Q9yTVsrf9MhT4VFIAobicxj5bOQKyJJO3m0LMDKF2DUJWq6a+O3LZlHv0j9/3NCJJVjVrKErRITFIijWpFkEcBgbYItf/fdd9pJr1IRIqkUMaWESCrHrGSJvHnWRghNYlXZl/Ywcjdr1qzitjGy4m+ELF68OJcnaBWDUqZAbogES3urVq30cLHU1xSLNGoQTX1CJHEh679eTuDMxb2gxta0t8j+R5udJ3NDJLhbt23bViOLYxO/Jy0EFDJHwBwblooqnnS/qqm9KPxIhEgqXzFCJJVjVrJEFsgswuE4WVUUfiRCJJVPvRBJ5ZgJkUSIWdRVReFHIkRS+azkhkjsbQW3b/k9abHvk+C9iSetSLIIRBFFXoik8jnLDZF069ZNRwJHcDWfNGlS5WhEUEIWYQQghqgiirw2YeeQeLAffPCBvjMFsVWD5IZIzj//fPXmm2/qOTvvvPPUG2+8kcr82YuQOzREHhdJDgHwNrFZ0zi1IZREw4YN1bp167Txn4Ra1SC5IRJOS8x25q677lIPP/xwKvNnO0QtWLBAhwAQSQaByZMnqyuuuMJrbNmyZd7FyUp6EEYj4dKniTvDlQ3u6lSD5IZIyOOCHwfCd2K2piEXXHCBpw099dRTqnfv3ml0oyrbJN/ujz/+qMcOGSxcuFB/LxRuedcUXsBOt/nxxx/vVPzrr79W5Ajad999vb/j+GZuds+ePVunR0UIemUi2ud9QoRIIp5hgjPffvvtulaSUXEdvVDQnDDG2oGXIu5GVVbXr18/NXLkyMyMXYgkM1PhvyNZ0UimT5+uAw0je++9tyJuiS2292thFDf/o5UnSyFA/uJZs2ZlAiAMv37Sf2aisyE7IRpJSAALixOsCGObETstRmG4wcIobhF3pWqrY2vCtubwww/faQsCIATZJmYuwbOLBVhiK8KJi5GTTjrJ+3nz5s06/i52MPInI+vXr9f1EQyLExr+j8GVLRUniaNHj66KecgNkdjGVsIVkiArLbHtJPRh3rx5Onubfas0LTf+tDBxpV3bWAphmLCNrvQ/rX7mhkg4ajP3a/jA1pQHJgmw0UrQTpAzzzxT38Hp0qWL17TcKk1iFipvwyYS5tDkEa68puoqkRsiOfnkk3WOVwSD54ABA1KdSfsokmNAvkzulrQ8b1MFxJHGbSLhZMekEnGk+6l1MzdEYiKUg2S5t/3GjRt1Thn2seZ40P5e7G/U6/d5M5u2c5T5G3tpCEXiuaa25mtsWIgk2Lzkhkj8OhGNHTtWJ44WCY4A6UHZRpIsLG9iEwm+Ir///nvehhjLeHJBJPb1/XJBjXr16lU1lvRYVsx/lb7++uuZzxMUZPxosyeeeKIuWk1+IEGwssvkgkgIINSjRw89rs6dO6upU6eWxOWVV15RN9xwg/4/aTb5Mj8Xfi/2Pz9/CzspWS8vGknWZyj5/uWCSLIa4tB22ZaIackv7iAtio0kCGo5Cf5s+2dkxVvUJjcxsAZbnGmUqtZLd2GxzoVGYidFKndiExYwP+U5lSEQNV6UCM5xOMmJZB+Bt99+W3Xo0EF3dM899/RSW2S/5+n20HkiwR0Zo5gRY8NIE1Y7v00542+a/ZS2d0WAXETmouXxxx/vpaUQrGpGwHkisVVRcpps27Yt1Tm3T5DoiNynSXU6Km7cTvsqc+cfvlwRCTlNuECVptgpI8XAmuZMVN62ffrHpTzjiVx5TdVXIldEkoZLMxoIWdlwe+ci3vvvv69XETdBV69eLR6sDn2m7C2pfWvboSGk1lUhkhDQ29sYrqyTa9YIwY2GDx8eonYpmiQChSEeyABQLMxAkn1yqS0hkhCzVWgPMVW1aNFCEUlcxB0EbG3k6KOPVoRUFPGPgBCJf6yKPmnbRHhATmlCAppScVKYvPTSS7p1Akib1CYpdce5ZoVIQkwZfiL4sJhwitwoXrlypRc9K0TVUjRhBLISqjPhYUfWnBBJCCg5JTKZ7001YqQLAWhERdlyjhgxQt+7wi/EjwiR+EGp9DNCJAHxs/fUdhX4IWCok3gjAYENUQyDKYTAMa4R8sr4mQtSSDzyyCO62B133KGGDRsWoifVV1SIJMCcs6Vp3Lixl9Gtb9++imv1HPciopUEADVEEeYDAinM98wdpyVLlvg6fTnnnHO8rHjt2rVT5KcR8Y9AroikXr16ikjfcctNN92kxowZo5sxTnC2M5NoJXHPwP/VD4GwhYFAzL0m0zLOgFyc9HuEm5Xc0ckgF30rzhMJb45zzz1XI5NE1G9SFbRp08aLYzJu3DhvH26HexStJPrFatcIcaOFFHqf4hQIgdjZ8vz0RGwkflAq/YzzRMLbiAjtCCkfSP0Ql0AiF110kff2K4ygRUAlEyletJJ4ZgFDKvPNlsUWXNpZC0GzF959991q6NChusoockejIfmxzcSDUvK1Ok8kttHT1g6ihrKQRLhi/swzz+xyKmDHRhGtJLpZgDggEJNyxNSMHQQC8Xs6U6pHUaYzMS+Uli1bKtYk3/MuzhOJfVszLrfmV199VefxNcmSaHPGjBnq9NNP32V9zJw5U2stiGgl4T8+xU5iqBUCIcYLX1G8+UkMbi583nPPPWrIkCGBO2+/TOgbWy3uYuVZnCYS3lIEEELiuq3Jgho8eLBnE6mJROgHFn87OZfcAA728SmlgVAbmKKFREEg1Gevoygix9tbXDN6NGe0k6j6HAzV+Eo5TSS2fSTqDyxHuSTdsh3OsInMnTu3qCZipsiO1mb+RuIuE5k8vqnMR81sX5588klFDt5CwcGMOfd7EuMXEbQaTn8MSdl+KH7rKHwOcmK7xc1wI5AIoUArNQQH7UOS5ZwmkrjsIxjKDj744J0yyaNKEzLRRKsvNUnFiIQt0Pz585OcVyfbYutI+IVCEkHbZLtx4403xjIuO5FZ1DF/bZIynedv2M/ypJ04TSRx2Ucuv/xyNWXKFG/RVqLtFCOSpk2bqq+++iqWD0GeKu3YsaOaNWtWySFdddVV6oUXXoh0yHaEPe5Kbd++PdL6qQwDMS89MjwayZsh1lkiics+gk1k0KBBgUiEQsXu37BFWrRoUeQLNG8VNmjQQK1fv77ksOKwg9lEgtZZ6NgWFcbUy1Zn2rRpO1XJVi0PhlhniQR/DZMIq3Xr1mrhwoWh5hx1k6C/9kIql2yrWIPEjS1UzTlanDNnTqj+VUNhto6o/NiiiAliCylZb7nlFm1oDSq8fNiaYqOgLSTp9BMQB6c4tnZCf9hSubzVcZZImjdvrlasWKEXA3ddnnjiiaDrS6uefNgLF+6vv/5a8eTiXbt169ad6gpCSIEHIwVLIlAseBHGUOPnEdfWprBDHGnTF9sQizb04IMPqn79+jk5g04SCVpD/fr1PcD93vAsNUPUx0kAbwlCJjZp0kSNHDlSkY6gUikMdER57nFMnDix0qrk+YgRsO9DUTV+R+3bt/eioaH1kN4kKbGTqJk2sc+RVtY1cZJI7AURVUQyyIQ3RVgvxGLGVtnaZOdjYWuy+HX07NnTM7CS0/jDDz9MtLNow127dvWc4XiRrVmzJtE+RNGYk0RiJzHKWha7YkQi8S2iWKrR1FF4Nwu7ibkxvnbtWoVGmbRMnz5dderUSTd74YUXaq9p18RJIrGPfbOQotOe9GJE0qtXLzVq1CjX1kYu+2uf9hUOkP+h4aYhOMQRpnPgwIEKd33XxDkisQ2jcRwHhp3AunXrendyTF1Y5W23+bBtSPlwCNSpU6doRsa0NJJwo8lGaeeIpHv37mrSpEkaPc7fC6NipQ1r7dq1d3FqIkZG4a3VtPtZ7e336dNHjR071iMUDOxoBCLBEHCKSDCINmrUSG3ZskWPdsKECQpiyZJwmlTo1NSsWTO1fPnyLHVT+iIIRIqAU0Ri31tI6sy/UrSL2UhEI6kURXneNQScIZJCIxnpMEmLmTWxL4CZvh133HFq6dKlWeuq9EcQiAwBZ4gE/w7jCZjlN3wxG8kpp5yiPvroo8gmTSoSBLKGgBNEYp/9A2BckdCimJzevXt7R714ShKOgBgm2ElEBIG8IpB5IsHbFFf1TZs26TlwIQ4qrs/kjr333nvV1Vdfnde1I+MSBDwEMk8k3AI1x3JZ9BuRtSQICAJKZZ5IbJsDQZgvu+wymTdBQBDIGAKZJxJsDOvWrdOwiedhxlaPdEcQ+A+BzBOJ7ZchRCLrVhDIJgJCJNmcF+mVIOAUAv8DAKUcNZiac5AAAAAASUVORK5CYII=', '2025-12-21 19:57:47', 'data:image/png;base64,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', '2025-12-21 20:00:38', '686db5978143a4757461fc4a22db5bc5208934bd33a49e3453397229cf4afdf0', 'approved', 19, '合同终止原因：111', '2025-12-21 20:01:34', 'terminated', 'contracts\\contract_CONTRACT_20251221_F2D98736.pdf', '2025-12-21 18:26:56', '2025-12-21 19:41:55', NULL, NULL, NULL, 'none');
INSERT INTO `rental_contracts` VALUES (2, 'CONTRACT_20251221_A2B681FB', 1, 24, 27, 1, '房屋租赁合同\n\n甲方（出租方）：%s\n乙方（承租方）：%s\n\n根据《中华人民共和国合同法》及相关法律法规，甲乙双方在平等、自愿、公平和诚实信用的基础上，\n就乙方承租甲方房屋事宜，为明确双方权利义务关系，经协商一致，订立本合同。\n\n第一条 房屋基本情况\n甲方房屋位于：%s\n房屋类型：%s\n建筑面积：%.2f平方米\n房屋朝向：%s\n装修情况：%s\n\n第二条 租赁期限\n租赁期限自%s至%s，共计%d个月。\n\n第三条 租金\n月租金为人民币%s元（大写：%s）。\n租金支付方式：%s\n\n第四条 押金\n乙方应于签订本合同时向甲方支付房屋押金，押金数额为%s元。\n\n第五条 甲方权利和义务\n1. 甲方应当按时将房屋及附属设施交付乙方使用。\n2. 甲方保证房屋的建筑结构和设备设施符合建筑、消防、治安、卫生等方面的安全条件。\n3. 租赁期间，甲方不得随意进入出租房屋。\n\n第六条 乙方权利和义务\n1. 乙方应当按照约定的用途使用房屋，不得擅自改变使用用途。\n2. 乙方应当爱护并合理使用房屋及其附属设施。\n3. 乙方应当按时支付租金。\n\n第七条 补充条款\n%s\n\n第八条 违约责任\n甲乙双方应当严格履行本合同约定的义务，如一方违约，应承担相应的违约责任。\n\n第九条 争议解决\n本合同履行过程中发生的争议，由双方协商解决；协商不成的，可向房屋所在地人民法院起诉。\n\n第十条 合同生效\n本合同自甲乙双方签字盖章后生效。\n\n甲方（出租方）签名：_________________ 日期：_________\n\n乙方（承租方）签名：_________________ 日期：_________\n\n小区管理员审核：_________________ 日期：_________\n', '', 6500.00, 13000.00, '押一付三', '2025-12-21', '2026-12-20', 'data:image/png;base64,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', '2025-12-21 21:06:19', 'data:image/png;base64,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', '2025-12-21 21:07:00', '1f8973cc7e4e6b4bc0a1f79f0bf0bcca94a6761b66cc93ba017b0536044a83b9', 'approved', 19, '解约审核通过：同意解约', '2025-12-21 21:09:05', 'terminated', 'contracts\\contract_CONTRACT_20251221_A2B681FB.pdf', '2025-12-21 20:58:29', '2025-12-21 20:58:29', '11', 27, '2025-12-21 21:08:51', 'approved');
INSERT INTO `rental_contracts` VALUES (3, 'CONTRACT_20251221_A18A5230', 1, 24, 27, 1, '房屋租赁合同\n\n甲方（出租方）：%s\n乙方（承租方）：%s\n\n根据《中华人民共和国合同法》及相关法律法规，甲乙双方在平等、自愿、公平和诚实信用的基础上，\n就乙方承租甲方房屋事宜，为明确双方权利义务关系，经协商一致，订立本合同。\n\n第一条 房屋基本情况\n甲方房屋位于：%s\n房屋类型：%s\n建筑面积：%.2f平方米\n房屋朝向：%s\n装修情况：%s\n\n第二条 租赁期限\n租赁期限自%s至%s，共计%d个月。\n\n第三条 租金\n月租金为人民币%s元（大写：%s）。\n租金支付方式：%s\n\n第四条 押金\n乙方应于签订本合同时向甲方支付房屋押金，押金数额为%s元。\n\n第五条 甲方权利和义务\n1. 甲方应当按时将房屋及附属设施交付乙方使用。\n2. 甲方保证房屋的建筑结构和设备设施符合建筑、消防、治安、卫生等方面的安全条件。\n3. 租赁期间，甲方不得随意进入出租房屋。\n\n第六条 乙方权利和义务\n1. 乙方应当按照约定的用途使用房屋，不得擅自改变使用用途。\n2. 乙方应当爱护并合理使用房屋及其附属设施。\n3. 乙方应当按时支付租金。\n\n第七条 补充条款\n%s\n\n第八条 违约责任\n甲乙双方应当严格履行本合同约定的义务，如一方违约，应承担相应的违约责任。\n\n第九条 争议解决\n本合同履行过程中发生的争议，由双方协商解决；协商不成的，可向房屋所在地人民法院起诉。\n\n第十条 合同生效\n本合同自甲乙双方签字盖章后生效。\n\n甲方（出租方）签名：_________________ 日期：_________\n\n乙方（承租方）签名：_________________ 日期：_________\n\n小区管理员审核：_________________ 日期：_________\n', '', 6500.00, 13000.00, '押一付三', '2025-12-21', '2026-12-20', 'data:image/png;base64,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', '2025-12-21 22:50:09', 'data:image/png;base64,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', '2025-12-21 22:50:33', '4ef84eac6846726595623dcd62d3c71fb66e75dfbea6c197e2f03c0a3a61d266', 'approved', 19, '合同条款合规，双方信息完整，审核通过', '2025-12-21 22:50:43', 'effective', 'contracts\\contract_CONTRACT_20251221_A18A5230.pdf', '2025-12-21 22:49:43', '2025-12-21 22:49:43', NULL, NULL, NULL, 'none');

-- ----------------------------
-- Table structure for rental_transactions
-- ----------------------------
DROP TABLE IF EXISTS `rental_transactions`;
CREATE TABLE `rental_transactions`  (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `contract_id` bigint NOT NULL COMMENT '关联合同ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `landlord_id` bigint NOT NULL COMMENT '房东ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `community_id` bigint NOT NULL COMMENT '小区ID',
  `status` enum('pending_sign','signed','pending_checkin','living','pending_complete','completed','evaluated','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending_sign' COMMENT '交易状态：pending_sign-待签署,signed-已签署待入住,pending_checkin-入住确认中,living-在租中,pending_complete-完成确认中,completed-已完成,evaluated-已评价,cancelled-已取消',
  `landlord_checkin_confirm` tinyint(1) NULL DEFAULT 0 COMMENT '房东确认入住',
  `landlord_checkin_time` timestamp NULL DEFAULT NULL COMMENT '房东确认入住时间',
  `tenant_checkin_confirm` tinyint(1) NULL DEFAULT 0 COMMENT '租客确认入住',
  `tenant_checkin_time` timestamp NULL DEFAULT NULL COMMENT '租客确认入住时间',
  `checkin_date` date NULL DEFAULT NULL COMMENT '实际入住日期',
  `checkin_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '入住备注（房屋交接情况）',
  `landlord_complete_confirm` tinyint(1) NULL DEFAULT 0 COMMENT '房东确认交易完成',
  `landlord_complete_time` timestamp NULL DEFAULT NULL COMMENT '房东确认完成时间',
  `tenant_complete_confirm` tinyint(1) NULL DEFAULT 0 COMMENT '租客确认交易完成',
  `tenant_complete_time` timestamp NULL DEFAULT NULL COMMENT '租客确认完成时间',
  `checkout_date` date NULL DEFAULT NULL COMMENT '实际退租日期',
  `checkout_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '退租备注',
  `landlord_evaluated` tinyint(1) NULL DEFAULT 0 COMMENT '房东已评价租客',
  `tenant_evaluated` tinyint(1) NULL DEFAULT 0 COMMENT '租客已评价房东',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`transaction_id`) USING BTREE,
  UNIQUE INDEX `uk_contract`(`contract_id` ASC) USING BTREE,
  INDEX `idx_house`(`house_id` ASC) USING BTREE,
  INDEX `idx_landlord`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `rental_transactions_ibfk_5`(`community_id` ASC) USING BTREE,
  CONSTRAINT `rental_transactions_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `rental_contracts` (`contract_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_transactions_ibfk_2` FOREIGN KEY (`house_id`) REFERENCES `houses` (`house_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_transactions_ibfk_3` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_transactions_ibfk_4` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `rental_transactions_ibfk_5` FOREIGN KEY (`community_id`) REFERENCES `communities` (`community_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租赁交易表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rental_transactions
-- ----------------------------
INSERT INTO `rental_transactions` VALUES (1, 1, 5, 24, 27, 1, 'pending_checkin', 0, NULL, 1, '2025-12-21 21:09:18', '2025-12-21', NULL, 0, NULL, 0, NULL, NULL, NULL, 0, 0, '2025-12-21 19:38:34', '2025-12-21 19:38:34');
INSERT INTO `rental_transactions` VALUES (2, 2, 1, 24, 27, 1, 'completed', 0, NULL, 1, '2025-12-21 21:09:26', '2025-12-21', NULL, 0, NULL, 0, NULL, NULL, NULL, 0, 1, '2025-12-21 21:07:22', '2025-12-21 21:07:22');
INSERT INTO `rental_transactions` VALUES (3, 3, 1, 24, 27, 1, 'living', 1, '2025-12-21 22:51:47', 1, '2025-12-21 22:51:18', '2025-12-21', NULL, 0, NULL, 0, NULL, NULL, NULL, 0, 0, '2025-12-21 22:50:43', '2025-12-21 22:50:43');

-- ----------------------------
-- Table structure for reports
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports`  (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `report_type` enum('house','user','facility','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报类型',
  `target_id` bigint NOT NULL COMMENT '被举报对象ID',
  `reason_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报原因类型',
  `reason_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '举报详细理由',
  `evidence_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '证据图片(JSON数组)',
  `status` enum('pending','processing','handled','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '处理状态',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理结果',
  `handle_time` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_reporter`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_target`(`report_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of reports
-- ----------------------------
INSERT INTO `reports` VALUES (1, 27, 'house', 6, '虚假房源', '', NULL, 'handled', 19, '11', '2025-12-21 15:57:53', '2025-12-21 15:16:57');

-- ----------------------------
-- Table structure for rules_documents
-- ----------------------------
DROP TABLE IF EXISTS `rules_documents`;
CREATE TABLE `rules_documents`  (
  `document_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档标题',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件路径(MinIO)',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型: pdf/doc/docx',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文档描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '状态: pending/active/archived',
  `uploader_id` bigint NULL DEFAULT NULL COMMENT '上传者ID',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0' COMMENT '版本号',
  `effective_date` datetime NULL DEFAULT NULL COMMENT '生效日期',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`document_id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租赁条例文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rules_documents
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_word_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word_whitelist`;
CREATE TABLE `sensitive_word_whitelist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '白名单词',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加入白名单原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词白名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensitive_word_whitelist
-- ----------------------------
INSERT INTO `sensitive_word_whitelist` VALUES (1, '押金', '租房正常用语', 'active', '2025-12-22 18:21:49');
INSERT INTO `sensitive_word_whitelist` VALUES (2, '房租', '租房正常用语', 'active', '2025-12-22 18:21:49');
INSERT INTO `sensitive_word_whitelist` VALUES (3, '中介费', '租房正常用语', 'active', '2025-12-22 18:21:49');
INSERT INTO `sensitive_word_whitelist` VALUES (4, '合同', '租房正常用语', 'active', '2025-12-22 18:21:49');
INSERT INTO `sensitive_word_whitelist` VALUES (5, '定金', '租房正常用语-需要结合上下文判断', 'active', '2025-12-22 18:21:49');

-- ----------------------------
-- Table structure for sensitive_words
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words`  (
  `word_id` bigint NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '敏感词',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'default' COMMENT '分类：default-默认, spam-垃圾信息, abuse-辱骂, fraud-欺诈, illegal-违法, porn-色情, ad-广告',
  `level` int NULL DEFAULT 1 COMMENT '级别：1-轻微, 2-一般, 3-严重, 4-特别严重',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`word_id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensitive_words
-- ----------------------------
INSERT INTO `sensitive_words` VALUES (1, '假房源', 'fraud', 3, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (2, '骗子', 'fraud', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (3, '先付款', 'fraud', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (4, '转账', 'fraud', 1, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (5, '定金诈骗', 'fraud', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (6, '假中介', 'fraud', 3, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (7, '傻逼', 'abuse', 3, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (8, '狗东西', 'abuse', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (9, '废物', 'abuse', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (10, '垃圾', 'abuse', 1, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (11, '滚蛋', 'abuse', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (12, '加微信', 'ad', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (13, '加QQ', 'ad', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (14, '优惠券', 'ad', 1, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (15, '免费领取', 'ad', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (16, '扫码关注', 'ad', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (17, '赌博', 'illegal', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (18, '毒品', 'illegal', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (19, '枪支', 'illegal', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (20, '洗钱', 'illegal', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (21, '色情', 'porn', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (22, '约炮', 'porn', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (23, '援交', 'porn', 4, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (24, '刷单', 'spam', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (25, '兼职日结', 'spam', 2, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');
INSERT INTO `sensitive_words` VALUES (26, '高薪招聘', 'spam', 1, 'active', '2025-12-22 18:21:49', '2025-12-22 18:21:49');

-- ----------------------------
-- Table structure for system_notifications
-- ----------------------------
DROP TABLE IF EXISTS `system_notifications`;
CREATE TABLE `system_notifications`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `notification_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型(审核结果、举报处理、合同提醒等)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '通知内容',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联对象类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` timestamp NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_type`(`notification_type` ASC) USING BTREE,
  CONSTRAINT `system_notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_notifications
-- ----------------------------
INSERT INTO `system_notifications` VALUES (1, 24, 'house_approved', '房源审核通过', '房源信息完整，符合发布要求，审核通过', 'house', 6, 1, '2025-12-21 17:21:09', '2025-12-20 22:35:03');
INSERT INTO `system_notifications` VALUES (2, 24, 'house_approved', '房源审核通过', '房源信息完整，符合发布要求，审核通过', 'house', 5, 1, '2025-12-21 17:21:09', '2025-12-21 15:23:33');
INSERT INTO `system_notifications` VALUES (3, 27, 'report_handled', '举报处理结果通知', '您举报的房源【11】（举报类型：虚假房源）已处理完成。处理结果：11', 'report', 1, 0, NULL, '2025-12-21 15:57:53');
INSERT INTO `system_notifications` VALUES (4, 24, 'house_rejected', '房源审核被拒', '2', 'house', 6, 1, '2025-12-21 16:45:05', '2025-12-21 16:11:52');
INSERT INTO `system_notifications` VALUES (5, 24, 'feedback_processed', '配套反馈已处理', '您在阳光花园提交的配套信息反馈【望京地铁站】（反馈内容：111111111）已处理完成。处理回复：好的', 'facility_feedback', 4, 1, '2025-12-21 17:20:57', '2025-12-21 17:20:51');
INSERT INTO `system_notifications` VALUES (6, 24, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 1, 0, NULL, '2025-12-21 19:38:34');
INSERT INTO `system_notifications` VALUES (7, 27, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 1, 0, NULL, '2025-12-21 19:38:34');
INSERT INTO `system_notifications` VALUES (8, 24, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 1, 0, NULL, '2025-12-21 20:01:35');
INSERT INTO `system_notifications` VALUES (9, 27, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 1, 0, NULL, '2025-12-21 20:01:35');
INSERT INTO `system_notifications` VALUES (10, 24, 'house_approved', '房源审核通过', '房源信息完整，符合发布要求，审核通过', 'house', 1, 0, NULL, '2025-12-21 20:57:54');
INSERT INTO `system_notifications` VALUES (11, 27, 'contract_created', '新合同待签署', '房东 微信用户 向您发起了一份租赁合同，请及时查看并确认签署', 'contract', 2, 1, '2025-12-21 21:06:35', '2025-12-21 20:58:29');
INSERT INTO `system_notifications` VALUES (12, 24, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 2, 0, NULL, '2025-12-21 21:07:22');
INSERT INTO `system_notifications` VALUES (13, 27, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 2, 0, NULL, '2025-12-21 21:07:22');
INSERT INTO `system_notifications` VALUES (14, 27, 'contract_created', '新合同待签署', '房东 微信用户 向您发起了一份租赁合同，请及时查看并确认签署', 'contract', 3, 1, '2025-12-21 22:50:22', '2025-12-21 22:49:43');
INSERT INTO `system_notifications` VALUES (15, 24, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 3, 0, NULL, '2025-12-21 22:50:43');
INSERT INTO `system_notifications` VALUES (16, 27, 'contract_approved', '合同审核通过', '合同条款合规，双方信息完整，审核通过', 'contract', 3, 1, '2025-12-21 22:50:52', '2025-12-21 22:50:43');
INSERT INTO `system_notifications` VALUES (17, 24, 'checkin_confirmed', '入住确认成功', '租客已入住，租赁交易正式开始，请双方遵守合同约定。', 'transaction', 3, 0, NULL, '2025-12-21 22:51:47');
INSERT INTO `system_notifications` VALUES (18, 27, 'checkin_confirmed', '入住确认成功', '您已确认入住，租赁交易正式开始，请遵守合同约定，如有问题及时与房东沟通。', 'transaction', 3, 0, NULL, '2025-12-21 22:51:47');
INSERT INTO `system_notifications` VALUES (19, 27, 'verification_community_approved', '小区管理员审核通过', '您的小区身份认证已通过小区管理员审核，正在等待平台最终审核', 'verification', 8, 0, NULL, '2025-12-22 15:57:26');
INSERT INTO `system_notifications` VALUES (20, 27, 'verification_approved', '小区认证通过', '恭喜！您的小区身份认证已通过审核，现在可以在小区内发布房源和参与论坛讨论', 'verification', 8, 0, NULL, '2025-12-22 16:04:10');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信OpenID',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录密码',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `user_type` tinyint NULL DEFAULT NULL COMMENT '用户类型: 3-租户, 4-房东',
  `credit_score` decimal(3, 1) NULL DEFAULT 5.0 COMMENT '信用评分(0-5分)',
  `status` enum('active','banned','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '账号状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `openid`(`openid` ASC) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (3, 'wx_openid_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgwxWqn3DgDPDpOa6RQ9Hf8ej.', '张三房东', 'https://example.com/avatar/001.jpg', '13800138001', 4, 4.8, 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `users` VALUES (4, 'wx_openid_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgwxWqn3DgDPDpOa6RQ9Hf8ej.', '李四租客', 'https://example.com/avatar/002.jpg', '13800138002', 3, 4.5, 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `users` VALUES (5, 'wx_openid_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgwxWqn3DgDPDpOa6RQ9Hf8ej.', '王五房东', 'https://example.com/avatar/003.jpg', '13800138003', 4, 4.9, 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `users` VALUES (6, 'wx_openid_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgwxWqn3DgDPDpOa6RQ9Hf8ej.', '赵六租客', 'https://example.com/avatar/004.jpg', '13800138004', 3, 4.3, 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `users` VALUES (7, 'wx_openid_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgwxWqn3DgDPDpOa6RQ9Hf8ej.', '小区管理员', 'https://example.com/avatar/005.jpg', '13800138005', 3, 5.0, 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `users` VALUES (8, 'wx_openid_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgwxWqn3DgDPDpOa6RQ9Hf8ej.', '平台管理员', 'https://example.com/avatar/006.jpg', '13800138006', 3, 5.0, 'active', '2025-12-18 01:56:11', '2025-12-18 01:56:11');
INSERT INTO `users` VALUES (18, 'admin_16677250163', '$2a$10$HLqM5YdP4a9/rzYbG3Z78eHCLzXyuQzLlR6/kZjgvpND2pgQnUcjC', '管理员_16677250163', NULL, '16677250163', 0, 5.0, 'active', '2025-12-18 02:56:20', '2025-12-18 02:56:20');
INSERT INTO `users` VALUES (19, 'admin_15677410862', '$2a$10$R0aWV71hq6M9F1LZNHGHHesNKL5pldSthmeAn8iNM5YlJPHW74AsS', '管理员_15677410862', NULL, '15677410862', 0, 5.0, 'active', '2025-12-18 03:00:17', '2025-12-18 03:00:17');
INSERT INTO `users` VALUES (24, 'ooFEU7agJ8QzYUIPVN8MKHh8Gu6Y', NULL, '响离', 'https://thirdwx.qlogo.cn/mmopen/vi_32/1zIbWib7NugBPIBE6cPAj10Nl9N8UPZa9lmgibDKib0ll8LTviaKLSEM1bSnKtXVHMdWF0LKoHLTtY0lg900Wr8aicb30ou5iaibd5pxBfpeulUuRM/132', NULL, 4, 3.0, 'active', '2025-12-19 13:56:15', '2025-12-22 00:20:43');
INSERT INTO `users` VALUES (25, 'wx_test_openid_025', NULL, '李房东', 'https://example.com/avatars/user25.jpg', '13900139000', 4, 95.5, 'active', '2025-11-01 10:00:00', '2025-12-19 15:10:24');
INSERT INTO `users` VALUES (26, 'wx_test_openid_026', NULL, '王业主', 'https://example.com/avatars/user26.jpg', '13700137000', 4, 92.0, 'active', '2025-10-15 14:00:00', '2025-12-19 15:10:24');
INSERT INTO `users` VALUES (27, 'wx_test_openid_027', NULL, '赵', 'http://172.31.14.194:9000/avatars/3feeb1986d7e4c51a4ad4d839408bb00.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20251221%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251221T165953Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f1721b8e9bd3bb86f2f56ab06af7fa16dc53bc632b947894578e777c44a87f58', '13600136000', 3, 88.0, 'active', '2025-12-01 09:00:00', '2025-12-22 00:59:53');
INSERT INTO `users` VALUES (28, 'wx_test_openid_028', NULL, '钱小姐', 'https://example.com/avatars/user28.jpg', '13500135000', 3, 90.5, 'active', '2025-11-20 16:00:00', '2025-12-19 15:10:24');
INSERT INTO `users` VALUES (29, 'wx_test_openid_029', NULL, '孙先生', 'https://example.com/avatars/user29.jpg', '13400134000', 3, 85.0, 'active', '2025-12-05 11:00:00', '2025-12-19 15:10:24');

-- ----------------------------
-- Table structure for violations
-- ----------------------------
DROP TABLE IF EXISTS `violations`;
CREATE TABLE `violations`  (
  `violation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '违规ID',
  `user_id` bigint NOT NULL COMMENT '违规用户ID',
  `message_id` bigint NULL DEFAULT NULL COMMENT '关联消息ID',
  `violation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '违规类型: spam/harassment/fraud/illegal_content/fake_info/privacy_leak/malicious_action/other',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '违规内容',
  `ai_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI分析结果JSON',
  `severity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '严重程度: low/medium/high/critical',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '处理状态: pending/processing/resolved/dismissed/auto_processed',
  `action_taken` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采取的措施: warning/mute/ban/none/auto_ban/auto_mute',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handler_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理意见',
  `ban_until` datetime NULL DEFAULT NULL COMMENT '封禁/禁言截止时间',
  `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`violation_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_type`(`violation_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '违规记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of violations
-- ----------------------------
INSERT INTO `violations` VALUES (1, 1, NULL, 'spam', '加微信xxx，免费领取优惠券', '{\"isViolation\": true, \"violationType\": \"spam\", \"severity\": \"low\", \"reason\": \"包含推广信息\"}', 'low', 'pending', NULL, NULL, NULL, NULL, NULL, '2025-12-22 17:57:01', '2025-12-22 17:57:01');
INSERT INTO `violations` VALUES (2, 2, NULL, 'harassment', '你这个骗子，滚出去', '{\"isViolation\": true, \"violationType\": \"harassment\", \"severity\": \"medium\", \"reason\": \"包含辱骂性言论\"}', 'medium', 'pending', NULL, NULL, NULL, NULL, NULL, '2025-12-22 17:57:01', '2025-12-22 17:57:01');
INSERT INTO `violations` VALUES (3, 3, NULL, 'fraud', '这套房子只要500元/月，押金只要100', '{\"isViolation\": true, \"violationType\": \"fraud\", \"severity\": \"high\", \"reason\": \"价格明显低于市场价，疑似欺诈\"}', 'high', 'pending', NULL, NULL, NULL, NULL, NULL, '2025-12-22 17:57:01', '2025-12-22 17:57:01');
INSERT INTO `violations` VALUES (4, 27, 12, 'harassment', '去你妈的', '{\n    \"isViolation\": true,\n    \"violationType\": \"harassment\",\n    \"severity\": \"medium\",\n    \"reason\": \"消息包含辱骂性语言（\'去你妈的\'），属于人身攻击和不文明用语，违反了社区准则和网络文明规范。\",\n    \"suggestion\": \"建议对该用户进行警告处理，首次违规可给予提醒，多次出现类似行为应考虑限制发言或封禁账号。\"\n}', 'medium', 'resolved', 'warning', 18, '111', NULL, '2025-12-22 18:50:42', '2025-12-22 18:50:00', '2025-12-22 18:50:00');
INSERT INTO `violations` VALUES (5, 27, 11, 'harassment', '我操你妈个逼', '{\n    \"isViolation\": true,\n    \"violationType\": \"harassment\",\n    \"severity\": \"high\",\n    \"reason\": \"消息包含严重的人身攻击和侮辱性语言，属于明显的骚扰行为。\",\n    \"suggestion\": \"建议立即删除该消息，并对发送者进行警告或限制发言处理。\"\n}', 'high', 'resolved', 'warning', 18, '乱搞什么', NULL, '2025-12-22 19:20:11', '2025-12-22 18:50:02', '2025-12-22 18:50:02');
INSERT INTO `violations` VALUES (6, 27, 13, 'harassment', '操你妈', '{\n    \"isViolation\": true,\n    \"violationType\": \"harassment\",\n    \"severity\": \"high\",\n    \"reason\": \"消息包含明显的辱骂性语言（\'操你妈\'），属于人身攻击和恶意辱骂，违反了平台关于文明交流的规定。\",\n    \"suggestion\": \"建议对该用户进行警告或短期禁言处理，若屡次发生类似行为，可考虑延长禁言时间或封号处理。\"\n}', 'high', 'resolved', 'warning', 18, '叫你妈', NULL, '2025-12-22 19:43:09', '2025-12-22 19:42:48', '2025-12-22 19:42:48');

-- ----------------------------
-- Table structure for websocket_sessions
-- ----------------------------
DROP TABLE IF EXISTS `websocket_sessions`;
CREATE TABLE `websocket_sessions`  (
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'WebSocket会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `server_node` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务节点(集群支持)',
  `status` enum('online','offline') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'online' COMMENT '在线状态',
  `last_heartbeat` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后心跳时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`session_id`) USING BTREE,
  UNIQUE INDEX `uk_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `websocket_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'WebSocket会话管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of websocket_sessions
-- ----------------------------
INSERT INTO `websocket_sessions` VALUES ('1db98a12-c691-4fec-ae3d-2bcf5475e34d', 27, NULL, 'offline', '2025-12-22 19:42:35', '2025-12-22 19:42:35', '2025-12-22 19:57:19');
INSERT INTO `websocket_sessions` VALUES ('d1a2d5be-0487-43d0-96fa-b6db839d89a2', 24, NULL, 'offline', '2025-12-22 00:34:54', '2025-12-22 00:34:54', '2025-12-22 00:49:31');

-- ----------------------------
-- View structure for v_house_details
-- ----------------------------
DROP VIEW IF EXISTS `v_house_details`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_house_details` AS select `h`.`house_id` AS `house_id`,`h`.`landlord_id` AS `landlord_id`,`h`.`community_id` AS `community_id`,`h`.`title` AS `title`,`h`.`house_type` AS `house_type`,`h`.`area` AS `area`,`h`.`floor` AS `floor`,`h`.`total_floor` AS `total_floor`,`h`.`orientation` AS `orientation`,`h`.`decoration` AS `decoration`,`h`.`rent_price` AS `rent_price`,`h`.`payment_method` AS `payment_method`,`h`.`rent_period` AS `rent_period`,`h`.`description` AS `description`,`h`.`images` AS `images`,`h`.`contact_phone` AS `contact_phone`,`h`.`facilities` AS `facilities`,`h`.`audit_status` AS `audit_status`,`h`.`auditor_id` AS `auditor_id`,`h`.`audit_opinion` AS `audit_opinion`,`h`.`audit_time` AS `audit_time`,`h`.`publish_status` AS `publish_status`,`h`.`view_count` AS `view_count`,`h`.`favorite_count` AS `favorite_count`,`h`.`created_at` AS `created_at`,`h`.`updated_at` AS `updated_at`,`c`.`community_name` AS `community_name`,`c`.`address` AS `community_address`,`u`.`nickname` AS `landlord_nickname`,`u`.`phone` AS `landlord_phone`,`u`.`credit_score` AS `landlord_credit_score` from ((`houses` `h` left join `communities` `c` on((`h`.`community_id` = `c`.`community_id`))) left join `users` `u` on((`h`.`landlord_id` = `u`.`user_id`)));

SET FOREIGN_KEY_CHECKS = 1;
