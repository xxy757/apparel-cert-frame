-- 将旧 question_bank 迁移到新 question（如已存在数据则跳过）
-- 注意：仅做基础字段迁移，难度/分类/适用认证类型默认置空或默认值

CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(30) NOT NULL COMMENT '题型代码：SINGLE_CHOICE/MULTIPLE_CHOICE/TRUE_FALSE/SHORT_ANSWER',
    content TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_answer VARCHAR(100) NOT NULL COMMENT '正确答案',
    explanation TEXT COMMENT '解析',
    difficulty VARCHAR(20) DEFAULT 'MEDIUM' COMMENT 'EASY/MEDIUM/HARD',
    category VARCHAR(100) COMMENT '题目分类',
    score INT DEFAULT 1 COMMENT '分值',
    applicable_certification VARCHAR(100) COMMENT '适用认证类型（如 designer）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0:停用, 1:启用',
    INDEX idx_question_type (type),
    INDEX idx_question_cert (applicable_certification),
    INDEX idx_question_category (category)
);

INSERT INTO question (
    id,
    type,
    content,
    option_a,
    option_b,
    option_c,
    option_d,
    correct_answer,
    explanation,
    difficulty,
    category,
    score,
    applicable_certification,
    create_time,
    update_time,
    status
)
SELECT
    qb.id,
    CASE qb.question_type
        WHEN 1 THEN 'SINGLE_CHOICE'
        WHEN 2 THEN 'MULTIPLE_CHOICE'
        WHEN 3 THEN 'TRUE_FALSE'
        ELSE 'SINGLE_CHOICE'
    END AS type,
    qb.content,
    CASE WHEN JSON_VALID(qb.options) THEN JSON_UNQUOTE(JSON_EXTRACT(qb.options, '$.A')) ELSE NULL END AS option_a,
    CASE WHEN JSON_VALID(qb.options) THEN JSON_UNQUOTE(JSON_EXTRACT(qb.options, '$.B')) ELSE NULL END AS option_b,
    CASE WHEN JSON_VALID(qb.options) THEN JSON_UNQUOTE(JSON_EXTRACT(qb.options, '$.C')) ELSE NULL END AS option_c,
    CASE WHEN JSON_VALID(qb.options) THEN JSON_UNQUOTE(JSON_EXTRACT(qb.options, '$.D')) ELSE NULL END AS option_d,
    qb.answer AS correct_answer,
    NULL AS explanation,
    'MEDIUM' AS difficulty,
    NULL AS category,
    qb.score,
    NULL AS applicable_certification,
    qb.create_time,
    qb.update_time,
    1 AS status
FROM question_bank qb
WHERE NOT EXISTS (SELECT 1 FROM question);
