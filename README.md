# GreenGrassland - 校园同好活动平台

帮助大学生快速找到附近兴趣相同的人，进行打球、桌游、宠物社交、拼活动、学习小组的轻社交校园平台。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 3.2, Spring Data JPA, Flyway |
| 数据库 | MySQL 8.0, Redis 7 |
| 实时通信 | WebSocket (STOMP over SockJS) |
| 认证 | JWT Token + Session 双通道 |
| 安全 | BCrypt, CSRF, XSS防护, 限流, magic bytes校验 |
| 前端 | 原生 HTML/CSS/JS (312+2062+2746 行) |
| 部署 | Docker + docker-compose |
| CI/CD | GitHub Actions |
| 文档 | Swagger / OpenAPI |

## 功能特性

### 用户系统
- 注册/登录/登出 · JWT Token · 密码修改 · 个人简介(bio) · 兴趣标签 · 头像上传

### 活动系统
- 发布活动 · 活动列表(分页/无限滚动/下拉刷新) · 活动详情 · 搜索(关键词高亮+历史)
- 地理位置(Haversine距离排序) · 活动状态(RECRUITING/FULL/ONGOING/FINISHED/CANCELLED)
- 定时归档 · 满员自动停止报名 · 审核模式 · 系列活动标记

### 社交互动
- 报名/取消 · 参与者列表 · 点赞 · 收藏 · 评论(支持回复)
- 关注/粉丝 · 共同关注 · 屏蔽用户 · 举报内容

### 实时聊天
- 私聊 + 群聊 · WebSocket 实时推送 · 在线状态绿点 · 正在输入提示 · 桌面通知

### 前端体验
- 暗色模式 · 骨架屏 · 图片懒加载 · 热门标签 · 精选活动 · 移动端底部导航
- 网络断线提示 · 接口自动重试 · 表单离开提醒 · 新人引导 · 活动分享
- 通知分类筛选 · 点击跳转 · Gzip 压缩 · Service Worker 离线缓存

### 基础设施
- Docker 一键部署 · Redis 缓存 · Flyway 数据库迁移 · Logback 日志轮转
- 健康检查 · 接口限流 · CSRF/XSS/SQL注入全量防护 · 35 个自动化测试
- GitHub Actions CI/CD · 环境变量配置 · 生产环境 profiles

## 快速开始

### 前置要求
- Java 17+ · Maven 3.6+ · MySQL 8.0+ · Redis 7+

### Docker 部署（推荐）
```bash
cp .env.example .env
# 编辑 .env 修改密码和密钥
docker-compose up -d
# 访问 http://localhost:8080
```

### 本地开发
```bash
# 1. 启动 MySQL 和 Redis
# 2. 配置环境变量 (或使用默认值)
# 3. 运行
mvn spring-boot:run
```

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_HOST | localhost | 数据库地址 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | greengrassland | 数据库名 |
| DB_USER | root | 数据库用户 |
| DB_PASSWORD | - | 数据库密码（生产必改）|
| REDIS_HOST | localhost | Redis地址 |
| REDIS_PORT | 6379 | Redis端口 |
| JWT_SECRET | - | JWT密钥（生产必改，256位+）|

### API 文档
启动后访问: http://localhost:8080/swagger-ui.html

### 运行测试
```bash
mvn test  # 35 个测试
```

## 活动类型

| 类型 | 图标 | 说明 |
|------|------|------|
| BALL_GAME | 🏀 | 打球 |
| BOARD_GAME | 🎲 | 桌游 |
| PET_SOCIAL | 🐾 | 宠物社交 |
| GROUP_ACTIVITY | 🎉 | 拼活动 |
| STUDY_GROUP | 📚 | 学习小组 |

## 项目结构

```
greengrassland/
├── src/main/java/com/greengrassland/
│   ├── config/       # JWT, WebSocket, Redis, CSRF, 限流, 跨域
│   ├── controller/   # 15 个控制器, 75+ API
│   ├── dto/          # 数据传输对象
│   ├── entity/       # JPA 实体 (14个)
│   ├── repository/   # Spring Data JPA (14个)
│   ├── service/      # 业务逻辑
│   ├── component/    # 定时任务
│   └── exception/    # 全局异常处理
├── src/main/resources/
│   ├── db/migration/ # Flyway 迁移脚本
│   ├── static/       # 前端 (HTML + CSS + JS)
│   └── application.yml
├── src/test/         # 35 个测试 (Service + Repository + Controller)
├── docker-compose.yml
├── Dockerfile
├── .github/workflows/ci.yml
└── TODO.md
```
