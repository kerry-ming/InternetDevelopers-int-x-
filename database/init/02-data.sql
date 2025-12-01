-- 精简初始化数据：仅管理员、角色与测试用户（字段与 `01-schema.sql` 对齐）

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库时指定字符集
CREATE DATABASE IF NOT EXISTS ai_platform
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ai_platform;

-- 管理员
INSERT INTO `admin` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 1);

-- 角色
INSERT INTO `role` (`name`, `code`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('普通用户', 'USER', '系统普通用户', 1);

-- 测试用户（字段仅包含 username/password/nickname/email/status）
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张小明', 'test@example.com', 1),
('alice', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李娜', 'alice@example.com', 1);

-- 说明：已移除所有与饮食/知识库/记录相关的插入语句，确保列与表结构一致。



