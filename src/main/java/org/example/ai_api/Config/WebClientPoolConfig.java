package org.example.ai_api.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientPoolConfig {

    // 非流式客户端连接池
    @Bean(name = "syncConnectionProvider")
    public ConnectionProvider syncConnectionProvider() {
        return ConnectionProvider.builder("sync-pool")
                .maxConnections(200)                 // 最大活跃连接数
                .maxIdleTime(Duration.ofSeconds(30)) // 空闲连接回收时间
                .maxLifeTime(Duration.ofMinutes(5))  // 连接最大生存时间
                .pendingAcquireTimeout(Duration.ofSeconds(60)) // 等待连接超时
                .pendingAcquireMaxCount(500)          // 最大等待队列数量
                .evictInBackground(Duration.ofSeconds(120)) // 后台清理周期
                .build();
    }

    // 流式客户端连接池
    @Bean(name = "streamConnectionProvider")
    public ConnectionProvider streamConnectionProvider() {
        return ConnectionProvider.builder("stream-pool")
                .maxConnections(120)                   // 连接数
                .maxIdleTime(Duration.ofMinutes(3))  // 空闲时间
                .maxLifeTime(Duration.ofMinutes(15)) // 生命周期
                .pendingAcquireTimeout(Duration.ofSeconds(15)) // 等待连接超时
                .pendingAcquireMaxCount(200)                // 最大等待队列
                .fifo()                                    // 先进先出策略（公平）
                .evictInBackground(Duration.ofSeconds(120)) // 后台清理间隔
                .build();
    }

}
