package com.fc.config;

import com.fc.mapper.api.TagPublicMapper;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 布隆过滤器配置类
 * 用于防止缓存穿透
 */
@Configuration
@Slf4j
public class BloomFilterConfig {

    @Autowired
    private TagPublicMapper tagPublicMapper;

    /**
     * 标签ID布隆过滤器
     * 预计插入元素数量: 10万 (根据业务量调整)
     * 误判率: 5%
     */
    @Bean("tagIdBloomFilter")
    public BloomFilter<String> tagIdBloomFilter() {
        // 根据预估标签数量设置，这里假设最多10万个标签
        long expectedInsertions = 100000L;
        double fpp = 0.05; // 误判率5%

        BloomFilter<String> bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                fpp
        );

        log.info("初始化标签ID布隆过滤器，预计容量: {}，误判率: {}%",
                expectedInsertions, fpp * 100);
        return bloomFilter;
    }

    /**
     * 应用启动后初始化布隆过滤器
     */
    @PostConstruct
    @Lazy
    public void initBloomFilter() {
        try {
            // 从数据库加载所有已存在的标签ID
            List<Long> allTagIds = tagPublicMapper.selectAllTagIds();

            BloomFilter<String> bloomFilter = tagIdBloomFilter();

            for (Long tagId : allTagIds) {
                bloomFilter.put(String.valueOf(tagId));
            }

            log.info("布隆过滤器初始化完成，已加载 {} 个标签ID到布隆过滤器", allTagIds.size());
            log.info("布隆过滤器当前预估误判率: {}%", bloomFilter.expectedFpp() * 100);
        } catch (Exception e) {
            log.error("初始化布隆过滤器失败", e);
        }
    }
}