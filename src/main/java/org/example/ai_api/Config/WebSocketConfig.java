package org.example.ai_api.Config;

import org.example.ai_api.WS.HeartbeatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//WebSocket配置类
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(HeartbeatWebSocketHandler(), "/ws")
                .setAllowedOrigins("*"); // 允许跨域访问
    }

    @Bean
    public HeartbeatWebSocketHandler HeartbeatWebSocketHandler() {
        return new HeartbeatWebSocketHandler();
    }
}