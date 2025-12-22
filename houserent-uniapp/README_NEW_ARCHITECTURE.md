# 租房小程序 - 新架构文档

## 📋 重构说明

基于**贝壳找房、自如**等主流租房APP的设计理念，完全重构了页面和路由系统。

---

## 🏗️ 新架构特点

### 1. 简洁清晰的目录结构
```
pages/
├── index/                 # 启动页（自动路由分发）
├── login/                 # 登录页
├── dev-login/             # 开发测试账号
├── home/                  # 租客找房首页 ⭐
├── house/
│   └── detail/           # 房源详情页
├── favorite/             # 我的收藏
├── chat/                 # 消息中心
├── profile/              # 个人中心（通用）
└── landlord/             # 房东专属模块
    ├── houses/          # 房源管理 ⭐
    └── publish/         # 发布房源

utils/
├── config.js             # 全局配置
├── storage.js            # 本地存储封装
├── request.js            # 网络请求封装
└── api.js                # API接口定义
```

### 2. 角色区分明确

#### 租客（userType=3）
- **首页**: `home.vue` - 找房、搜索、筛选
- **核心流程**: 找房 → 查看详情 → 收藏 → 咨询房东 → 预约看房
- **TabBar**: 找房 | 收藏 | 消息 | 我的

#### 房东（userType=4）
- **首页**: `landlord/houses.vue` - 房源管理
- **核心流程**: 发布房源 → 管理房源 → 上线/下线 → 编辑/删除
- **TabBar**: 房源 | 消息 | 我的

### 3. 启动流程

```
index.vue (启动页)
    ↓
检查登录状态
    ↓
已登录 ──→ 租客(userType=3) ──→ home.vue
    ↓
    └──→ 房东(userType=4) ──→ landlord/houses.vue
    ↓
未登录 ──→ login.vue
```

---

## 🔧 核心功能

### 租客功能模块
| 页面 | 路径 | 功能 |
|------|------|------|
| 找房首页 | `/pages/home/home` | 房源列表、筛选（整租/合租/品牌公寓） |
| 房源详情 | `/pages/house/detail/detail` | 图片轮播、价格、户型、收藏、咨询 |
| 我的收藏 | `/pages/favorite/favorite` | 收藏房源列表、取消收藏 |
| 消息中心 | `/pages/chat/chat` | （开发中） |
| 个人中心 | `/pages/profile/profile` | 用户信息、申请房东、退出登录 |

### 房东功能模块
| 页面 | 路径 | 功能 |
|------|------|------|
| 房源管理 | `/pages/landlord/houses/houses` | 房源列表、统计、上线/下线、删除 |
| 发布房源 | `/pages/landlord/publish/publish` | 图片上传、信息填写、草稿/提交 |

---

## 🎨 设计特色

### 1. 参考主流APP设计
- **贝壳找房**: 清晰的房源卡片、筛选标签
- **自如**: 简洁的UI、渐变色统计卡片
- **整体风格**: 现代化、扁平化、卡片式布局

### 2. 交互优化
- ✅ 底部TabBar固定导航
- ✅ 下拉刷新、上拉加载更多
- ✅ 图片轮播、收藏动画
- ✅ 表单验证、提示优化

### 3. 颜色体系
```scss
主色调: #409eff (蓝色)
房东主题: #667eea → #764ba2 (紫色渐变)
价格: #ff6b6b (红色)
背景: #f5f7fa (浅灰)
```

---

## 📱 页面截图说明

### 租客端
1. **找房首页** - 搜索框 + 筛选标签 + 房源卡片列表
2. **房源详情** - 轮播图 + 价格信息 + 房东信息 + 咨询/预约按钮
3. **收藏页面** - 收藏房源列表 + 一键取消收藏

### 房东端
1. **房源管理** - 顶部统计 + 发布按钮 + 房源列表（带操作按钮）
2. **发布房源** - 图片上传 + 表单填写 + 草稿/提交

---

## 🔌 API接口

### 已集成接口
```javascript
// 认证
api.auth.wechatLogin()        // 微信登录
api.auth.devLogin()            // 开发测试登录

// 房源
api.house.search()             // 搜索房源
api.house.getDetail()          // 房源详情
api.house.publish()            // 发布房源
api.house.getLandlordHouses()  // 房东房源列表
api.house.update()             // 更新房源
api.house.delete()             // 删除房源
api.house.toggleStatus()       // 上线/下线

// 收藏
api.favorite.add()             // 收藏
api.favorite.remove()          // 取消收藏
api.favorite.getList()         // 收藏列表

// 用户
api.user.updateProfile()       // 更新用户信息
```

---

## ⚙️ 本地存储

```javascript
import { getToken, setToken, getUserInfo, isLogin } from '@/utils/storage.js'

// Token管理
setToken(token)
getToken()
removeToken()

// 用户信息
setUserInfo(userInfo)
getUserInfo()

// 状态判断
isLogin()         // 是否登录
isLandlord()      // 是否房东
isTenant()        // 是否租客
```

---

## 🚀 快速开始

### 1. 启动开发服务器
```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

### 2. 测试账号登录
- 点击登录页的"开发测试账号"按钮
- 选择租客或房东账号快速登录

### 3. 功能测试流程

**租客流程**:
```
登录 → 找房首页 → 查看房源详情 → 收藏 → 个人中心
```

**房东流程**:
```
登录 → 房源管理 → 发布房源 → 上传图片 → 填写信息 → 提交审核
```

---

## 🎯 后续扩展

### 待实现功能
- [ ] 房源搜索页（关键词搜索）
- [ ] 地图找房（高德地图）
- [ ] 聊天功能（WebSocket）
- [ ] 合同管理
- [ ] 小区认证
- [ ] 房东认证流程
- [ ] 收益统计
- [ ] 租客管理

### 优化方向
- [ ] 图片上传到OSS
- [ ] 缓存优化
- [ ] 分页加载优化
- [ ] 错误处理完善
- [ ] 动画效果增强

---

## 📝 注意事项

1. **登录状态**：所有请求自动携带token，401自动跳转登录
2. **角色切换**：房东可在个人中心切换到租客视角
3. **路由守卫**：启动页自动根据角色分发路由
4. **图片处理**：房源图片使用JSON格式存储数组
5. **开发模式**：保留dev-login用于快速测试

---

## 🔗 相关文档

- [UniApp官方文档](https://uniapp.dcloud.net.cn/)
- [贝壳找房APP分析](https://www.woshipm.com/evaluating/1230970.html)
- 后端API文档：见项目 `houserent-backend` 的 Swagger

---

**重构完成时间**: 2025-12-19  
**参考设计**: 贝壳找房、自如、58同城  
**技术栈**: UniApp + Vue3 + SpringBoot
