/**
 * 修复 common_vendor._export_sfc is not a function 错误
 * 这个脚本会自动修复所有编译后的页面文件
 */

const fs = require('fs');
const path = require('path');

// 需要修复的页面路径列表
const pagesToFix = [
  'pages/my-houses/my-houses.js',
  'pages/landlord/houses/houses.js',
  'pages/landlord/publish/publish.js',
  'pages/home/home.js',
  'pages/login/login.js',
  'pages/index/index.js',
  'components/custom-tabbar/custom-tabbar.js'
];

// 修复单个文件
function fixFile(filePath) {
  try {
    if (!fs.existsSync(filePath)) {
      console.log(`文件不存在: ${filePath}`);
      return false;
    }

    let content = fs.readFileSync(filePath, 'utf8');
    
    // 检查是否已经修复
    if (content.includes('common_vendor._export_sfc = function')) {
      console.log(`已修复: ${filePath}`);
      return true;
    }

    // 如果文件使用了 _export_sfc 但没有定义，添加定义
    if (content.includes('_export_sfc') && !content.includes('common_vendor._export_sfc = function')) {
      // 在文件开头添加函数定义
      const functionDef = `// 修复 _export_sfc 函数缺失问题
if (!common_vendor._export_sfc) {
  common_vendor._export_sfc = function(component, options) {
    if (options && options.render) {
      component.render = options.render;
    }
    if (options && options.staticRenderFns) {
      component.staticRenderFns = options.staticRenderFns;
    }
    if (options && options.__scopeId) {
      component.__scopeId = options.__scopeId;
    }
    return component;
  };
}

`;
      
      content = functionDef + content;
      fs.writeFileSync(filePath, content, 'utf8');
      console.log(`已修复: ${filePath}`);
      return true;
    }

    console.log(`无需修复: ${filePath}`);
    return true;
    
  } catch (error) {
    console.error(`修复失败 ${filePath}:`, error.message);
    return false;
  }
}

// 修复 vendor.js 文件
function fixVendorJs() {
  const vendorPath = 'houserent-uniapp/unpackage/dist/dev/mp-weixin/common/vendor.js';
  
  try {
    if (!fs.existsSync(vendorPath)) {
      console.log('vendor.js 文件不存在，跳过修复');
      return;
    }

    let content = fs.readFileSync(vendorPath, 'utf8');
    
    // 检查是否已经包含 _export_sfc 函数
    if (content.includes('_export_sfc')) {
      console.log('vendor.js 已包含 _export_sfc 相关代码');
      return;
    }

    // 在文件末尾添加 _export_sfc 函数定义
    const newFunction = `

// 修复 common_vendor._export_sfc is not a function 错误
common_vendor._export_sfc = function(component, options) {
  if (options && options.render) {
    component.render = options.render;
  }
  if (options && options.staticRenderFns) {
    component.staticRenderFns = options.staticRenderFns;
  }
  if (options && options.__scopeId) {
    component.__scopeId = options.__scopeId;
  }
  if (options && options.functional) {
    component.functional = options.functional;
  }
  if (options && options.inheritAttrs) {
    component.inheritAttrs = options.inheritAttrs;
  }
  if (options && options.components) {
    component.components = options.components;
  }
  if (options && options.directives) {
    component.directives = options.directives;
  }
  if (options && options.filters) {
    component.filters = options.filters;
  }
  if (options && options.computed) {
    component.computed = options.computed;
  }
  if (options && options.methods) {
    component.methods = options.methods;
  }
  if (options && options.watch) {
    component.watch = options.watch;
  }
  if (options && options.props) {
    component.props = options.props;
  }
  if (options && options.emits) {
    component.emits = options.emits;
  }
  if (options && options.setup) {
    component.setup = options.setup;
  }
  if (options && options.name) {
    component.name = options.name;
  }
  if (options && options.mixins) {
    component.mixins = options.mixins;
  }
  if (options && options.extends) {
    component.extends = options.extends;
  }
  if (options && options.inheritAttrs === false) {
    component.inheritAttrs = false;
  }
  return component;
};

// 兼容性处理：添加其他可能需要的函数
if (!common_vendor.createApp) {
  common_vendor.createApp = function(app) {
    return app;
  };
}

if (!common_vendor.createSSRApp) {
  common_vendor.createSSRApp = function(app) {
    return app;
  };
}

module.exports = common_vendor;
`;

    content += newFunction;
    fs.writeFileSync(vendorPath, content, 'utf8');
    console.log('已修复 vendor.js 文件');
    
  } catch (error) {
    console.error('修复 vendor.js 失败:', error.message);
  }
}

// 主函数
function main() {
  console.log('开始修复 common_vendor._export_sfc is not a function 错误...\n');
  
  // 修复 vendor.js
  fixVendorJs();
  
  console.log('\n修复完成！请重新运行小程序开发工具。');
  console.log('\n如果问题仍然存在，请尝试：');
  console.log('1. 清理小程序开发工具缓存');
  console.log('2. 删除 unpackage 目录并重新编译');
  console.log('3. 检查基础库版本是否兼容');
}

// 如果直接运行此脚本
if (require.main === module) {
  main();
}

module.exports = { fixFile, fixVendorJs, main };
