package com.fc.service.impl.user;

import com.fc.enums.WebSocketMessageType;
import com.fc.handler.NotificationWebSocketHandler;
import com.fc.mapper.user.PostStatMapper;
import com.fc.service.user.PostStatService;
import com.fc.vo.post.PostStatVO;
import com.fc.vo.websocket.PostStatUpdateVO;
import com.fc.vo.websocket.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PostStatServiceImpl implements PostStatService {

    @Autowired
    private PostStatMapper postStatMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    private static final String POST_STAT_KEY_PREFIX = "post:stat:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 增加帖子浏览量
     * @param postId
     */
    @Override
    @Transactional
    public void incrementViewCount(Long postId) {
        try {
            // 更新数据库
            postStatMapper.incrementViewCount(postId);

            // 更新缓存
            updateCache(postId, "viewCount");

            // 广播更新
            broadcastPostStatUpdate(postId);

            log.debug("帖子浏览量增加: postId={}", postId);
        } catch (Exception e) {
            log.error("增加帖子浏览量失败: postId={}", postId, e);
        }
    }

    /**
     * 更新帖子点赞数
     * @param postId 帖子
     * @param likeCount 点赞数
     */
    @Override
    @Transactional
    public void updateLikeCount(Long postId, Integer likeCount) {
        try {
            // 更新数据库
            postStatMapper.updateLikeCount(postId, likeCount);

            // 更新缓存
            updateCache(postId, "likeCount");

            // 广播更新
            broadcastPostStatUpdate(postId);

            log.debug("帖子点赞数更新: postId={}, likeCount={}", postId, likeCount);
        } catch (Exception e) {
            log.error("更新帖子点赞数失败: postId={}", postId, e);
        }
    }

    /**
     * 更新帖子评论数
     * @param postId 帖子
     * @param commentCount 评论数
     */
    @Override
    @Transactional
    public void updateCommentCount(Long postId, Integer commentCount) {
        try {
            // 更新数据库
            postStatMapper.updateCommentCount(postId, commentCount);

            // 更新缓存
            updateCache(postId, "commentCount");

            // 广播更新
            broadcastPostStatUpdate(postId);

            log.debug("帖子评论数更新: postId={}, commentCount={}", postId, commentCount);
        } catch (Exception e) {
            log.error("更新帖子评论数失败: postId={}", postId, e);
        }
    }

    /**
     * 获取帖子统计数据
     * @param postId 帖子
     * @return
     */
    @Override
    public PostStatVO getPostStats(Long postId) {
        try {
            // 先尝试从缓存获取
            String cacheKey = POST_STAT_KEY_PREFIX + postId;
            PostStatVO cachedStats = (PostStatVO) redisTemplate.opsForValue().get(cacheKey);

            if (cachedStats != null) {
                return cachedStats;
            }

            // 缓存未命中，查询数据库
            PostStatVO stats = postStatMapper.selectPostStats(postId);
            if (stats != null) {
                // 写入缓存
                redisTemplate.opsForValue().set(
                        cacheKey,
                        stats,
                        CACHE_EXPIRE_HOURS,
                        TimeUnit.HOURS
                );
            }

            return stats;
        } catch (Exception e) {
            log.error("获取帖子统计信息失败: postId={}", postId, e);
            return PostStatVO.builder()
                    .postId(postId)
                    .likeCount(0)
                    .commentCount(0)
                    .viewCount(0)
                    .lastUpdateTime(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 广播帖子统计更新
     * @param postId 帖子
     */
    @Override
    public void broadcastPostStatUpdate(Long postId) {
        try {
            PostStatVO stats = getPostStats(postId);
            if (stats != null) {
                PostStatUpdateVO updateVO = PostStatUpdateVO.builder()
                        .postId(postId)
                        .likeCount(stats.getLikeCount())
                        .commentCount(stats.getCommentCount())
                        .viewCount(stats.getViewCount())
                        .timestamp(System.currentTimeMillis())
                        .build();

                WebSocketMessage message = WebSocketMessage.builder()
                        .type(WebSocketMessageType.POST_STAT_UPDATE.getCode())
                        .data(updateVO)
                        .timestamp(LocalDateTime.now())
                        .build();

                // 广播给所有在线用户
                webSocketHandler.broadcastToAll(message);

                log.debug("广播帖子统计更新: postId={}", postId);
            }
        } catch (Exception e) {
            log.error("广播帖子统计更新失败: postId={}", postId, e);
        }
    }

    /**
     * 更新缓存中的特定字段
     * @param postId 帖子
     * @param field 更新的字段名
     */
    private void updateCache(Long postId, String field) {
        try {
            String cacheKey = POST_STAT_KEY_PREFIX + postId;
            PostStatVO cachedStats = (PostStatVO) redisTemplate.opsForValue().get(cacheKey);

            if (cachedStats != null) {
                // 获取最新的数据库数据来更新缓存
                PostStatVO latestStats = postStatMapper.selectPostStats(postId);
                if (latestStats != null) {
                    redisTemplate.opsForValue().set(
                            cacheKey,
                            latestStats,
                            CACHE_EXPIRE_HOURS,
                            TimeUnit.HOURS
                    );
                }
            }
        } catch (Exception e) {
            log.error("更新缓存失败: postId={}, field={}", postId, field, e);
        }
    }
}