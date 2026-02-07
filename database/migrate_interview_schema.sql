-- 面试表字段补齐脚本
-- 用途：为 interview 表补充企业/岗位/用户等字段，兼容面试管理统计和展示
use apparel_cert;
ALTER TABLE interview
    ADD COLUMN user_id BIGINT NULL AFTER delivery_id,
    ADD COLUMN enterprise_id BIGINT NULL AFTER user_id,
    ADD COLUMN job_id BIGINT NULL AFTER enterprise_id,
    ADD COLUMN evaluation INT NULL AFTER result,
    ADD COLUMN strengths TEXT NULL AFTER evaluation,
    ADD COLUMN weaknesses TEXT NULL AFTER strengths,
    ADD COLUMN hire_status TINYINT DEFAULT 0 AFTER weaknesses,
    ADD COLUMN salary VARCHAR(50) NULL AFTER hire_status,
    ADD COLUMN entry_date VARCHAR(50) NULL AFTER salary,
    ADD COLUMN remark TEXT NULL AFTER entry_date;

CREATE INDEX idx_interview_enterprise ON interview (enterprise_id);
CREATE INDEX idx_interview_job ON interview (job_id);
CREATE INDEX idx_interview_user ON interview (user_id);
