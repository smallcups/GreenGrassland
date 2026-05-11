-- GreenGrassland 数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS greengrassland DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE greengrassland;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 活动表
CREATE TABLE IF NOT EXISTS post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(50) NOT NULL COMMENT '活动类型',
    max_people INT NOT NULL COMMENT '最大人数',
    activity_time DATETIME COMMENT '活动时间',
    location VARCHAR(200) COMMENT '活动地点',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_type (type),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 活动报名表
CREATE TABLE IF NOT EXISTS post_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    post_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '报名用户ID',
    register_time DATETIME NOT NULL COMMENT '报名时间',
    UNIQUE KEY uk_post_user (post_id, user_id) COMMENT '唯一索引：防止重复报名',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_register_time (register_time),
    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';
