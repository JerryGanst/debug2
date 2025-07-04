package org.example.ai_api.Config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // 注入连接池提供者
    private final ConnectionProvider syncConnectionProvider;
    private final ConnectionProvider streamConnectionProvider;

    // 通过构造器注入ConnectionProvider
    public WebClientConfig(
            @Qualifier("syncConnectionProvider") ConnectionProvider syncConnectionProvider,
            @Qualifier("streamConnectionProvider") ConnectionProvider streamConnectionProvider) {
        this.syncConnectionProvider = syncConnectionProvider;
        this.streamConnectionProvider = streamConnectionProvider;
    }

    @Bean(name = "SyncWebClient")
    public WebClient syncWebClient() {
        // 创建自定义的编解码器配置，增大缓冲区限制（例如设为10MB）
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies) // 应用自定义的缓冲区配置
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(syncConnectionProvider)
                                .responseTimeout(Duration.ofSeconds(90)) // 整个响应超时
                                .doOnConnected(conn ->
                                        conn.addHandlerLast(new ReadTimeoutHandler(90)) // 读取超时（秒）
                                )
                ))
                .build();
    }

    @Bean(name = "StreamWebClient")
    public WebClient streamWebClient() {
        // 创建自定义的编解码器配置，增大缓冲区限制（例如设为10MB）
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(3 * 1024 * 1024))
                .build();
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies) // 应用自定义的缓冲区配置
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(streamConnectionProvider)
                                .responseTimeout(Duration.ofMinutes(3)) // 整个响应超时
                                .doOnConnected(conn ->
                                        conn.addHandlerLast(new ReadTimeoutHandler(180)) // 读取超时（秒）
                                )
                ))
                .build();
    }
}
