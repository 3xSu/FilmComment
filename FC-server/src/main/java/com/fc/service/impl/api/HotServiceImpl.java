package com.fc.service.impl.api;

import com.fc.service.api.HotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HotServiceImpl implements HotService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private com.fc.mapper.api.TagPublicMapper tagPublicMapper;

    // Redis key 前缀
    private static final String TAG_HOT_KEY_PREFIX = "hot:tag:";
    // 缓存过期时间：6小时
    private static final long CACHE_EXPIRE_HOURS = 6;

    /**
     * 获取标签热度信息
     * @param tagId
     * @return
     */
    @Override
    public Map<String, Object> getTagHotInfo(Long tagId) {
        String cacheKey = TAG_HOT_KEY_PREFIX + tagId;

        // 1. 尝试从缓存获取
        Map<String, Object> hotInfo = getHotInfoFromCache(cacheKey);
        if (hotInfo != null) {
            log.info("从缓存获取标签热度信息: {}", cacheKey);
            return hotInfo;
        }

        // 2. 缓存不存在，从数据源获取
        hotInfo = getTagHotInfoFromDataSource(String.valueOf(tagId));

        // 3. 将结果存入缓存
        if (hotInfo != null && !hotInfo.isEmpty()) {
            cacheHotInfo(cacheKey, hotInfo);
        }

        return hotInfo;
    }

    /**
     * 批量获取标签热度信息
     * @param tagIds
     * @return
     */
    @Override
    public Map<Long, Map<String, Object>> batchGetTagHotInfo(List<Long> tagIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();

        // 批量构建缓存key
        List<String> cacheKeys = tagIds.stream()
                .map(tagId -> TAG_HOT_KEY_PREFIX + tagId)
                .collect(Collectors.toList());

        // 批量从缓存获取
        List<Object> cachedList = redisTemplate.opsForValue().multiGet(cacheKeys);

        // 处理缓存命中和未命中的标签
        List<Long> missTagIds = new ArrayList<>();

        for (int i = 0; i < tagIds.size(); i++) {
            Long tagId = tagIds.get(i);
            Object cached = cachedList.get(i);

            if (cached instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> hotInfo = (Map<String, Object>) cached;
                result.put(tagId, hotInfo);
                log.info("从缓存获取标签热度信息: tagId={}", tagId);
            } else {
                missTagIds.add(tagId);
            }
        }

        // 处理未命中缓存的标签
        if (!missTagIds.isEmpty()) {
            Map<Long, Map<String, Object>> missResults = getTagHotInfoFromDataSourceBatch(missTagIds);
            result.putAll(missResults);

            // 将新获取的数据存入缓存
            for (Long tagId : missTagIds) {
                Map<String, Object> hotInfo = missResults.get(tagId);
                if (hotInfo != null) {
                    String cacheKey = TAG_HOT_KEY_PREFIX + tagId;
                    cacheHotInfo(cacheKey, hotInfo);
                }
            }
        }

        return result;
    }

    /**
     * 清除标签热度缓存
     * @param tagId
     */
    @Override
    public void evictTagHotCache(String tagId) {
        String cacheKey = TAG_HOT_KEY_PREFIX + tagId;
        try {
            redisTemplate.delete(cacheKey);
            log.info("删除标签热度缓存: {}", cacheKey);
        } catch (Exception e) {
            log.error("删除标签热度缓存失败: {}", cacheKey, e);
        }
    }

    /**
     * 从缓存获取热度信息
     * @param cacheKey
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getHotInfoFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Map) {
                return (Map<String, Object>) cached;
            }
        } catch (Exception e) {
            log.error("从Redis获取热度信息失败: {}", cacheKey, e);
        }
        return null;
    }

    /**
     * 缓存热度信息
     * @param cacheKey
     * @param hotInfo
     */
    private void cacheHotInfo(String cacheKey, Map<String, Object> hotInfo) {
        try {
            redisTemplate.opsForValue().set(cacheKey, hotInfo, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("热度信息已缓存: {}", cacheKey);
        } catch (Exception e) {
            log.error("缓存热度信息失败: {}", cacheKey, e);
        }
    }

    /**
     * 从数据源获取标签热度信息
     * @param tagId
     * @return
     */
    private Map<String, Object> getTagHotInfoFromDataSource(String tagId) {
        Map<String, Object> hotInfo = new LinkedHashMap<>();

        try {
            // 获取标签的使用统计
            int totalUsage = getTagTotalUsageCount(tagId);
            int recentUsage = getTagRecentUsageCount(tagId);
            int relatedPostCount = getTagRelatedPostCount(tagId);

            // 计算热度评分
            double hotScore = calculateTagHotScore(totalUsage, recentUsage, relatedPostCount);

            hotInfo.put("tagId", tagId);
            hotInfo.put("totalUsage", totalUsage);
            hotInfo.put("recentUsage", recentUsage);
            hotInfo.put("relatedPostCount", relatedPostCount);
            hotInfo.put("hotScore", hotScore);
            hotInfo.put("updateTime", new Date());

        } catch (Exception e) {
            log.error("获取标签热度信息失败: tagId={}", tagId, e);
            // 返回默认值
            hotInfo.put("tagId", tagId);
            hotInfo.put("totalUsage", 0);
            hotInfo.put("recentUsage", 0);
            hotInfo.put("relatedPostCount", 0);
            hotInfo.put("hotScore", 0.0);
            hotInfo.put("updateTime", new Date());
        }

        return hotInfo;
    }

    /**
     * 批量从数据源获取标签热度信息
     * @param tagIds
     * @return
     */
    private Map<Long, Map<String, Object>> getTagHotInfoFromDataSourceBatch(List<Long> tagIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();

        for (Long tagId : tagIds) {
            Map<String, Object> hotInfo = getTagHotInfoFromDataSource(tagId.toString());
            result.put(tagId, hotInfo);
        }

        return result;
    }

    /**
     * 获取标签总使用次数
     * @param tagId
     * @return
     */
    private int getTagTotalUsageCount(String tagId) {
        try {
            // 查询post_tag表中该标签的使用总次数
            Integer count = tagPublicMapper.countTagTotalUsage(Long.valueOf(tagId));
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取标签总使用次数失败: tagId={}", tagId, e);
            return 0;
        }
    }

    /**
     * 获取标签近期使用次数（最近30天）
     * @param tagId
     * @return
     */
    private int getTagRecentUsageCount(String tagId) {
        try {
            // 查询post_tag表中该标签在最近30天的使用次数
            Integer count = tagPublicMapper.countTagRecentUsage(Long.valueOf(tagId), 30);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取标签近期使用次数失败: tagId={}", tagId, e);
            return 0;
        }
    }

    /**
     * 获取标签关联的帖子数量
     * @param tagId
     * @return
     */
    private int getTagRelatedPostCount(String tagId) {
        try {
            // 查询使用该标签的不重复帖子数量
            Integer count = tagPublicMapper.countTagRelatedPosts(Long.valueOf(tagId));
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取标签关联帖子数量失败: tagId={}", tagId, e);
            return 0;
        }
    }

    /**
     * 计算标签热度评分
     * @param totalUsage 总使用次数
     * @param recentUsage 近期使用次数
     * @param relatedPostCount 关联帖子数量
     * @return
     */
    private double calculateTagHotScore(int totalUsage, int recentUsage, int relatedPostCount) {
        // 热度计算公式，可以根据业务需求调整权重
        // 总使用次数 - 基础热度权重0.5
        // 近期使用 - 时效性权重1.5（更高的权重）
        // 关联帖子数 - 分布广度权重0.8
        return totalUsage * 0.5 + recentUsage * 1.5 + relatedPostCount * 0.8;
    }
}