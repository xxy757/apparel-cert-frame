CREATE DATABASE IF NOT EXISTS apparel_cert CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE apparel_cert;

-- 用户表（个人用户）
CREATE TABLE IF NOT EXISTS user_personal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(50),
    id_card VARCHAR(20),
    career_direction VARCHAR(50),
    avatar VARCHAR(200),
    resume_status TINYINT DEFAULT 0 COMMENT '0:未完善, 1:已完善',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用'
);

-- 用户表（企业用户）
CREATE TABLE IF NOT EXISTS user_enterprise (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    company_type VARCHAR(50),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    email VARCHAR(50),
    business_license VARCHAR(200),
    address VARCHAR(200),
    description TEXT,
    logo VARCHAR(200),
    auth_status TINYINT DEFAULT 0 COMMENT '0:待审核, 1:审核通过, 2:审核拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用'
);

-- 评审专家表
CREATE TABLE IF NOT EXISTS expert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(50),
    specialty VARCHAR(50),
    avatar VARCHAR(200),
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 技能认证标准表
CREATE TABLE IF NOT EXISTS certification_standard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_type VARCHAR(50) NOT NULL COMMENT '岗位类型：设计师、打版师、质检员、导购等',
    level VARCHAR(20) NOT NULL COMMENT '等级：初级、中级、高级',
    name VARCHAR(100) NOT NULL,
    description TEXT,
    theory_requirements TEXT COMMENT '理论考试要求',
    practical_requirements TEXT COMMENT '实操要求',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用'
);

-- 技能认证申请表
CREATE TABLE IF NOT EXISTS certification_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    standard_id BIGINT NOT NULL,
    theory_score INT DEFAULT 0 COMMENT '理论考试成绩',
    practical_file VARCHAR(200) COMMENT '实操作品文件路径',
    expert_id BIGINT,
    review_comment TEXT,
    status TINYINT DEFAULT 0 COMMENT '0:待审核, 1:待考试, 2:待评审, 3:通过, 4:不通过',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_personal(id) ON DELETE CASCADE,
    FOREIGN KEY (standard_id) REFERENCES certification_standard(id) ON DELETE CASCADE,
    FOREIGN KEY (expert_id) REFERENCES expert(id) ON DELETE SET NULL
);

-- 技能证书表
CREATE TABLE IF NOT EXISTS certification_certificate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    certificate_no VARCHAR(50) NOT NULL UNIQUE COMMENT '证书编号',
    issue_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    expire_date DATETIME,
    status TINYINT DEFAULT 1 COMMENT '0:过期, 1:有效',
    FOREIGN KEY (application_id) REFERENCES certification_application(id) ON DELETE CASCADE
);

-- 理论题库表
CREATE TABLE IF NOT EXISTS question_bank (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    standard_id BIGINT NOT NULL,
    question_type TINYINT NOT NULL COMMENT '1:单选, 2:多选, 3:判断',
    content TEXT NOT NULL,
    options TEXT COMMENT '选项，JSON格式',
    answer TEXT NOT NULL COMMENT '答案',
    score INT DEFAULT 10 COMMENT '分值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (standard_id) REFERENCES certification_standard(id) ON DELETE CASCADE
);

-- 个人简历表
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    basic_info JSON NOT NULL COMMENT '基本信息，JSON格式',
    education JSON NOT NULL COMMENT '教育背景，JSON格式数组',
    work_experience JSON NOT NULL COMMENT '工作经历，JSON格式数组',
    project_experience JSON NOT NULL COMMENT '项目经验，JSON格式数组',
    skills JSON NOT NULL COMMENT '技能，JSON格式数组',
    certificates JSON NOT NULL COMMENT '证书，JSON格式数组',
    is_public TINYINT DEFAULT 1 COMMENT '0:私有, 1:公开',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_personal(id) ON DELETE CASCADE
);

-- 岗位表
CREATE TABLE IF NOT EXISTS job_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    certification_requirement VARCHAR(100) COMMENT '技能认证要求',
    salary_min DECIMAL(10,2) DEFAULT 0,
    salary_max DECIMAL(10,2) DEFAULT 0,
    work_location VARCHAR(100),
    work_type VARCHAR(20) COMMENT '全职/兼职/实习',
    description TEXT,
    requirements TEXT,
    benefits TEXT,
    status TINYINT DEFAULT 1 COMMENT '0:已结束, 1:招聘中, 2:暂停招聘',
    view_count INT DEFAULT 0,
    apply_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES user_enterprise(id) ON DELETE CASCADE
);

-- 简历投递表
CREATE TABLE IF NOT EXISTS resume_delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    position_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0:已投递, 1:已查看, 2:面试邀请, 3:已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_personal(id) ON DELETE CASCADE,
    FOREIGN KEY (position_id) REFERENCES job_position(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resume(id) ON DELETE CASCADE
);

-- 面试表
CREATE TABLE IF NOT EXISTS interview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    interview_time DATETIME,
    interview_location VARCHAR(200),
    interview_type VARCHAR(20) COMMENT '现场/视频/电话',
    interviewer VARCHAR(50),
    status TINYINT DEFAULT 0 COMMENT '0:待确认, 1:已确认, 2:已完成, 3:已取消',
    feedback TEXT,
    result TINYINT DEFAULT 0 COMMENT '0:待录用, 1:已录用, 2:未录用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (delivery_id) REFERENCES resume_delivery(id) ON DELETE CASCADE
);

-- 消息通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type TINYINT NOT NULL COMMENT '1:个人用户, 2:企业用户, 3:专家',
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read TINYINT DEFAULT 0 COMMENT '0:未读, 1:已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 培训课程表
CREATE TABLE IF NOT EXISTS training_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    cover_image VARCHAR(200),
    course_type VARCHAR(50) COMMENT '课程类型',
    suitable_job_type VARCHAR(50) COMMENT '适合岗位类型',
    suitable_level VARCHAR(20) COMMENT '适合等级',
    price DECIMAL(10,2) DEFAULT 0,
    provider VARCHAR(100),
    url VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用'
);

-- 系统公告表
CREATE TABLE IF NOT EXISTS system_announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:草稿, 1:发布'
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    user_type TINYINT COMMENT '1:个人用户, 2:企业用户, 3:专家, 4:管理员',
    operation_type VARCHAR(50) NOT NULL,
    operation_content TEXT NOT NULL,
    ip_address VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 索引优化
-- 索引将在表结构创建完成后手动添加，以避免版本兼容性问题
