package com.fc.agent.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI Agent监控配置类
 * 配置Agent相关的监控指标和性能追踪
 */
@Configuration
public class AgentMonitoringConfig {
    
    /**
     * Agent响应时间监控
     * 
     * @param registry MeterRegistry实例
     * @return Timer实例
     */
    @Bean
    public Timer agentResponseTimer(MeterRegistry registry) {
        return Timer.builder("agent.response.time")
                .description("Agent响应时间")
                .register(registry);
    }
    
    /**
     * Agent工具调用成功率监控
     * 
     * @param registry MeterRegistry实例
     * @return Counter实例
     */
    @Bean
    public io.micrometer.core.instrument.Counter agentToolSuccessCounter(MeterRegistry registry) {
        return io.micrometer.core.instrument.Counter.builder("agent.tool.success")
                .description("Agent工具调用成功次数")
                .register(registry);
    }
    
    /**
     * Agent工具调用失败率监控
     * 
     * @param registry MeterRegistry实例
     * @return Counter实例
     */
    @Bean
    public io.micrometer.core.instrument.Counter agentToolFailureCounter(MeterRegistry registry) {
        return io.micrometer.core.instrument.Counter.builder("agent.tool.failure")
                .description("Agent工具调用失败次数")
                .register(registry);
    }
    
    /**
     * Agent会话数量监控
     * 
     * @param registry MeterRegistry实例
     * @return Gauge实例
     */
    @Bean
    public io.micrometer.core.instrument.Gauge agentSessionGauge(MeterRegistry registry) {
        // 使用AtomicInteger来跟踪活跃会话数量
        AtomicInteger activeSessions = new AtomicInteger(0);
        
        return io.micrometer.core.instrument.Gauge.builder("agent.sessions.active", activeSessions, AtomicInteger::get)
                .description("活跃的Agent会话数量")
                .register(registry);
    }
}