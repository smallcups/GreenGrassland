# GreenGrassland - 校园同好活动平台

一个帮助大学生快速找到附近兴趣相同的人，一起进行打球、桌游、宠物社交、拼活动、学习小组的轻社交校园平台。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- MySQL 8
- Session 认证
- BCrypt 密码加密
- Maven

### 前端
- 原生 HTML + CSS + JavaScript
- Fetch API

## 功能特性

### 用户系统
- ✅ 用户注册
- ✅ 用户登录
- ✅ 用户登出
- ✅ 获取当前用户信息

### 活动系统
- ✅ 发布活动
- ✅ 活动列表
- ✅ 活动详情
- ✅ 我的发布
- ✅ 我的报名
- ✅ 删除活动（只能删除自己的）

### 报名系统
- ✅ 报名活动
- ✅ 取消报名
- ✅ 防止重复报名
- ✅ 限制最大人数

## 快速开始

### 前置要求

1. **Java 17+**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **MySQL 8.0+**
   ```bash
   mysql --version
   ```

### 安装步骤

1. **克隆项目**
   ```bash
   cd /Users/wangqixiang/Desktop/青青草原
   ```

2. **创建数据库**
   
   登录MySQL后执行：
   ```sql
   CREATE DATABASE IF NOT EXISTS greengrassland DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   
   或者执行 `src/main/resources/schema.sql` 文件：
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```

3. **配置数据库连接**
   
   编辑 `src/main/resources/application.yml`，修改数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/greengrassland?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: root  # 修改为你的MySQL用户名
       password: root  # 修改为你的MySQL密码
   ```

4. **编译项目**
   ```bash
   mvn clean compile
   ```

5. **运行项目**
   ```bash
   mvn spring-boot:run
   ```
   
   或者打包后运行：
   ```bash
   mvn clean package
   java -jar target/greengrassland-1.0.0.jar
   ```

6. **访问应用**
   
   - 前端页面：http://localhost:8080/api/index.html
   - API基础路径：http://localhost:8080/api

## API 文档

### 用户相关 API

#### 1. 用户注册
- **URL**: `/api/user/register`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "123456",
    "nickname": "测试用户",
    "email": "test@example.com"
  }
  ```
- **Response**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "email": "test@example.com"
    }
  }
  ```

#### 2. 用户登录
- **URL**: `/api/user/login`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "123456"
  }
  ```

#### 3. 用户登出
- **URL**: `/api/user/logout`
- **Method**: `POST`

#### 4. 获取当前用户
- **URL**: `/api/user/current`
- **Method**: `GET`

### 活动相关 API

#### 1. 发布活动
- **URL**: `/api/post`
- **Method**: `POST`
- **需要登录**: 是
- **Request Body**:
  ```json
  {
    "title": "一起打篮球",
    "content": "周末想找人一起打篮球",
    "type": "BALL_GAME",
    "maxPeople": 10,
    "activityTime": "2024-01-20T14:00:00",
    "location": "体育馆"
  }
  ```

#### 2. 获取活动列表
- **URL**: `/api/post`
- **Method**: `GET`

#### 3. 获取活动详情
- **URL**: `/api/post/{id}`
- **Method**: `GET`

#### 4. 获取我的发布
- **URL**: `/api/post/my/posts`
- **Method**: `GET`
- **需要登录**: 是

#### 5. 获取我的报名
- **URL**: `/api/post/my/registrations`
- **Method**: `GET`
- **需要登录**: 是

#### 6. 删除活动
- **URL**: `/api/post/{id}`
- **Method**: `DELETE`
- **需要登录**: 是
- **权限**: 只能删除自己发布的活动

### 报名相关 API

#### 1. 报名活动
- **URL**: `/api/post/registration/{postId}`
- **Method**: `POST`
- **需要登录**: 是

#### 2. 取消报名
- **URL**: `/api/post/registration/{postId}`
- **Method**: `DELETE`
- **需要登录**: 是

## 活动类型

- `BALL_GAME` - 打球
- `BOARD_GAME` - 桌游
- `PET_SOCIAL` - 宠物社交
- `GROUP_ACTIVITY` - 拼活动
- `STUDY_GROUP` - 学习小组

## 项目结构

```
greengrassland/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/greengrassland/
│   │   │       ├── config/          # 配置类
│   │   │       ├── controller/      # 控制器
│   │   │       ├── dto/             # 数据传输对象
│   │   │       ├── entity/          # 实体类
│   │   │       ├── exception/       # 异常处理
│   │   │       ├── repository/      # 数据访问层
│   │   │       ├── service/         # 业务逻辑层
│   │   │       └── GreenGrasslandApplication.java
│   │   └── resources/
│   │       ├── application.yml      # 应用配置
│   │       ├── schema.sql           # 数据库初始化脚本
│   │       └── static/
│   │           └── index.html       # 前端页面
│   └── test/                        # 测试代码
├── pom.xml                          # Maven配置
└── README.md                        # 项目说明
```

## 开发规范

- 使用 DTO 隔离实体
- 使用统一返回体 `ApiResponse`
- 统一异常处理 `@ControllerAdvice`
- 使用 `Optional` 规范空值处理
- 使用事务 `@Transactional`
- Repository 不写业务逻辑
- Service 处理所有业务规则
- 密码使用 BCrypt 加密
- Session 中只存储 userId

## 注意事项

1. **数据库配置**：确保 MySQL 服务已启动，并正确配置 `application.yml` 中的数据库连接信息。

2. **端口占用**：默认端口为 8080，如果被占用，可在 `application.yml` 中修改 `server.port`。

3. **Session 管理**：应用使用 Session 进行用户认证，前端需要支持 Cookie。

4. **跨域问题**：当前已配置跨域支持，但生产环境建议配置具体的域名。

## 常见问题

### Q: 启动失败，提示数据库连接错误？
A: 检查 MySQL 服务是否启动，以及 `application.yml` 中的数据库配置是否正确。

### Q: 前端页面无法访问？
A: 确保访问地址为 `http://localhost:8080/index.html` 或 `http://localhost:8080/`。

### Q: 登录后无法保持登录状态？
A: 检查浏览器 Cookie 设置，确保允许 Cookie。

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue。
