# 小区房屋租赁管理平台 - API接口文档

## 1. 认证模块 (/api/auth)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /wechat/login | 微信登录 | code |
| POST | /admin/login | 管理员登录 | phone, password |
| POST | /admin/register | 管理员注册 | phone, password, nickname |
| POST | /refresh | 刷新Token | - |
| POST | /logout | 退出登录 | - |
| GET | /current | 获取当前用户 | - |

## 2. 用户模块 (/api/user)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /profile | 获取个人资料 | - |
| PUT | /profile | 更新个人资料 | nickname, avatar, phone |
| PUT | /role | 切换角色 | role |
| GET | /favorites | 获取收藏列表 | pageNum, pageSize |

## 3. 房源模块 (/api/house)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /list | 房源列表 | communityId, minPrice, maxPrice, houseType, pageNum, pageSize |
| GET | /search | 搜索房源 | keyword, city, district, pageNum, pageSize |
| GET | /{id} | 房源详情 | - |
| POST | /publish | 发布房源 | title, communityId, houseType, area, rentPrice, images... |
| PUT | /{id} | 更新房源 | 同发布参数 |
| DELETE | /{id} | 删除房源 | - |
| POST | /{id}/online | 上架房源 | - |
| POST | /{id}/offline | 下架房源 | - |
| POST | /{id}/favorite | 收藏房源 | - |
| DELETE | /{id}/favorite | 取消收藏 | - |
| GET | /{id}/is-favorite | 是否已收藏 | - |

## 4. 预约模块 (/api/appointment)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /create | 创建预约 | houseId, appointmentTime, contactPhone, remark |
| GET | /tenant | 租客预约列表 | status, pageNum, pageSize |
| GET | /landlord | 房东预约列表 | status, pageNum, pageSize |
| POST | /{id}/confirm | 确认预约 | - |
| POST | /{id}/reject | 拒绝预约 | reason |
| POST | /{id}/cancel | 取消预约 | reason |
| POST | /{id}/complete | 完成预约 | - |

## 5. 合同模块 (/api/contract)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /create | 创建合同 | houseId, tenantId, startDate, endDate, rentPrice, paymentMethod |
| GET | /list | 合同列表 | status, pageNum, pageSize |
| GET | /{id} | 合同详情 | - |
| POST | /{id}/landlord-sign | 房东签名 | signatureData |
| POST | /{id}/tenant-sign | 租客签名 | signatureData |
| GET | /{id}/pdf | 下载PDF | - |
| GET | /{id}/verify | 验证完整性 | - |
| POST | /{id}/terminate | 申请解约 | reason |

## 6. 聊天模块 (/api/chat)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /history | 聊天历史 | targetUserId, pageNum, pageSize |
| GET | /recent | 最近聊天 | - |
| GET | /unread-count | 未读数量 | - |
| POST | /mark-read | 标记已读 | targetUserId |
| DELETE | /session/{sessionId} | 删除会话 | - |

## 7. 小区模块 (/api/community)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /list | 小区列表 | city, pageNum, pageSize |
| GET | /search | 搜索小区 | keyword |
| GET | /{id} | 小区详情 | - |
| GET | /{id}/facilities | 配套设施 | type |
| POST | /{id}/facilities | 添加设施 | name, type, description, location |
| PUT | /facilities/{id} | 更新设施 | name, type, description |
| DELETE | /facilities/{id} | 删除设施 | - |

## 8. 小区认证模块 (/api/verification)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /submit | 提交认证 | communityId, proofType, proofImages |
| GET | /status | 认证状态 | communityId |
| GET | /history | 认证历史 | - |
| POST | /{id}/cancel | 撤销认证 | - |

## 9. 论坛模块 (/api/forum)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /posts | 帖子列表 | communityId, postType, pageNum, pageSize |
| GET | /posts/{id} | 帖子详情 | - |
| POST | /posts | 发布帖子 | communityId, postType, title, content, images |
| PUT | /posts/{id} | 更新帖子 | title, content, images |
| DELETE | /posts/{id} | 删除帖子 | - |
| POST | /posts/{id}/close | 关闭帖子 | - |
| GET | /posts/{id}/replies | 回复列表 | - |
| POST | /posts/{id}/replies | 发布回复 | content, parentReplyId |
| DELETE | /replies/{id} | 删除回复 | - |

## 10. 评价模块 (/api/evaluation)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /submit | 提交评价 | contractId, evaluatedId, starRating, content, tags |
| GET | /received | 收到的评价 | pageNum, pageSize |
| GET | /sent | 发出的评价 | pageNum, pageSize |
| GET | /statistics | 评价统计 | userId |
| GET | /can-evaluate | 是否可评价 | contractId |

## 11. 交易模块 (/api/transaction)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /list | 交易列表 | status, pageNum, pageSize |
| GET | /{id} | 交易详情 | - |
| POST | /{id}/confirm-checkin | 确认入住 | - |
| POST | /{id}/confirm-complete | 确认退租 | - |
| POST | /{id}/cancel | 取消交易 | reason |

## 12. 支付模块 (/api/payment)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /records | 支付记录 | transactionId, pageNum, pageSize |
| POST | /{id}/confirm | 确认支付 | - |
| POST | /{id}/dispute | 支付争议 | reason |

## 13. 举报模块 (/api/report)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /submit | 提交举报 | reportType, targetId, reasonType, reasonDetail, evidenceImages |
| GET | /my | 我的举报 | - |
| POST | /{id}/withdraw | 撤回举报 | - |

## 14. 通知模块 (/api/notification)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /list | 通知列表 | pageNum, pageSize |
| GET | /unread-count | 未读数量 | - |
| POST | /{id}/read | 标记已读 | - |
| POST | /read-all | 全部已读 | - |
| DELETE | /{id} | 删除通知 | - |

## 15. 文件上传 (/api/file)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| POST | /upload | 上传单文件 | file, folder |
| POST | /upload/batch | 批量上传 | files, folder |
| DELETE | /delete | 删除文件 | fileUrl |

## 16. 房东模块 (/api/landlord)

| 方法 | 接口 | 描述 | 参数 |
|------|------|------|------|
| GET | /houses | 我的房源 | status, pageNum, pageSize |
| GET | /statistics | 房源统计 | - |
| GET | /tenants | 我的租客 | pageNum, pageSize |
| GET | /revenue | 收入统计 | startDate, endDate |
| GET | /revenue/monthly | 月度收入 | year |

## 17. 管理员模块 (/api/admin)

### 用户管理
| 方法 | 接口 | 描述 |
|------|------|------|
| GET | /users | 用户列表 |
| POST | /users/{id}/auth-audit | 审核用户认证 |
| POST | /users/{id}/toggle-status | 封禁/解封用户 |

### 小区管理
| 方法 | 接口 | 描述 |
|------|------|------|
| GET | /communities | 小区列表 |
| GET | /communities/{id} | 小区详情 |
| POST | /communities | 添加小区 |
| PUT | /communities/{id} | 更新小区 |
| DELETE | /communities/{id} | 删除小区 |

### 管理员管理
| 方法 | 接口 | 描述 |
|------|------|------|
| GET | /admins/unassigned-community | 未分配小区的管理员 |
| POST | /admins/create | 创建小区管理员 |
| PUT | /admins/{id} | 更新管理员 |
| DELETE | /admins/{id} | 删除管理员 |
| POST | /admins/{id}/assign-community | 分配小区 |

### 审核管理
| 方法 | 接口 | 描述 |
|------|------|------|
| GET | /houses/pending | 待审核房源 |
| POST | /houses/{id}/audit | 审核房源 |
| GET | /verifications/community/{id}/pending | 小区待审核认证 |
| GET | /verifications/platform/pending | 平台待审核认证 |
| POST | /verifications/{id}/community-audit | 小区管理员审核 |
| POST | /verifications/{id}/platform-audit | 平台管理员审核 |
| GET | /contracts/pending | 待审核合同 |
| POST | /contracts/{id}/audit | 审核合同 |
| POST | /contracts/{id}/termination-audit | 审核解约 |

### 举报管理
| 方法 | 接口 | 描述 |
|------|------|------|
| GET | /reports | 举报列表 |
| GET | /reports/community/{id} | 小区举报 |
| POST | /reports/{id}/handle | 处理举报 |

### 统计
| 方法 | 接口 | 描述 |
|------|------|------|
| GET | /dashboard | 仪表板数据 |

## 18. WebSocket接口

### 连接地址
```
ws://{host}:8888/websocket/chat/{userId}?token={token}
```

### 消息格式

**发送消息**
```json
{
  "type": "CHAT",
  "receiverId": 123,
  "content": "消息内容",
  "messageType": "TEXT"
}
```

**心跳**
```json
{"type": "PING"}
```

**接收消息**
```json
{
  "type": "CHAT",
  "messageId": "uuid",
  "senderId": 456,
  "content": "消息内容",
  "messageType": "TEXT",
  "createdAt": "2024-01-01 12:00:00"
}
```

**系统通知**
```json
{
  "type": "notification",
  "notificationType": "contract_approved",
  "title": "合同审核通过",
  "content": "您的租赁合同已通过审核",
  "notificationId": 789
}
```

---

## 19. 响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ ... ],
    "total": 100,
    "current": 1,
    "size": 20
  }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

### 常见错误码
| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |
| 1001 | 用户已存在 |
| 1002 | 用户不存在 |
| 1003 | 密码错误 |
| 2001 | 房源不存在 |
| 2002 | 房源状态错误 |
| 3001 | 合同不存在 |
| 3002 | 合同状态错误 |
| 4001 | 小区认证未通过 |
