# 启动和导航栏问题修复

## 🐛 问题描述

1. **启动问题**：进入小程序直接进入房源浏览页，而不是登录页
2. **导航栏问题**：自定义TabBar没有显示

## ✅ 已修复

### 1. 启动页问题修复

**修改文件**: `pages.json`

**问题原因**：
- `pages.json`中第一个页面是`house-list`，所以小程序启动时直接进入房源浏览页

**解决方案**：
- 将`login`页面移到`pages`数组的第一位
- 删除重复的`login`页面配置

**修改内容**：
```json
{
  "pages": [
    {
      "path": "pages/login/login",  // 移到第一位作为启动页
      "style": {
        "navigationBarTitleText": "登录",
        "navigationStyle": "custom"
      }
    },
    // ... 其他页面
  ]
}
```

### 2. 登录页自动跳转

**修改文件**: `pages/login/login.vue`

**新增功能**：
- 在`onLoad`生命周期检查登录状态
- 如果已登录，自动跳转到对应的首页
  - 租户(userType=3) → 房源浏览页
  - 房东(userType=4) → 房源发布页

**新增代码**：
```javascript
onLoad() {
  // 检查是否已登录
  this.checkLoginStatus();
  // 读取记住的密码
  this.loadSavedCredentials();
},

methods: {
  // 检查登录状态
  checkLoginStatus() {
    const token = uni.getStorageSync('token');
    const userInfo = uni.getStorageSync('userInfo');
    
    if (token && userInfo && userInfo.userType) {
      // 已登录，跳转到对应首页
      if (userInfo.userType === 3) {
        uni.reLaunch({ url: '/pages/house-list/house-list' });
      } else if (userInfo.userType === 4) {
        uni.reLaunch({ url: '/pages/landlord-publish/landlord-publish' });
      }
    }
  }
}
```

### 3. 自定义TabBar显示问题修复

**修改文件**: `components/custom-tabbar/custom-tabbar.vue`

**问题原因**：
- 组件在`created`生命周期可能获取不到当前页面信息
- `getCurrentPages()`在组件初始化时可能返回空数组

**解决方案**：
- 添加`mounted`生命周期，再次初始化
- 使用`$nextTick`延迟设置当前选中的tab
- 完善用户类型判断逻辑

**修改内容**：
```javascript
created() {
  this.initTabBar();
},

mounted() {
  // mounted时再次初始化，确保能获取到当前页面信息
  this.initTabBar();
},

methods: {
  initTabBar() {
    const userType = getUserType();
    
    // 根据用户类型设置TabBar
    if (userType === USER_TYPE.LANDLORD) {
      this.tabBarList = this.landlordTabBar;
    } else if (userType === USER_TYPE.TENANT) {
      this.tabBarList = this.tenantTabBar;
    } else {
      // 未登录，默认显示租户TabBar
      this.tabBarList = this.tenantTabBar;
    }
    
    // 延迟设置当前选中的tab，确保页面已加载
    this.$nextTick(() => {
      this.setCurrentTab();
    });
  }
}
```

## 🔄 启动流程

修复后的启动流程：

1. **小程序启动** → 进入`pages/login/login`页面
2. **登录页onLoad** → 检查登录状态
   - 如果**未登录** → 停留在登录页，显示登录表单
   - 如果**已登录** → 自动跳转到对应首页
     - 租户 → `pages/house-list/house-list`（显示租户TabBar）
     - 房东 → `pages/landlord-publish/landlord-publish`（显示房东TabBar）
3. **TabBar页面** → 自定义TabBar组件初始化并显示

## 📝 注意事项

1. **清除缓存测试**
   - 首次测试时建议清除小程序缓存
   - 或者先退出登录，测试未登录状态

2. **TabBar显示条件**
   - 只在TabBar页面显示底部导航栏
   - 登录页、找回密码页等不显示TabBar

3. **用户体验**
   - 已登录用户打开小程序会直接进入首页
   - 未登录用户需要先登录
   - 不同角色看到不同的TabBar

## 🧪 测试步骤

### 测试1：未登录启动
```
1. 清除小程序缓存/退出登录
2. 重新启动小程序
3. ✅ 应该进入登录页
4. ✅ 底部不显示TabBar
```

### 测试2：租户已登录启动
```
1. 使用租户账号登录（13800138001/123456）
2. 关闭小程序
3. 重新打开小程序
4. ✅ 应该直接进入房源浏览页
5. ✅ 底部显示：首页-聊一聊-个人中心
```

### 测试3：房东已登录启动
```
1. 使用房东账号登录（13900139001/123456）
2. 关闭小程序
3. 重新打开小程序
4. ✅ 应该直接进入房源发布页
5. ✅ 底部显示：房源发布-聊一聊-个人中心
```

### 测试4：TabBar切换
```
1. 已登录状态下
2. 点击底部TabBar各个选项
3. ✅ 页面应该正确切换
4. ✅ 当前页面应该高亮显示
```

## 📂 修改的文件

1. `pages.json` - 调整页面顺序，login移到第一位
2. `pages/login/login.vue` - 添加登录状态检查和自动跳转
3. `components/custom-tabbar/custom-tabbar.vue` - 优化TabBar初始化逻辑

---

**修复日期**: 2025-11-26  
**状态**: ✅ 已完成
