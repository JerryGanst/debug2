package org.example.ai_api.Config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "ai")//绑定以ai为前缀的所有配置信息
@Getter
@Setter
@ToString
public class AIConfig {
    private Map<String, String> categories;
    private List<String> models;
}
