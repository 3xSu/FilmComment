USE FilmComment_DB;

-- 1. 用户对话历史表（存储Agent对话上下文）
CREATE TABLE IF NOT EXISTS agent_conversation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    message_text TEXT NOT NULL COMMENT '消息内容',
    message_role ENUM('user', 'assistant') NOT NULL COMMENT '消息角色',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_user (session_id, user_id) COMMENT '会话-用户复合索引',
    INDEX idx_user_time (user_id, create_time DESC) COMMENT '用户-时间索引（优化查询）',
    INDEX idx_session_time (session_id, create_time DESC) COMMENT '会话-时间索引（优化查询）',
    CONSTRAINT fk_agent_conversation_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent对话历史表';

-- 2. 用户偏好表（存储AI提取的用户偏好）
CREATE TABLE IF NOT EXISTS agent_user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    favorite_genres JSON COMMENT '喜欢的电影类型',
    preferred_rating_range VARCHAR(20) COMMENT '偏好评分范围',
    disliked_movies JSON COMMENT '不喜欢的电影',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    CONSTRAINT fk_agent_preferences_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent用户偏好表';

-- 3. Agent工具调用记录表（监控工具使用情况）
CREATE TABLE IF NOT EXISTS agent_tool_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    tool_name VARCHAR(100) NOT NULL COMMENT '工具名称',
    tool_parameters JSON COMMENT '工具参数',
    execution_time_ms INT COMMENT '执行时间(毫秒)',
    success BOOLEAN DEFAULT TRUE COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_tool (session_id, tool_name) COMMENT '会话-工具复合索引',
    INDEX idx_tool_time (tool_name, create_time DESC) COMMENT '工具-时间索引（优化查询）',
    INDEX idx_session_time (session_id, create_time DESC) COMMENT '会话-时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent工具调用记录表';

-- 插入示例数据（可选，保持注释状态）
-- INSERT INTO agent_user_preferences (user_id, favorite_genres, preferred_rating_range)
-- VALUES (1, '["动作", "科幻", "悬疑"]', '7-10');

-- 4. 创建视图：用户对话统计（已修正列名错误）
CREATE OR REPLACE VIEW agent_conversation_stats AS
SELECT
    user_id,
    COUNT(*) AS total_messages,
    COUNT(CASE WHEN message_role = 'user' THEN 1 END) AS user_messages,
    COUNT(CASE WHEN message_role = 'assistant' THEN 1 END) AS assistant_messages,
    MAX(create_time) AS last_conversation_time -- 修正：将错误的 `created_time` 改为正确的 `create_time`
FROM agent_conversation_history
GROUP BY user_id;

-- 5. 创建视图：工具使用统计
CREATE OR REPLACE VIEW agent_tool_usage_stats AS
SELECT
    tool_name,
    COUNT(*) AS total_usage,
    AVG(execution_time_ms) AS avg_execution_time,
    SUM(CASE WHEN success = TRUE THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN success = FALSE THEN 1 ELSE 0 END) AS failure_count
FROM agent_tool_usage
GROUP BY tool_name;


USE FilmComment_DB;

-- =============================================
-- 1. 索引优化 - 提升清理性能
-- =============================================

-- 1.1 为agent_conversation_history表添加清理专用索引
-- 优化基于时间的批量删除操作
CREATE INDEX IF NOT EXISTS idx_agent_conversation_cleanup_time 
ON agent_conversation_history(create_time);

-- 优化按用户和时间的查询性能
CREATE INDEX IF NOT EXISTS idx_agent_conversation_cleanup_user_time 
ON agent_conversation_history(user_id, create_time);

-- 1.2 为agent_tool_usage表添加清理相关索引
-- 优化工具使用记录的清理性能
CREATE INDEX IF NOT EXISTS idx_agent_tool_usage_cleanup_time 
ON agent_tool_usage(create_time);

-- 优化按会话和时间的查询
CREATE INDEX IF NOT EXISTS idx_agent_tool_usage_session_time 
ON agent_tool_usage(session_id, create_time);

-- =============================================
-- 2. 清理统计视图 - 监控清理效果
-- =============================================

-- 2.1 对话历史清理统计视图
CREATE OR REPLACE VIEW agent_conversation_cleanup_stats AS
SELECT 
    DATE(create_time) as cleanup_date,
    COUNT(*) as total_records,
    COUNT(CASE WHEN create_time < DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as expired_records,
    COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as active_records,
    ROUND(
        COUNT(CASE WHEN create_time < DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) * 100.0 / COUNT(*), 
        2
    ) as expired_percentage
FROM agent_conversation_history
GROUP BY DATE(create_time)
ORDER BY cleanup_date DESC;

-- 2.2 工具使用记录清理统计视图
CREATE OR REPLACE VIEW agent_tool_usage_cleanup_stats AS
SELECT 
    DATE(create_time) as cleanup_date,
    tool_name,
    COUNT(*) as total_usage,
    COUNT(CASE WHEN create_time < DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as expired_usage,
    COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as active_usage,
    AVG(execution_time_ms) as avg_execution_time,
    SUM(CASE WHEN success = TRUE THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN success = FALSE THEN 1 ELSE 0 END) as failure_count
FROM agent_tool_usage
GROUP BY DATE(create_time), tool_name
ORDER BY cleanup_date DESC, total_usage DESC;

-- 2.3 用户对话统计视图（增强版）
CREATE OR REPLACE VIEW agent_conversation_user_stats AS
SELECT
    user_id,
    COUNT(*) AS total_messages,
    COUNT(CASE WHEN message_role = 'user' THEN 1 END) AS user_messages,
    COUNT(CASE WHEN message_role = 'assistant' THEN 1 END) AS assistant_messages,
    MAX(create_time) AS last_conversation_time,
    MIN(create_time) AS first_conversation_time,
    COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) AS recent_7d_messages,
    COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) AS recent_30d_messages,
    COUNT(CASE WHEN create_time < DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) AS expired_messages
FROM agent_conversation_history
GROUP BY user_id
ORDER BY total_messages DESC;