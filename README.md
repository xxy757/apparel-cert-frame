# 服装行业人才技能认证与招聘服务平台

集技能认证、招聘服务、培训推荐于一体的服装行业垂直人才服务平台。
ceshi
## 项目简介
我的测试
本项目旨在解决服装行业企业招聘时技能与岗位不匹配、人才求职时缺乏权威技能认证凭证的核心痛点，通过建立权威技能认证体系，实现人岗精准匹配，降低企业招聘筛选成本，提升求职者就业效率，形成"认证—招聘—培训"的职业生态闭环。

## 技术栈
测试1224
### 后端
- **框架**: Spring Boot 2.7.15 + Java 8
- **数据库**: MySQL 8.0.30
- **ORM**: MyBatis-Plus 3.5.3.1
- **安全认证**: Spring Security + JWT
- **API文档**: Swagger 3.0.0
- **构建工具**: Maven

### 前端
- **框架**: Vue.js 3.3.4
- **构建工具**: Vite 4.4.9
- **UI组件**: Element Plus 2.3.12
- **路由**: Vue Router 4.2.4
- **HTTP客户端**: Axios 1.5.0

## 项目结构

```
apparel-cert-frame/
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/com/apparelcert/
│   │   ├── common/              # 公共类
│   │   ├── config/              # 配置类
│   │   ├── controller/          # REST API 控制器
│   │   ├── entity/              # 实体类
│   │   ├── mapper/              # MyBatis Mapper
│   │   ├── service/             # 业务逻辑层
│   │   └── utils/               # 工具类
│   └── src/main/resources/
│       └── application.yml      # Spring 配置
├── frontend/                     # Vue.js 前端
│   ├── src/
│   │   ├── router/              # 路由配置
│   │   ├── views/               # 页面组件
│   │   │   ├── admin/           # 管理员模块
│   │   │   ├── enterprise/      # 企业用户模块
│   │   │   └── personal/        # 个人用户模块
│   │   └── main.js
│   └── package.json
└── database/                     # 数据库脚本
    └── create_tables.sql         # 数据库表结构
```

## 快速开始

### 环境要求

- JDK 8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 数据库初始化

1. 创建数据库并导入表结构：

```bash
mysql -u root -p < database/create_tables.sql
```

2. 修改数据库连接配置：编辑 [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/apparel_cert
    username: root
    password: your_password
```

### 后端启动

1. 进入后端目录：

```bash
cd backend
```

2. 使用 Maven 编译运行：

```bash
mvn clean install
mvn spring-boot:run
```

3. 访问 Swagger API 文档：http://localhost:8080/swagger-ui.html

### 前端启动

1. 进入前端目录：

```bash
cd frontend
```

2. 安装依赖：

```bash
npm install
```

3. 启动开发服务器：

```bash
npm run dev
```

4. 访问前端页面：http://localhost:3000

### 快捷脚本

项目根目录提供了便捷的批处理脚本（Windows）：

- `start.bat` - 启动前端开发服务器
- `build.bat` - 构建前端生产版本
- `status.bat` - 查看服务运行状态
- `stop.bat` - 停止运行中的服务

## 核心功能模块

### 用户角色

1. **个人用户（求职者）** - 服装行业从业者、职业院校学生
2. **企业用户（招聘方）** - 服装企业 HR、招聘负责人
3. **评审专家** - 技能认证评审专家
4. **系统管理员** - 平台管理人员

### 功能模块

#### 公共模块
- 用户注册登录（个人/企业）
- JWT 身份认证
- 消息通知系统

#### 个人用户功能
- 简历管理（创建、编辑、导出 PDF）
- 技能认证申请与查询
- 在线理论考试
- 实操成果上传
- 岗位搜索与投递
- 智能岗位推荐
- 培训课程推荐

#### 企业用户功能
- 企业认证与主页管理
- 岗位发布与管理
- 人才搜索与筛选
- 面试邀请与管理
- 简历批量操作

#### 技能认证核心模块
- 认证标准管理（按岗位和等级）
- 理论题库管理
- 自动组卷与评分
- 专家评审系统
- 电子证书生成与下载

#### 系统管理
- 用户管理
- 内容管理
- 系统公告
- 操作日志

## 开发状态

当前项目整体进度约 **48%**，核心功能模块已基本完成：

| 模块 | 完成度 |
|------|--------|
| 公共功能模块 | 40% |
| 个人用户功能模块 | 52% |
| 企业用户功能模块 | 55% |
| 技能认证核心模块 | 52% |
| 系统管理模块 | 30% |

详细任务进度请参考 [开发任务文档.md](开发任务文档.md)

## API 接口

后端 API 遵循 RESTful 规范，所有接口统一前缀 `/api`。

主要接口分类：
- `/api/auth/*` - 用户认证相关
- `/api/resume/*` - 简历管理
- `/api/certification/*` - 技能认证
- `/api/job/*` - 岗位管理
- `/api/application/*` - 投递管理
- `/api/interview/*` - 面试管理
- `/api/admin/*` - 系统管理

## 数据库设计

数据库采用 MySQL 8.0，字符集 utf8mb4。主要数据表：

- `user_personal` - 个人用户表
- `user_enterprise` - 企业用户表
- `expert` - 评审专家表
- `certification_standard` - 认证标准表
- `certification_application` - 认证申请表
- `certification_certificate` - 技能证书表
- `question_bank` - 理论题库表
- `resume` - 个人简历表
- `job_position` - 岗位表
- `resume_delivery` - 简历投递表
- `interview` - 面试表
- `notification` - 消息通知表
- `training_course` - 培训课程表
- `system_announcement` - 系统公告表
- `operation_log` - 操作日志表

## 配置说明

### JWT 配置

在 [application.yml](backend/src/main/resources/application.yml) 中配置：

```yaml
apparelcert:
  jwt:
    secret: your-jwt-secret-key    # JWT 密钥
    expiration: 86400000            # 过期时间（毫秒）
```

### 文件上传配置

```yaml
apparelcert:
  upload:
    path: /uploads/                 # 文件上传路径
```

### 邮件服务配置

如需使用邮件功能（找回密码等），配置：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-email-password
```

## 安全特性

- BCrypt 密码加密存储
- JWT Token 身份认证
- Spring Security 权限控制
- SQL 注入防护（MyBatis 参数化查询）
- XSS 防护
- 文件上传类型和大小验证

## 浏览器支持

- Chrome (推荐)
- Firefox
- Edge
- Safari

## 许可证

本项目仅供学习交流使用。

## 联系方式

如有问题或建议，请提交 Issue。

---

**开发状态**: 🟡 进行中

**最后更新**: 2024年
