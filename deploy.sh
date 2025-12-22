#!/bin/bash

# 稳住租房系统部署脚本
# Author: System Administrator
# Version: 1.0.0

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker和Docker Compose
check_prerequisites() {
    log_info "检查系统环境..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    log_success "系统环境检查通过"
}

# 构建前端项目
build_frontend() {
    log_info "构建前端项目..."
    
    cd houserent-front
    
    if [ ! -f "package.json" ]; then
        log_error "前端项目package.json不存在"
        exit 1
    fi
    
    # 安装依赖
    log_info "安装前端依赖..."
    npm install
    
    # 构建生产版本
    log_info "构建生产版本..."
    npm run build
    
    if [ ! -d "dist" ]; then
        log_error "前端构建失败，dist目录不存在"
        exit 1
    fi
    
    cd ..
    log_success "前端项目构建完成"
}

# 构建后端项目
build_backend() {
    log_info "构建后端项目..."
    
    cd houserent-backend
    
    if [ ! -f "pom.xml" ]; then
        log_error "后端项目pom.xml不存在"
        exit 1
    fi
    
    # Maven构建
    log_info "执行Maven构建..."
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    elif [ -f "mvnw" ]; then
        ./mvnw clean package -DskipTests
    else
        log_error "Maven或Maven Wrapper不存在"
        exit 1
    fi
    
    if [ ! -f "target/renting-management-*.jar" ]; then
        log_error "后端构建失败，JAR文件不存在"
        exit 1
    fi
    
    cd ..
    log_success "后端项目构建完成"
}

# 初始化数据库
init_database() {
    log_info "初始化数据库..."
    
    # 创建SQL目录
    mkdir -p houserent-backend/sql
    
    # 创建数据库初始化脚本
    cat > houserent-backend/sql/01-init.sql << 'EOF'
-- 创建数据库
CREATE DATABASE IF NOT EXISTS houserent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER IF NOT EXISTS 'houserent'@'%' IDENTIFIED BY 'houserent123';
GRANT ALL PRIVILEGES ON houserent.* TO 'houserent'@'%';
FLUSH PRIVILEGES;

-- 使用数据库
USE houserent;
EOF
    
    log_success "数据库初始化脚本创建完成"
}

# 创建环境配置文件
create_env_config() {
    log_info "创建环境配置文件..."
    
    cat > .env << 'EOF'
# 数据库配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=houserent
MYSQL_USER=houserent
MYSQL_PASSWORD=houserent123

# Redis配置
REDIS_PASSWORD=

# MinIO配置
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123

# JWT配置
JWT_SECRET=yourSecretKeyForProductionShouldBeVeryLongAndRandomString2024

# 应用配置
SPRING_PROFILES_ACTIVE=prod
EOF
    
    log_success "环境配置文件创建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    # 停止现有服务
    docker-compose down
    
    # 清理旧镜像（可选）
    if [ "$1" = "--clean" ]; then
        log_info "清理旧镜像..."
        docker-compose down --rmi all --volumes --remove-orphans
        docker system prune -f
    fi
    
    # 构建并启动服务
    log_info "构建并启动Docker服务..."
    docker-compose up -d --build
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30
    
    # 检查服务状态
    check_services_health
}

# 检查服务健康状态
check_services_health() {
    log_info "检查服务健康状态..."
    
    # 检查MySQL
    if docker-compose exec mysql mysqladmin ping -h localhost -u root -proot123456 > /dev/null 2>&1; then
        log_success "MySQL服务正常"
    else
        log_error "MySQL服务异常"
        return 1
    fi
    
    # 检查Redis
    if docker-compose exec redis redis-cli ping | grep -q PONG; then
        log_success "Redis服务正常"
    else
        log_error "Redis服务异常"
        return 1
    fi
    
    # 检查MinIO
    if curl -f http://localhost:9000/minio/health/live > /dev/null 2>&1; then
        log_success "MinIO服务正常"
    else
        log_warning "MinIO服务检查失败，可能正在启动中"
    fi
    
    # 检查后端应用
    max_attempts=10
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_success "后端应用正常"
            break
        else
            log_info "等待后端应用启动... ($attempt/$max_attempts)"
            sleep 10
            ((attempt++))
        fi
    done
    
    if [ $attempt -gt $max_attempts ]; then
        log_error "后端应用启动超时"
        return 1
    fi
    
    # 检查Nginx
    if curl -f http://localhost/ > /dev/null 2>&1; then
        log_success "Nginx服务正常"
    else
        log_error "Nginx服务异常"
        return 1
    fi
    
    log_success "所有服务健康检查通过"
}

# 显示访问信息
show_access_info() {
    log_info "部署完成！访问信息如下："
    echo ""
    echo "🌐 前端管理系统: http://localhost/"
    echo "🔗 后端API接口: http://localhost/api/"
    echo "📊 MinIO控制台: http://localhost:9001/ (minioadmin/minioadmin123)"
    echo "🗄️  MySQL数据库: localhost:3306 (houserent/houserent123)"
    echo "🔄 Redis缓存: localhost:6379"
    echo ""
    echo "📝 查看服务状态: docker-compose ps"
    echo "📋 查看服务日志: docker-compose logs -f [service_name]"
    echo "⏹️  停止所有服务: docker-compose down"
    echo ""
}

# 主函数
main() {
    log_info "开始部署稳住租房系统..."
    echo ""
    
    # 检查系统环境
    check_prerequisites
    
    # 构建项目
    build_frontend
    build_backend
    
    # 初始化配置
    init_database
    create_env_config
    
    # 启动服务
    start_services $1
    
    # 显示访问信息
    show_access_info
    
    log_success "部署完成！"
}

# 显示使用帮助
show_help() {
    echo "稳住租房系统部署脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --clean    清理旧镜像和数据卷"
    echo "  --help     显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                 # 正常部署"
    echo "  $0 --clean         # 清理后重新部署"
    echo ""
}

# 解析命令行参数
case "$1" in
    --help|-h)
        show_help
        exit 0
        ;;
    --clean)
        main --clean
        ;;
    "")
        main
        ;;
    *)
        log_error "未知选项: $1"
        show_help
        exit 1
        ;;
esac
