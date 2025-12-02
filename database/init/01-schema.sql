-- 精简版数据库初始化脚本，仅保留与“用户/权限”相关的表
-- 数据库: ai_platform

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库时指定字符集
CREATE DATABASE IF NOT EXISTS ai_platform
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ai_platform;

-- 1. 用户表（保留用户核心字段）
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `nickname` VARCHAR(50) NULL COMMENT '昵称',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `avatar` VARCHAR(255) NULL COMMENT '头像URL',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 管理员表（可选：保留用于后台管理）
CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) NULL COMMENT '昵称',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `role_id` BIGINT NULL COMMENT '角色ID',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 3. 角色表（简单权限模型）
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) NULL COMMENT '描述',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';



-- ============================================
-- 智能体创作平台 - 数据库初始化脚本
-- 版本: v1.0
-- 日期: 2025-11-19
-- 说明: 该脚本用于创建智能体创作平台的数据库和表结构
-- ============================================

-- 创建数据库


-- ============================================
-- 1. 智能体表 (agent)
-- 对应用户故事: US-001, US-002, US-003, US-004, US-012, US-013
-- ============================================
CREATE TABLE IF NOT EXISTS `agent` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '智能体名称',
    `description` VARCHAR(500) COMMENT '智能体描述',
    `system_prompt` TEXT COMMENT '系统提示词',
    `user_prompt_template` TEXT COMMENT '用户提示词模板',
    `model_config` JSON COMMENT '模型配置: {"provider": "deepseek", "model": "deepseek-chat", "temperature": 0.7}',
    `workflow_id` BIGINT COMMENT '关联的工作流ID',
    `knowledge_base_ids` JSON COMMENT '关联的知识库ID列表: [1, 2, 3]',
    `plugin_ids` JSON COMMENT '关联的插件ID列表: [101, 102]',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft(草稿)/published(已发布)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体表';

-- ============================================
-- 2. 工作流表 (workflow)
-- 对应用户故事: US-008, US-009
-- ============================================
CREATE TABLE IF NOT EXISTS `workflow` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '工作流名称',
    `description` VARCHAR(500) COMMENT '描述',
    `nodes` JSON COMMENT '节点列表: [{"id": "node1", "type": "start", "config": {}}]',
    `edges` JSON COMMENT '边列表: [{"source": "node1", "target": "node2"}]',
    `config` JSON COMMENT '全局配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表';

-- ============================================
-- 3. 知识库表 (knowledge_base)
-- 对应用户故事: US-005
-- ============================================
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `description` VARCHAR(500) COMMENT '描述',
    `vector_db_type` VARCHAR(50) DEFAULT 'milvus' COMMENT '向量库类型: milvus/chroma',
    `chunk_size` INT DEFAULT 512 COMMENT '分块大小(字符数)',
    `chunk_overlap` INT DEFAULT 50 COMMENT '分块重叠(字符数)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ============================================
-- 4. 文档表 (document)
-- 对应用户故事: US-006, US-007
-- ============================================
CREATE TABLE IF NOT EXISTS `document` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `knowledge_base_id` BIGINT NOT NULL COMMENT '所属知识库ID',
    `filename` VARCHAR(255) NOT NULL COMMENT '文件名',
    `content` LONGTEXT COMMENT '文件原始内容(TXT/Markdown)',
    `chunks` JSON COMMENT '分块后的文本列表: ["chunk1", "chunk2"]',
    `vector_ids` JSON COMMENT '向量数据库返回的Vector ID列表: ["vec_1", "vec_2"]',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending(待处理)/processing(处理中)/success(成功)/failed(失败)',
    `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX `idx_kb_id` (`knowledge_base_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- ============================================
-- 5. 插件表 (plugin)
-- 对应用户故事: US-010, US-011
-- 智能体插件系统：支持 OpenAPI 3.0 规范的插件注册和管理
-- ============================================
CREATE TABLE IF NOT EXISTS `plugin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '插件名称（唯一）',
    `description` VARCHAR(500) COMMENT '插件描述',
    `type` VARCHAR(20) DEFAULT 'custom' COMMENT '插件类型: builtin(内置)/custom(自定义)',
    `openapi_spec` JSON COMMENT 'OpenAPI 3.0规范内容（完整的API定义）',
    `config` JSON COMMENT '插件配置信息（API密钥、服务器地址等）',
    `status` VARCHAR(20) DEFAULT 'disabled' COMMENT '插件状态: enabled(启用)/disabled(禁用)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    UNIQUE KEY `uk_name` (`name`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件表';

-- ============================================
-- 显示创建的表
-- ============================================
SHOW TABLES;

-- ============================================
-- 查看表结构（可选，取消注释使用）
-- ============================================
-- DESC agent;
-- DESC workflow;
-- DESC knowledge_base;
-- DESC document;
-- DESC plugin;

-- ============================================
-- 插入示例数据（可选，取消注释使用）
-- ============================================

-- 示例：创建一个测试智能体
-- INSERT INTO `agent` (`name`, `description`, `system_prompt`, `model_config`, `status`)
-- VALUES (
--     '客服助手',
--     '一个友好的客服AI助手',
--     '你是一个专业、友好的客服助手，负责解答用户问题。请保持礼貌和耐心。',
--     '{"provider": "deepseek", "model": "deepseek-chat", "temperature": 0.7, "max_tokens": 2000}',
--     'draft'
-- );

-- 示例：创建一个测试知识库
-- INSERT INTO `knowledge_base` (`name`, `description`, `vector_db_type`, `chunk_size`, `chunk_overlap`)
-- VALUES (
--     '产品文档库',
--     '存储公司产品相关文档',
--     'milvus',
--     512,
--     50
-- );

-- 示例：创建一个测试工作流
-- INSERT INTO `workflow` (`name`, `description`, `nodes`, `edges`)
-- VALUES (
--     '简单问答流程',
--     '一个简单的LLM问答工作流',
--     '[{"id": "start", "type": "start"}, {"id": "llm", "type": "llm", "config": {"prompt": "回答用户问题"}}, {"id": "end", "type": "end"}]',
--     '[{"source": "start", "target": "llm"}, {"source": "llm", "target": "end"}]'
-- );

-- ============================================
-- 脚本执行完成
-- ============================================
SELECT '数据库初始化完成！' AS message;

