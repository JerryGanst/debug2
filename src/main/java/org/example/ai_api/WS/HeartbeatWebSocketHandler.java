package org.example.ai_api.WS;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public class HeartbeatWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActiveTimes = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_INTERVAL = 30000; // 30秒
    private static final long HEARTBEAT_TIMEOUT = 30000; // 30秒
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatWebSocketHandler.class.getName());

    // 使用 Java 原生的调度线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        lastActiveTimes.put(sessionId, System.currentTimeMillis());
        logger.info("Heartbeat session {} established", sessionId);
        // 启动心跳检查任务（全局只启动一次）
        scheduler.scheduleAtFixedRate(
                this::checkHeartbeat,
                0,           // 初始延迟（立即开始）
                HEARTBEAT_INTERVAL,    // 间隔时间
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        if ("heart".equals(payload)) {
            lastActiveTimes.put(session.getId(), System.currentTimeMillis());
            try {
                session.sendMessage(new TextMessage("HEARTBEAT_ACK"));
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        logger.info("Heartbeat session {} closed", session.getId());
        String sessionId = session.getId();
        sessions.remove(sessionId);
        lastActiveTimes.remove(sessionId);
    }

    private void checkHeartbeat() {
        long currentTime = System.currentTimeMillis();
        lastActiveTimes.forEach((sessionId, lastTime) -> {
            if (currentTime - lastTime > HEARTBEAT_TIMEOUT) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.close(CloseStatus.SESSION_NOT_RELIABLE);
                    } catch (IOException e) {
                        logger.info(e.getMessage());
                    }
                }
                sessions.remove(sessionId);
                lastActiveTimes.remove(sessionId);
            }
        });
    }

    // 可选：关闭线程池释放资源
    @PreDestroy
    public void destroy() {
        logger.info("开始关闭heartbeat scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("成功关闭heartbeat scheduler");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.info("heartbeat scheduler 已强制关闭  {}", e.getMessage());
        }
    }
}
