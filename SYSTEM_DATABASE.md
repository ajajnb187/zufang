# 小区房屋租赁管理平台 - 数据库设计

## 1. 数据库概览

- **数据库**: MySQL
- **数据库名**: houserent_system
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## 2. 核心数据表

### 2.1 用户表 (user)
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 主键 |
| openid | VARCHAR(100) | 微信openid |
| phone | VARCHAR(20) | 手机号 |
| password | VARCHAR(255) | 密码(BCrypt) |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| user_type | INT | 0管理员/3租客/4房东 |
| status | VARCHAR(20) | active/banned/muted |
| credit_score | DECIMAL(3,1) | 信用分 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.2 管理员表 (admin)
| 字段 | 类型 | 说明 |
|------|------|------|
| admin_id | BIGINT | 主键 |
| user_id | BIGINT | 关联用户 |
| admin_type | VARCHAR(20) | platform/community |
| community_id | BIGINT | 管理的小区ID |
| status | VARCHAR(20) | 状态 |
| created_at | DATETIME | 创建时间 |

### 2.3 小区表 (community)
| 字段 | 类型 | 说明 |
|------|------|------|
| community_id | BIGINT | 主键 |
| community_name | VARCHAR(100) | 小区名称 |
| city | VARCHAR(50) | 城市 |
| district | VARCHAR(50) | 区县 |
| address | VARCHAR(200) | 详细地址 |
| property_company | VARCHAR(100) | 物业公司 |
| property_phone | VARCHAR(20) | 物业电话 |
| description | TEXT | 简介 |
| status | VARCHAR(20) | 状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.4 小区认证表 (community_verification)
| 字段 | 类型 | 说明 |
|------|------|------|
| verification_id | BIGINT | 主键 |
| user_id | BIGINT | 申请用户 |
| community_id | BIGINT | 申请小区 |
| proof_type | VARCHAR(50) | 证明类型 |
| proof_images | TEXT | 证明材料JSON |
| community_admin_status | VARCHAR(20) | 小区审核状态 |
| community_admin_id | BIGINT | 小区审核人 |
| community_audit_time | DATETIME | 小区审核时间 |
| community_audit_opinion | VARCHAR(500) | 小区审核意见 |
| platform_admin_status | VARCHAR(20) | 平台审核状态 |
| platform_admin_id | BIGINT | 平台审核人 |
| platform_audit_time | DATETIME | 平台审核时间 |
| platform_audit_opinion | VARCHAR(500) | 平台审核意见 |
| created_at | DATETIME | 创建时间 |

### 2.5 房源表 (house)
| 字段 | 类型 | 说明 |
|------|------|------|
| house_id | BIGINT | 主键 |
| landlord_id | BIGINT | 房东ID |
| community_id | BIGINT | 小区ID |
| title | VARCHAR(100) | 标题 |
| house_type | VARCHAR(20) | 户型(1室1厅等) |
| area | DECIMAL(10,2) | 面积 |
| floor | VARCHAR(20) | 楼层 |
| total_floor | INT | 总楼层 |
| orientation | VARCHAR(20) | 朝向 |
| decoration | VARCHAR(20) | 装修 |
| rent_price | DECIMAL(10,2) | 月租金 |
| deposit | DECIMAL(10,2) | 押金 |
| payment_method | VARCHAR(50) | 付款方式 |
| description | TEXT | 描述 |
| contact_phone | VARCHAR(20) | 联系电话 |
| publish_status | VARCHAR(20) | online/offline/rented |
| audit_status | VARCHAR(20) | pending/approved/rejected |
| audit_opinion | VARCHAR(500) | 审核意见 |
| auditor_id | BIGINT | 审核人 |
| audit_time | DATETIME | 审核时间 |
| view_count | INT | 浏览次数 |
| favorite_count | INT | 收藏次数 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.6 房源图片表 (house_image)
| 字段 | 类型 | 说明 |
|------|------|------|
| image_id | BIGINT | 主键 |
| house_id | BIGINT | 房源ID |
| image_url | VARCHAR(500) | 图片URL |
| image_type | VARCHAR(20) | cover/detail |
| sort_order | INT | 排序 |
| created_at | DATETIME | 创建时间 |

### 2.7 收藏表 (favorite)
| 字段 | 类型 | 说明 |
|------|------|------|
| favorite_id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| house_id | BIGINT | 房源ID |
| created_at | DATETIME | 创建时间 |

### 2.8 预约表 (appointment)
| 字段 | 类型 | 说明 |
|------|------|------|
| appointment_id | BIGINT | 主键 |
| house_id | BIGINT | 房源ID |
| tenant_id | BIGINT | 租客ID |
| landlord_id | BIGINT | 房东ID |
| appointment_time | DATETIME | 预约时间 |
| contact_phone | VARCHAR(20) | 联系电话 |
| remark | VARCHAR(500) | 备注 |
| status | VARCHAR(20) | pending/confirmed/rejected/cancelled/completed |
| reject_reason | VARCHAR(500) | 拒绝原因 |
| cancel_reason | VARCHAR(500) | 取消原因 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.9 合同表 (rental_contract)
| 字段 | 类型 | 说明 |
|------|------|------|
| contract_id | BIGINT | 主键 |
| contract_no | VARCHAR(50) | 合同编号 |
| house_id | BIGINT | 房源ID |
| landlord_id | BIGINT | 房东ID |
| tenant_id | BIGINT | 租客ID |
| start_date | DATE | 起始日期 |
| end_date | DATE | 结束日期 |
| rent_price | DECIMAL(10,2) | 月租金 |
| deposit | DECIMAL(10,2) | 押金 |
| payment_method | VARCHAR(50) | 付款方式 |
| custom_terms | TEXT | 自定义条款 |
| landlord_signature | TEXT | 房东签名数据 |
| landlord_sign_time | DATETIME | 房东签名时间 |
| tenant_signature | TEXT | 租客签名数据 |
| tenant_sign_time | DATETIME | 租客签名时间 |
| contract_status | VARCHAR(20) | draft/signed/pending/effective/terminated |
| pdf_url | VARCHAR(500) | PDF文件URL |
| contract_hash | VARCHAR(64) | SHA256哈希 |
| auditor_id | BIGINT | 审核人 |
| audit_time | DATETIME | 审核时间 |
| audit_opinion | VARCHAR(500) | 审核意见 |
| termination_reason | VARCHAR(500) | 解约原因 |
| termination_applicant | BIGINT | 解约申请人 |
| termination_status | VARCHAR(20) | 解约审核状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.10 交易表 (rental_transaction)
| 字段 | 类型 | 说明 |
|------|------|------|
| transaction_id | BIGINT | 主键 |
| contract_id | BIGINT | 合同ID |
| house_id | BIGINT | 房源ID |
| landlord_id | BIGINT | 房东ID |
| tenant_id | BIGINT | 租客ID |
| status | VARCHAR(30) | 交易状态 |
| landlord_checkin_confirmed | BOOLEAN | 房东确认入住 |
| tenant_checkin_confirmed | BOOLEAN | 租客确认入住 |
| checkin_time | DATETIME | 入住时间 |
| landlord_complete_confirmed | BOOLEAN | 房东确认退租 |
| tenant_complete_confirmed | BOOLEAN | 租客确认退租 |
| complete_time | DATETIME | 退租时间 |
| is_evaluated | BOOLEAN | 是否已评价 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.11 支付记录表 (rent_payment)
| 字段 | 类型 | 说明 |
|------|------|------|
| payment_id | BIGINT | 主键 |
| transaction_id | BIGINT | 交易ID |
| contract_id | BIGINT | 合同ID |
| amount | DECIMAL(10,2) | 金额 |
| payment_type | VARCHAR(20) | rent/deposit |
| payment_period | VARCHAR(20) | 账单周期 |
| due_date | DATE | 应付日期 |
| paid_date | DATETIME | 支付日期 |
| status | VARCHAR(20) | pending/paid/overdue/disputed |
| landlord_confirmed | BOOLEAN | 房东确认 |
| tenant_confirmed | BOOLEAN | 租客确认 |
| created_at | DATETIME | 创建时间 |

### 2.12 聊天消息表 (chat_message)
| 字段 | 类型 | 说明 |
|------|------|------|
| message_id | VARCHAR(50) | 主键UUID |
| session_id | VARCHAR(100) | 会话ID |
| sender_id | BIGINT | 发送者ID |
| receiver_id | BIGINT | 接收者ID |
| content | TEXT | 消息内容 |
| original_content | TEXT | 原始内容(过滤前) |
| message_type | VARCHAR(20) | TEXT/IMAGE |
| is_read | BOOLEAN | 是否已读 |
| has_sensitive | BOOLEAN | 是否含敏感词 |
| created_at | DATETIME | 创建时间 |

### 2.13 WebSocket会话表 (websocket_session)
| 字段 | 类型 | 说明 |
|------|------|------|
| session_id | VARCHAR(100) | 主键 |
| user_id | BIGINT | 用户ID |
| is_online | BOOLEAN | 是否在线 |
| last_heartbeat | DATETIME | 最后心跳 |
| created_at | DATETIME | 创建时间 |

### 2.14 论坛帖子表 (forum_post)
| 字段 | 类型 | 说明 |
|------|------|------|
| post_id | BIGINT | 主键 |
| user_id | BIGINT | 作者ID |
| community_id | BIGINT | 小区ID |
| post_type | VARCHAR(20) | 帖子类型 |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| images | TEXT | 图片JSON |
| budget_min | DECIMAL(10,2) | 预算最小 |
| budget_max | DECIMAL(10,2) | 预算最大 |
| expected_area | VARCHAR(50) | 期望面积 |
| expected_type | VARCHAR(50) | 期望户型 |
| is_urgent | BOOLEAN | 是否紧急 |
| status | VARCHAR(20) | active/closed/deleted |
| view_count | INT | 浏览次数 |
| reply_count | INT | 回复数 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.15 论坛回复表 (forum_reply)
| 字段 | 类型 | 说明 |
|------|------|------|
| reply_id | BIGINT | 主键 |
| post_id | BIGINT | 帖子ID |
| user_id | BIGINT | 回复者ID |
| parent_reply_id | BIGINT | 父回复ID |
| content | TEXT | 内容 |
| images | TEXT | 图片JSON |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | DATETIME | 创建时间 |

### 2.16 信用评价表 (credit_evaluation)
| 字段 | 类型 | 说明 |
|------|------|------|
| evaluation_id | BIGINT | 主键 |
| contract_id | BIGINT | 合同ID |
| evaluator_id | BIGINT | 评价人 |
| evaluated_id | BIGINT | 被评价人 |
| star_rating | DECIMAL(2,1) | 星级(1-5) |
| content | TEXT | 评价内容 |
| contract_performance | VARCHAR(20) | 履约评价 |
| tags | TEXT | 标签JSON |
| is_anonymous | BOOLEAN | 是否匿名 |
| created_at | DATETIME | 创建时间 |

### 2.17 举报表 (report)
| 字段 | 类型 | 说明 |
|------|------|------|
| report_id | BIGINT | 主键 |
| reporter_id | BIGINT | 举报人 |
| report_type | VARCHAR(20) | 举报类型(house/user) |
| target_id | BIGINT | 目标ID |
| reason_type | VARCHAR(50) | 原因类型 |
| reason_detail | TEXT | 详细描述 |
| evidence_images | TEXT | 证据图片JSON |
| status | VARCHAR(20) | pending/handled/rejected/withdrawn |
| handler_id | BIGINT | 处理人 |
| handle_result | TEXT | 处理结果 |
| handle_time | DATETIME | 处理时间 |
| created_at | DATETIME | 创建时间 |

### 2.18 系统通知表 (system_notification)
| 字段 | 类型 | 说明 |
|------|------|------|
| notification_id | BIGINT | 主键 |
| user_id | BIGINT | 接收用户 |
| notification_type | VARCHAR(50) | 通知类型 |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| related_type | VARCHAR(50) | 关联类型 |
| related_id | BIGINT | 关联ID |
| is_read | BOOLEAN | 是否已读 |
| created_at | DATETIME | 创建时间 |

### 2.19 违规记录表 (violation)
| 字段 | 类型 | 说明 |
|------|------|------|
| violation_id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| message_id | VARCHAR(50) | 消息ID |
| violation_type | VARCHAR(50) | 违规类型 |
| content | TEXT | 违规内容 |
| ai_analysis | TEXT | AI分析结果 |
| severity | VARCHAR(20) | 严重程度 |
| status | VARCHAR(20) | 状态 |
| action_taken | VARCHAR(50) | 采取措施 |
| ban_until | DATETIME | 封禁截止 |
| handler_id | BIGINT | 处理人 |
| handler_opinion | TEXT | 处理意见 |
| handled_at | DATETIME | 处理时间 |
| created_at | DATETIME | 创建时间 |

### 2.20 敏感词表 (sensitive_word)
| 字段 | 类型 | 说明 |
|------|------|------|
| word_id | BIGINT | 主键 |
| word | VARCHAR(100) | 敏感词 |
| word_type | VARCHAR(20) | deny/allow |
| is_active | BOOLEAN | 是否启用 |
| created_at | DATETIME | 创建时间 |

### 2.21 小区配套表 (community_facility)
| 字段 | 类型 | 说明 |
|------|------|------|
| facility_id | BIGINT | 主键 |
| community_id | BIGINT | 小区ID |
| name | VARCHAR(100) | 名称 |
| facility_type | VARCHAR(50) | 类型 |
| description | TEXT | 描述 |
| location | VARCHAR(200) | 位置 |
| creator_id | BIGINT | 创建者 |
| status | VARCHAR(20) | 状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.22 配套反馈表 (facility_feedback)
| 字段 | 类型 | 说明 |
|------|------|------|
| feedback_id | BIGINT | 主键 |
| community_id | BIGINT | 小区ID |
| facility_id | BIGINT | 配套ID |
| user_id | BIGINT | 用户ID |
| feedback_type | VARCHAR(20) | 反馈类型 |
| content | TEXT | 内容 |
| images | TEXT | 图片JSON |
| status | VARCHAR(20) | 状态 |
| handler_id | BIGINT | 处理人 |
| handler_reply | TEXT | 处理回复 |
| handled_at | DATETIME | 处理时间 |
| created_at | DATETIME | 创建时间 |

---

## 3. 实体关系图 (ER图)

```
┌──────────┐       ┌───────────────┐       ┌───────────┐
│   User   │───────│ CommunityVeri │───────│ Community │
│          │  1:N  │   fication    │  N:1  │           │
└────┬─────┘       └───────────────┘       └─────┬─────┘
     │                                           │
     │ 1:N                                       │ 1:N
     ▼                                           ▼
┌──────────┐       ┌───────────────┐       ┌───────────┐
│  House   │───────│  HouseImage   │       │ Facility  │
│          │  1:N  │               │       │           │
└────┬─────┘       └───────────────┘       └───────────┘
     │
     │ 1:N
     ▼
┌──────────┐       ┌───────────────┐       ┌───────────┐
│Appointment│      │RentalContract │───────│Transaction│
│          │       │               │  1:1  │           │
└──────────┘       └───────┬───────┘       └─────┬─────┘
                           │                     │
                           │ 1:N                 │ 1:N
                           ▼                     ▼
                   ┌───────────────┐       ┌───────────┐
                   │CreditEvaluation│      │RentPayment│
                   │               │       │           │
                   └───────────────┘       └───────────┘

┌──────────┐       ┌───────────────┐       ┌───────────┐
│   User   │───────│  ChatMessage  │       │ ForumPost │
│          │  1:N  │               │       │           │
└────┬─────┘       └───────────────┘       └─────┬─────┘
     │                                           │
     │ 1:N                                       │ 1:N
     ▼                                           ▼
┌──────────┐       ┌───────────────┐       ┌───────────┐
│  Report  │       │ Notification  │       │ForumReply │
│          │       │               │       │           │
└──────────┘       └───────────────┘       └───────────┘
```

---

## 4. 索引设计

### 关键索引
```sql
-- 用户表
CREATE INDEX idx_user_openid ON user(openid);
CREATE INDEX idx_user_phone ON user(phone);

-- 房源表
CREATE INDEX idx_house_landlord ON house(landlord_id);
CREATE INDEX idx_house_community ON house(community_id);
CREATE INDEX idx_house_status ON house(publish_status, audit_status);
CREATE INDEX idx_house_price ON house(rent_price);

-- 合同表
CREATE INDEX idx_contract_landlord ON rental_contract(landlord_id);
CREATE INDEX idx_contract_tenant ON rental_contract(tenant_id);
CREATE INDEX idx_contract_status ON rental_contract(contract_status);

-- 聊天消息表
CREATE INDEX idx_chat_session ON chat_message(session_id);
CREATE INDEX idx_chat_sender ON chat_message(sender_id);
CREATE INDEX idx_chat_receiver ON chat_message(receiver_id);
CREATE INDEX idx_chat_time ON chat_message(created_at);

-- 通知表
CREATE INDEX idx_notification_user ON system_notification(user_id);
CREATE INDEX idx_notification_read ON system_notification(user_id, is_read);
```
