-- 公告表创建脚本
-- 用途：创建 announcement 表供后台公告管理使用

CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type INT DEFAULT 1 COMMENT '1-系统公告 2-活动通知 3-政策法规 4-行业动态',
    status TINYINT DEFAULT 1 COMMENT '0-草稿 1-已发布 2-已下架',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    publish_time DATETIME COMMENT '发布时间',
    publisher_id BIGINT COMMENT '发布人ID',
    publisher_name VARCHAR(50) COMMENT '发布人姓名',
    views INT DEFAULT 0 COMMENT '浏览次数',
    cover_image VARCHAR(200) COMMENT '封面图片',
    attachment_url VARCHAR(200) COMMENT '附件URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
