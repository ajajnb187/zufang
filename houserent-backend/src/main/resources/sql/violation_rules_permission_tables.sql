-- 违规记录表
CREATE TABLE IF NOT EXISTS violations (
    violation_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '违规ID',
    user_id BIGINT NOT NULL COMMENT '违规用户ID',
    message_id BIGINT COMMENT '关联消息ID',
    violation_type VARCHAR(50) NOT NULL COMMENT '违规类型: spam/harassment/fraud/illegal_content/fake_info/privacy_leak/malicious_action/other',
    content TEXT COMMENT '违规内容',
    ai_analysis TEXT COMMENT 'AI分析结果JSON',
    severity VARCHAR(20) DEFAULT 'medium' COMMENT '严重程度: low/medium/high/critical',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '处理状态: pending/processing/resolved/dismissed/auto_processed',
    action_taken VARCHAR(50) COMMENT '采取的措施: warning/mute/ban/none/auto_ban/auto_mute',
    handler_id BIGINT COMMENT '处理人ID',
    handler_opinion TEXT COMMENT '处理意见',
    ban_until DATETIME COMMENT '封禁/禁言截止时间',
    handled_at DATETIME COMMENT '处理时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_type (violation_type),
    INDEX idx_created_at (created_at),
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

-- 管理员权限配置表
CREATE TABLE IF NOT EXISTS admin_permissions (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限配置ID',
    admin_id BIGINT NOT NULL UNIQUE COMMENT '管理员ID',
    max_house_audit INT DEFAULT 50 COMMENT '每日房源审核上限',
    max_user_audit INT DEFAULT 30 COMMENT '每日用户审核上限',
    max_contract_audit INT DEFAULT 20 COMMENT '每日合同审核上限',
    can_manage_facilities TINYINT(1) DEFAULT 1 COMMENT '是否可管理配套设施',
    can_handle_reports TINYINT(1) DEFAULT 1 COMMENT '是否可处理举报',
    can_handle_violations TINYINT(1) DEFAULT 1 COMMENT '是否可处理违规',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/inactive',
    remarks TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员权限配置表';

-- 租赁条例文档表
CREATE TABLE IF NOT EXISTS rules_documents (
    document_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径(MinIO)',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_type VARCHAR(20) COMMENT '文件类型: pdf/doc/docx',
    description TEXT COMMENT '文档描述',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/active/archived',
    uploader_id BIGINT COMMENT '上传者ID',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    effective_date DATETIME COMMENT '生效日期',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁条例文档表';

-- 插入测试数据
INSERT INTO violations (user_id, violation_type, content, ai_analysis, severity, status, created_at) VALUES
(1, 'spam', '加微信xxx，免费领取优惠券', '{"isViolation": true, "violationType": "spam", "severity": "low", "reason": "包含推广信息"}', 'low', 'pending', NOW()),
(2, 'harassment', '你这个骗子，滚出去', '{"isViolation": true, "violationType": "harassment", "severity": "medium", "reason": "包含辱骂性言论"}', 'medium', 'pending', NOW()),
(3, 'fraud', '这套房子只要500元/月，押金只要100', '{"isViolation": true, "violationType": "fraud", "severity": "high", "reason": "价格明显低于市场价，疑似欺诈"}', 'high', 'pending', NOW());
