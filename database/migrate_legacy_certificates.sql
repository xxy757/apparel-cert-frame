-- 旧证书表迁移脚本
-- 用途：将 certification_certificate 历史数据迁移到 certificate 新表

-- 注意：请先确保 certificate 表已存在（create_tables.sql 已包含）
-- 迁移为幂等：已存在的 certificate_number 会被跳过

INSERT INTO certificate (
    user_id,
    certification_type,
    level,
    certificate_number,
    name,
    id_card,
    issue_date,
    expire_date,
    certificate_url,
    certificate_status,
    issuer,
    description,
    create_time,
    update_time
)
SELECT
    ca.user_id,
    cs.job_type,
    CASE cs.level
        WHEN '初级' THEN 1
        WHEN '中级' THEN 2
        WHEN '高级' THEN 3
        ELSE NULL
    END AS level,
    cc.certificate_no,
    up.name,
    up.id_card,
    cc.issue_date,
    cc.expire_date,
    NULL AS certificate_url,
    CASE cc.status
        WHEN 1 THEN '有效'
        WHEN 0 THEN '过期'
        ELSE '未知'
    END AS certificate_status,
    '服装行业人才技能认证中心' AS issuer,
    cs.name AS description,
    NOW(),
    NOW()
FROM certification_certificate cc
JOIN certification_application ca ON ca.id = cc.application_id
LEFT JOIN certification_standard cs ON cs.id = ca.standard_id
LEFT JOIN user_personal up ON up.id = ca.user_id
WHERE NOT EXISTS (
    SELECT 1 FROM certificate c WHERE c.certificate_number = cc.certificate_no
);

-- 迁移完成后可手动核对数据并考虑删除历史表：
-- DROP TABLE certification_certificate;
