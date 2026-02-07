-- 兼容当前招聘链路（application + job）的面试外键迁移
-- 原约束：interview.delivery_id -> resume_delivery.id
-- 现状问题：系统实际投递主表是 application，resume_delivery 未同步维护，导致发送面试邀请外键失败

USE apparel_cert;

-- 1) 去掉 interview.delivery_id 对 resume_delivery 的强依赖
ALTER TABLE interview DROP FOREIGN KEY interview_ibfk_1;

-- 2) 放宽 delivery_id，兼容存 application.id 或历史 delivery.id
ALTER TABLE interview MODIFY COLUMN delivery_id BIGINT NULL;

