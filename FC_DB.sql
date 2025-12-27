-- 创建数据库
CREATE DATABASE IF NOT EXISTS FilmComment_DB 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE FilmComment_DB;

-- 1. user（用户表）
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL DEFAULT '',
    role TINYINT NOT NULL DEFAULT 1,
    avatar_url VARCHAR(255) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_create_time (create_time),
    CHECK (role IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. movie（电影表）
CREATE TABLE movie (
    movie_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    duration INT DEFAULT 0,
    intro TEXT,  -- 移除了 DEFAULT ''
    poster_url VARCHAR(255) DEFAULT '',
    release_date DATE,
    avg_rating DECIMAL(3,2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_title (title),
    INDEX idx_release_date (release_date),
    INDEX idx_is_deleted (is_deleted),
    CHECK (avg_rating BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. persons（电影人表）
CREATE TABLE persons (
    person_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_name VARCHAR(50) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_person_name (person_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. movie_person_relation（电影-人关联表）
CREATE TABLE movie_person_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    role_type TINYINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_movie_id (movie_id),
    INDEX idx_person_id (person_id),
    INDEX idx_role_type (role_type),
    UNIQUE KEY uk_movie_person_role (movie_id, person_id, role_type),
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    FOREIGN KEY (person_id) REFERENCES persons(person_id),
    CHECK (role_type IN (1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. post（帖子表）
CREATE TABLE post (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    title VARCHAR(100) DEFAULT '',
    content TEXT,  -- 移除了 DEFAULT ''
    post_type TINYINT NOT NULL,
    content_form TINYINT NOT NULL DEFAULT 1,
    video_url VARCHAR(255) DEFAULT '',
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_movie_id (movie_id),
    INDEX idx_title (title),
    INDEX idx_post_type (post_type),
    INDEX idx_content_form (content_form),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    CHECK (post_type IN (1,2,3,4)),
    CHECK (content_form IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. post_images（帖子图片表）
CREATE TABLE post_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL DEFAULT '',
    sort_order TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    FOREIGN KEY (post_id) REFERENCES post(post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. creative_tag（二创标签表）
CREATE TABLE creative_tag (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(50) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. post_tag（帖子-标签关联表）
CREATE TABLE post_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_post_tag (post_id, tag_id),
    FOREIGN KEY (post_id) REFERENCES post(post_id),
    FOREIGN KEY (tag_id) REFERENCES creative_tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. comment（评论表）
CREATE TABLE comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL DEFAULT '',
    parent_id BIGINT DEFAULT 0,
    like_count INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (post_id) REFERENCES post(post_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. user_movie_relation（用户-电影关系表）
CREATE TABLE user_movie_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    relation_type TINYINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_movie_id (movie_id),
    INDEX idx_relation_type (relation_type),
    UNIQUE KEY uk_user_movie_relation (user_id, movie_id, relation_type),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    CHECK (relation_type IN (1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. movie_rating（电影评分表）
CREATE TABLE movie_rating (
    rating_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    rating_value DECIMAL(2,1) NOT NULL,
    rating_comment VARCHAR(500) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_movie_id (movie_id),
    UNIQUE KEY uk_user_movie (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    CHECK (rating_value BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. collection（收藏表）
CREATE TABLE collection (
    collection_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id),
    UNIQUE KEY uk_user_post (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (post_id) REFERENCES post(post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. ai_record（AI记录表）
CREATE TABLE ai_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT,
    type TINYINT NOT NULL,
    content TEXT, -- 移除了 DEFAULT ''
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_movie_id (movie_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    CHECK (type IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. report（举报表）
CREATE TABLE report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_user_id BIGINT NOT NULL,
    reported_post_id BIGINT,
    reported_comment_id BIGINT,
    report_type TINYINT NOT NULL,
    report_reason VARCHAR(500) DEFAULT '',
    status TINYINT DEFAULT 0,
    admin_id BIGINT,
    handle_remark VARCHAR(500) DEFAULT '',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reporter_user_id (reporter_user_id),
    INDEX idx_reported_post_id (reported_post_id),
    INDEX idx_reported_comment_id (reported_comment_id),
    INDEX idx_report_type (report_type),
    INDEX idx_status (status),
    INDEX idx_admin_id (admin_id),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_create_time (create_time),
    INDEX idx_status_create_time (status, create_time),
    FOREIGN KEY (reporter_user_id) REFERENCES users(user_id),
    FOREIGN KEY (reported_post_id) REFERENCES post(post_id),
    FOREIGN KEY (reported_comment_id) REFERENCES comment(comment_id),
    FOREIGN KEY (admin_id) REFERENCES users(user_id),
    CHECK (status IN (0,1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. admin_operation_log（管理员操作日志表）
CREATE TABLE admin_operation_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    operation_type TINYINT NOT NULL,
    target_id BIGINT NOT NULL,
    operation_content VARCHAR(500) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_target_id (target_id),
    INDEX idx_create_time (create_time),
    INDEX idx_admin_create_time (admin_id, create_time),
    FOREIGN KEY (admin_id) REFERENCES users(user_id),
    CHECK (operation_type IN (1,2,3,4,5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE FilmComment_DB;
ALTER TABLE users 
CHANGE COLUMN password_hash password VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE FilmComment_DB;
ALTER TABLE post 
MODIFY COLUMN content_form TINYINT NOT NULL DEFAULT 1,
ADD CHECK (content_form IN (1, 2));

USE FilmComment_DB;
ALTER TABLE comment ADD COLUMN delete_time datetime DEFAULT NULL COMMENT '逻辑删除时间';

USE FilmComment_DB;
CREATE TABLE IF NOT EXISTS post_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_user_post (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (post_id) REFERENCES post(post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE FilmComment_DB;
ALTER TABLE post ADD COLUMN delete_time datetime DEFAULT NULL COMMENT '逻辑删除时间';

USE FilmComment_DB;
CREATE TABLE comment_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL DEFAULT '',
    sort_order TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_id (comment_id),
    FOREIGN KEY (comment_id) REFERENCES comment(comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE FilmComment_DB;
ALTER TABLE ai_record ADD COLUMN summary_type TINYINT DEFAULT 1 COMMENT '总结类型:1-电影评论总结,2-智能搜索,3-每日推荐,4-口味分析';
CREATE INDEX idx_ai_record_user_movie_type ON ai_record(user_id, movie_id, type, summary_type);
CREATE INDEX idx_ai_record_create_time ON ai_record(create_time DESC);

USE FilmComment_DB;
ALTER TABLE ai_record 
ADD COLUMN post_count INT DEFAULT 0 COMMENT '生成总结时的帖子数量',
ADD COLUMN version INT DEFAULT 1 COMMENT '总结版本号',
ADD COLUMN threshold INT DEFAULT 3 COMMENT '更新阈值',
ADD INDEX idx_movie_post_count (movie_id, post_count);

USE FilmComment_DB;
ALTER TABLE ai_record 
ADD COLUMN post_type TINYINT DEFAULT NULL COMMENT '帖子类型:1-无剧透普通,2-有剧透深度,3-二创无剧透,4-二创有剧透，NULL表示全部类型';
DROP INDEX idx_movie_post_count ON ai_record;
CREATE INDEX idx_movie_post_type ON ai_record(movie_id, post_type, summary_type);
CREATE INDEX idx_movie_post_count ON ai_record(movie_id, post_type, post_count);

USE FilmComment_DB;
ALTER TABLE creative_tag 
ADD COLUMN hot_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '热度评分',
ADD COLUMN usage_count INT DEFAULT 0 COMMENT '使用次数',
ADD INDEX idx_hot_score (hot_score DESC),
ADD INDEX idx_usage_count (usage_count DESC);


USE FilmComment_DB;
CREATE INDEX idx_creative_tag_create_time ON creative_tag(create_time DESC);
CREATE INDEX idx_creative_tag_name_prefix ON creative_tag(tag_name(10));

USE FilmComment_DB;
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type TINYINT NOT NULL COMMENT '通知类型：1-评论通知，2-点赞通知，3-系统通知',
    title VARCHAR(100) NOT NULL DEFAULT '',
    content VARCHAR(500) NOT NULL DEFAULT '',
    related_id BIGINT COMMENT '关联ID（帖子ID/评论ID等）',
    related_type TINYINT COMMENT '关联类型：1-帖子，2-评论，3-用户等',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time DESC),
    INDEX idx_user_read_time (user_id, is_read, create_time DESC),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    CHECK (type IN (1,2,3)),
    CHECK (is_read IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE FilmComment_DB;
ALTER TABLE ai_record 
ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
CREATE INDEX idx_ai_record_update_time ON ai_record(update_time DESC);

USE FilmComment_DB;
ALTER TABLE post ADD COLUMN comment_count INT NOT NULL DEFAULT 0 COMMENT '帖子评论数';