package com.fc.service.api;

import java.util.List;
import java.util.Map;

public interface HotService {

    /**
     * 获取标签热度信息
     * @param tagId
     * @return
     */
    Map<String, Object> getTagHotInfo(Long tagId);

    /**
     * 批量获取标签热度信息
     * @param tagIds
     * @return
     */
    Map<Long, Map<String, Object>> batchGetTagHotInfo(List<Long> tagIds);

    /**
     * 清除标签热度缓存
     * @param tagId
     */
    void evictTagHotCache(String tagId);
}
