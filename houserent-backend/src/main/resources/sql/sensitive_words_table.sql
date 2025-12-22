-- 敏感词表
-- 用于存储自定义敏感词，支持动态加载

CREATE TABLE IF NOT EXISTS `sensitive_words` (
    `word_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '敏感词ID',
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT 'default' COMMENT '分类：default-默认, spam-垃圾信息, abuse-辱骂, fraud-欺诈, illegal-违法, porn-色情, ad-广告',
    `level` INT DEFAULT 1 COMMENT '级别：1-轻微, 2-一般, 3-严重, 4-特别严重',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_word` (`word`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 白名单词表（允许的词，不会被过滤）
CREATE TABLE IF NOT EXISTS `sensitive_word_whitelist` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `word` VARCHAR(100) NOT NULL COMMENT '白名单词',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '加入白名单原因',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词白名单表';

-- 修改chat_messages表，添加敏感词相关字段
ALTER TABLE `chat_messages` 
ADD COLUMN `filtered_content` TEXT DEFAULT NULL COMMENT '过滤后的内容（用于显示）' AFTER `content`,
ADD COLUMN `has_sensitive` TINYINT(1) DEFAULT 0 COMMENT '是否包含敏感词' AFTER `filtered_content`,
ADD COLUMN `sensitive_words` VARCHAR(500) DEFAULT NULL COMMENT '检测到的敏感词列表（JSON数组）' AFTER `has_sensitive`;

-- 插入一些基础敏感词（示例）
INSERT INTO `sensitive_words` (`word`, `category`, `level`) VALUES
-- 欺诈类
('假房源', 'fraud', 3),
('骗子', 'fraud', 2),
('先付款', 'fraud', 2),
('转账', 'fraud', 1),
('定金诈骗', 'fraud', 4),
('假中介', 'fraud', 3),
-- 辱骂类
('傻逼', 'abuse', 3),
('狗东西', 'abuse', 2),
('废物', 'abuse', 2),
('垃圾', 'abuse', 1),
('滚蛋', 'abuse', 2),
-- 广告类
('加微信', 'ad', 2),
('加QQ', 'ad', 2),
('优惠券', 'ad', 1),
('免费领取', 'ad', 2),
('扫码关注', 'ad', 2),
-- 违法类
('赌博', 'illegal', 4),
('毒品', 'illegal', 4),
('枪支', 'illegal', 4),
('洗钱', 'illegal', 4),
-- 色情类
('色情', 'porn', 4),
('约炮', 'porn', 4),
('援交', 'porn', 4),
-- 垃圾信息
('刷单', 'spam', 2),
('兼职日结', 'spam', 2),
('高薪招聘', 'spam', 1)
ON DUPLICATE KEY UPDATE `updated_at` = CURRENT_TIMESTAMP;

-- 插入一些白名单词（租房相关的正常用语）
INSERT INTO `sensitive_word_whitelist` (`word`, `reason`) VALUES
('押金', '租房正常用语'),
('房租', '租房正常用语'),
('中介费', '租房正常用语'),
('合同', '租房正常用语'),
('定金', '租房正常用语-需要结合上下文判断')
ON DUPLICATE KEY UPDATE `reason` = VALUES(`reason`);
