# 房东用户功能开发文档

## 📋 开发概述

本次开发实现了房东用户功能模块，支持租户和房东两种角色的登录和页面区分展示。

## ✅ 完成功能

### 1. 登录页面改造
**文件**: `pages/login/login.vue`

#### 功能特性：
- ✅ 添加租户/房东身份选择标签切换
- ✅ 根据选择的身份调用不同的登录API
  - 租户：`/api/auth/tenant-login`
  - 房东：`/api/auth/landlord-login`
- ✅ 登录成功后根据userType跳转到对应首页
  - 租户(userType=3)：跳转到房源浏览页
  - 房东(userType=4)：跳转到房源发布页

#### 界面展示：
- 美观的渐变背景
- 动画波浪效果
- 身份切换标签
- 表单输入框
- 记住密码、忘记密码功能
- 微信快捷登录

### 2. 房东房源发布首页
**文件**: `pages/landlord-publish/landlord-publish.vue`

#### 功能模块：
- ✅ **统计卡片**
  - 房源总数
  - 在线房源
  - 总浏览量
  
- ✅ **快速发布按钮**
  - 醒目的发布新房源入口
  
- ✅ **房源管理菜单**
  - 我的房源
  - 待审核（带数量角标）
  - 已下线
  - 已出租
  
- ✅ **租赁管理菜单**
  - 租赁合同
  - 租客管理
  - 收益统计
  - 信用评价
  
- ✅ **最近发布列表**
  - 显示最近发布的房源卡片
  - 包含图片、标题、价格、状态、浏览量
  - 点击查看房源详情
  
- ✅ **空状态提示**
  - 当没有房源时显示友好的空状态页面

### 3. 房东个人中心页面
**文件**: `pages/landlord-profile/landlord-profile.vue`

#### 功能模块：
- ✅ **用户信息卡片**
  - 头像、昵称、手机号
  - 房东认证标识
  - 信用评分显示
  
- ✅ **收益统计卡片**
  - 本月收益
  - 累计收益
  - 待收款
  
- ✅ **功能菜单**
  - 房源管理（我的房源、发布房源）
  - 租赁服务（租赁合同、租客管理、收益明细）
  - 信用与评价（我的评价、信用记录）
  - 其他（设置、帮助与反馈）
  
- ✅ **退出登录功能**

### 4. 聊天页面优化
**文件**: `pages/chat/chat.vue`

#### 功能特性：
- ✅ 根据用户类型显示不同的副标题
  - 租户：显示"与房东沟通"
  - 房东：显示"与租客沟通"
- ✅ 根据用户类型显示不同的空状态提示
  - 租户："开始咨询心仪的房源吧"
  - 房东："等待租客咨询您的房源"

### 5. 用户类型工具类
**文件**: `utils/userType.js`

#### 提供的工具函数：
```javascript
// 用户类型常量
USER_TYPE = {
  PLATFORM_ADMIN: 1,    // 平台管理员
  COMMUNITY_ADMIN: 2,   // 小区管理员
  TENANT: 3,            // 租户
  LANDLORD: 4           // 房东
}

// 工具函数
getCurrentUser()          // 获取当前用户信息
getUserType()            // 获取当前用户类型
isTenant()              // 判断是否为租户
isLandlord()            // 判断是否为房东
isAdmin()               // 判断是否为管理员
isLoggedIn()            // 判断用户是否已登录
getUserTypeName()        // 获取用户类型名称
getHomePageByUserType()  // 根据用户类型获取首页路径
navigateToHome()         // 跳转到当前用户的首页
checkPagePermission()    // 检查页面访问权限
logout()                // 退出登录
```

### 6. 自定义TabBar组件
**文件**: `components/custom-tabbar/custom-tabbar.vue`

#### 功能特性：
- ✅ 根据用户类型自动显示不同的TabBar
  - 租户TabBar：首页(房源浏览) - 聊一聊 - 个人中心
  - 房东TabBar：房源发布 - 聊一聊 - 个人中心
- ✅ 自动高亮当前页面
- ✅ 支持底部安全区适配
- ✅ 使用与原生TabBar相同的图标

### 7. 页面路由配置
**文件**: `pages.json`

#### 新增页面：
- `pages/landlord-publish/landlord-publish` - 房东房源发布首页
- `pages/landlord-profile/landlord-profile` - 房东个人中心

## 🎨 UI设计特点

### 设计风格
- 统一的渐变主题色：`#667eea` → `#764ba2`
- 圆角卡片设计：20rpx
- 阴影效果：柔和的box-shadow
- 动画过渡：smooth transitions

### 色彩规范
- 主色：`#667eea`（紫蓝色）
- 副色：`#764ba2`（深紫色）
- 成功：`#4caf50`（绿色）
- 警告：`#ff9800`（橙色）
- 错误：`#ff4757`（红色）
- 文字主色：`#333`
- 文字副色：`#666`、`#999`

### 图标使用
- 统一使用Emoji图标，美观且无需额外资源
- 使用与租户相同的底部导航图标

## 📁 项目结构

```
houserent-uniapp/
├── pages/
│   ├── login/                      # 登录页（已改造）
│   ├── house-list/                 # 租户-房源浏览
│   ├── landlord-publish/           # 房东-房源发布（新增）
│   ├── chat/                       # 聊一聊（已优化）
│   ├── profile/                    # 租户-个人中心
│   ├── landlord-profile/           # 房东-个人中心（新增）
│   └── ...
├── components/
│   └── custom-tabbar/              # 自定义TabBar组件（新增）
├── utils/
│   └── userType.js                 # 用户类型工具类（新增）
├── static/
│   └── tabbar/                     # TabBar图标
├── pages.json                      # 页面配置（已更新）
├── README_LANDLORD_FEATURE.md      # 本文档
└── README_TABBAR.md               # TabBar实现方案文档
```

## 🔧 使用指南

### 1. 测试账号

参考后端SQL文件 `houserent-backend/sql/test_users.sql`：

**租户账号**：
- 手机号：13800138001，密码：123456
- 手机号：13800138002，密码：123456

**房东账号**：
- 手机号：13900139001，密码：123456
- 手机号：13900139002，密码：123456

### 2. 登录流程

1. 打开登录页面
2. 选择"租户登录"或"房东登录"标签
3. 输入对应的手机号和密码
4. 点击"登录"按钮
5. 登录成功后自动跳转到对应的首页

### 3. 用户类型判断

在任何页面中使用工具函数：

```javascript
import { getUserType, isTenant, isLandlord } from '@/utils/userType.js';

export default {
  onShow() {
    const userType = getUserType();
    console.log('当前用户类型:', userType);
    
    if (isTenant()) {
      console.log('当前是租户');
    }
    
    if (isLandlord()) {
      console.log('当前是房东');
    }
  }
}
```

### 4. 页面权限控制

在需要权限控制的页面中：

```javascript
import { checkPagePermission, USER_TYPE } from '@/utils/userType.js';

export default {
  onShow() {
    // 检查是否为房东，如果不是则跳转
    if (!checkPagePermission(USER_TYPE.LANDLORD)) {
      return;
    }
    
    // 继续页面逻辑
    this.loadData();
  }
}
```

## 🚀 部署说明

### 前端部署
1. 确保所有依赖已安装
2. 配置后端API地址（目前为 `http://localhost:8080`）
3. 运行项目：`npm run dev:mp-weixin`（微信小程序）
4. 或运行：`npm run dev:h5`（H5）

### 后端部署
1. 确保MySQL数据库已创建
2. 执行数据库脚本：`houserent_system.sql`
3. 导入测试数据：`test_users.sql`
4. 启动Spring Boot后端服务
5. 确保后端服务运行在8080端口

## 📝 待完成功能

以下功能当前显示为"功能开发中"，需要后续实现：

### 房东功能
- [ ] 房源发布表单页面
- [ ] 我的房源列表页面
- [ ] 房源详情编辑
- [ ] 租客管理页面
- [ ] 收益统计页面
- [ ] 租赁合同管理

### 租户功能
- [ ] 房源详情页面
- [ ] 我的收藏
- [ ] 我的订单
- [ ] 租赁合同查看

### 通用功能
- [ ] 聊天功能完整实现
- [ ] 消息通知
- [ ] 信用评价系统
- [ ] 设置页面
- [ ] 帮助与反馈

## ⚠️ 注意事项

1. **TabBar实现**
   - 当前保留了原生TabBar配置（租户）
   - 房东页面需要手动切换或使用自定义TabBar组件
   - 详见 `README_TABBAR.md` 文档

2. **API接口**
   - 确保后端已实现对应的登录接口
   - 房东登录：`POST /api/auth/landlord-login`
   - 租户登录：`POST /api/auth/tenant-login`

3. **用户体验**
   - 所有"功能开发中"的菜单点击会提示用户
   - 页面加载时会检查用户类型，非法访问会被拦截
   - 退出登录会清除本地存储并跳转登录页

4. **数据持久化**
   - 用户信息保存在本地存储中
   - 包含：token、userId、userType、nickname、phone等

## 🐛 已知问题

1. TabBar无法根据用户类型动态切换（UniApp限制）
   - 解决方案：使用自定义TabBar或通过reLaunch切换页面

2. 部分功能菜单为占位，点击提示"功能开发中"
   - 待后续开发完整功能

## 📞 技术支持

如有问题，请参考：
- `README_TABBAR.md` - TabBar实现方案
- `utils/userType.js` - 用户类型工具类
- 后端文档：`houserent-backend/sql/README.md`

---

**开发日期**: 2025-11-26  
**版本**: v1.0.0  
**开发者**: Cascade AI
