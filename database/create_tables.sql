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
    duration INT DEFAULT 0 COMMENT '课程时长（分钟）',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    collect_count INT DEFAULT 0 COMMENT '收藏次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用'
);

-- 课程收藏表
CREATE TABLE IF NOT EXISTS course_collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_course (user_id, course_id),
    FOREIGN KEY (user_id) REFERENCES user_personal(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES training_course(id) ON DELETE CASCADE
);

-- 定时投递表
CREATE TABLE IF NOT EXISTS scheduled_delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    scheduled_time DATETIME NOT NULL COMMENT '计划投递时间',
    status TINYINT DEFAULT 0 COMMENT '0:待执行, 1:已执行, 2:已取消, 3:执行失败',
    executed_time DATETIME COMMENT '执行时间',
    result_message VARCHAR(200) COMMENT '执行结果说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_personal(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_position(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resume(id) ON DELETE CASCADE
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


-- ============================================
-- 以下是新增的缺失表
-- ============================================

-- 登录尝试记录表（用于登录失败锁定功能）
CREATE TABLE IF NOT EXISTS login_attempt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    user_type TINYINT NOT NULL COMMENT '用户类型 1:个人用户 2:企业用户',
    fail_count INT DEFAULT 0 COMMENT '失败次数',
    lock_time DATETIME COMMENT '锁定时间',
    last_attempt_time DATETIME COMMENT '最后尝试时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username_type (username, user_type)
);

-- 考试记录表
CREATE TABLE IF NOT EXISTS exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    application_id BIGINT COMMENT '认证申请ID',
    standard_id BIGINT COMMENT '认证标准ID',
    paper_content TEXT COMMENT '试卷内容（JSON格式，包含题目ID列表）',
    user_answers TEXT COMMENT '用户答案（JSON格式）',
    score INT DEFAULT 0 COMMENT '得分',
    total_score INT DEFAULT 100 COMMENT '总分',
    start_time DATETIME COMMENT '考试开始时间',
    end_time DATETIME COMMENT '考试结束时间',
    duration INT COMMENT '考试时长（分钟）',
    status TINYINT DEFAULT 0 COMMENT '状态 0:进行中 1:已完成 2:超时',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_application_id (application_id)
);

-- 专家评审表
CREATE TABLE IF NOT EXISTS expert_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certification_id BIGINT NOT NULL COMMENT '认证ID',
    expert_id BIGINT NOT NULL COMMENT '专家ID',
    expert_name VARCHAR(50) COMMENT '专家姓名',
    review_type TINYINT COMMENT '评审类型：1-理论考试 2-实操评审',
    score INT COMMENT '评审分数',
    grade VARCHAR(10) COMMENT '评审等级：A/B/C/D',
    comment TEXT COMMENT '评审意见',
    status TINYINT DEFAULT 0 COMMENT '评审状态：0-待评审 1-已评审 2-已驳回',
    review_time DATETIME COMMENT '评审时间',
    assign_time DATETIME COMMENT '分配时间',
    attachments VARCHAR(500) COMMENT '附件URL（多个用逗号分隔）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_certification_id (certification_id),
    INDEX idx_expert_id (expert_id)
);

-- 实操任务表
CREATE TABLE IF NOT EXISTS practical_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    requirements TEXT COMMENT '任务要求',
    submission_format VARCHAR(200) COMMENT '提交格式',
    evaluation_criteria TEXT COMMENT '评估标准',
    applicable_certification VARCHAR(100) COMMENT '适用认证类型',
    type VARCHAR(50) COMMENT '任务类型',
    level INT COMMENT '难度等级',
    max_score INT DEFAULT 100 COMMENT '最高分数',
    required_tools VARCHAR(200) COMMENT '所需工具',
    example_url VARCHAR(200) COMMENT '示例URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 简历收藏表（企业收藏简历）
CREATE TABLE IF NOT EXISTS resume_collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    resume_user_id BIGINT NOT NULL COMMENT '简历用户ID',
    intention_level TINYINT COMMENT '意向程度：1-高 2-中 3-低',
    notes TEXT COMMENT '备注',
    tags VARCHAR(200) COMMENT '标签（多个用逗号分隔）',
    collect_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_enterprise_user (enterprise_id, resume_user_id),
    INDEX idx_enterprise_id (enterprise_id)
);

-- 技能认证表
CREATE TABLE IF NOT EXISTS certification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    certification_type VARCHAR(50) COMMENT '认证类型',
    level INT COMMENT '认证等级',
    project_name VARCHAR(100) COMMENT '项目名称',
    theory_score VARCHAR(20) COMMENT '理论成绩',
    practical_score VARCHAR(20) COMMENT '实操成绩',
    review_comment TEXT COMMENT '评审意见',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-审核中 2-已通过 3-未通过',
    apply_time DATETIME COMMENT '申请时间',
    review_time DATETIME COMMENT '评审时间',
    reviewer_id BIGINT COMMENT '评审人ID',
    certificate_url VARCHAR(200) COMMENT '证书URL',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- 岗位表（简化版）
CREATE TABLE IF NOT EXISTS job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    title VARCHAR(100) NOT NULL COMMENT '岗位名称',
    type VARCHAR(50) COMMENT '岗位类型',
    level VARCHAR(20) COMMENT '级别要求',
    salary VARCHAR(50) COMMENT '薪资范围',
    location VARCHAR(100) COMMENT '工作地点',
    education VARCHAR(50) COMMENT '学历要求',
    experience INT COMMENT '经验要求（年）',
    description TEXT COMMENT '岗位描述',
    requirements TEXT COMMENT '岗位要求',
    benefits TEXT COMMENT '福利待遇',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已关闭 1-招聘中',
    views INT DEFAULT 0 COMMENT '浏览次数',
    applications INT DEFAULT 0 COMMENT '申请次数',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status)
);

-- 简历投递/申请表
CREATE TABLE IF NOT EXISTS application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    enterprise_id BIGINT COMMENT '企业ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-已投递 1-已查看 2-面试邀请 3-已拒绝 4-已录用',
    resume_url VARCHAR(200) COMMENT '简历URL',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    feedback TEXT COMMENT '反馈',
    feedback_time DATETIME COMMENT '反馈时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_enterprise_id (enterprise_id)
);
