package org.example.ai_api.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StartupLoggerConfig {

    private static final Logger logger = LoggerFactory.getLogger(StartupLoggerConfig.class);
    @Autowired
    private AIConfig aiConfig;
    private final Environment environment;

    public StartupLoggerConfig(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logActiveProfilesAndConfig() {
        String[] activeProfiles = environment.getActiveProfiles();
        Map<String, String> urls = aiConfig.getCategories();
        if (activeProfiles.length == 0) {
            logger.info("No active profile set. Running with DEFAULT configuration.");
        } else {
            logger.info("Active Profiles: {}", String.join(", ", activeProfiles));
        }
        // 2. 输出关键配置信息
        logger.info("MongoDB URL: {}", environment.getProperty("spring.data.mongodb.uri"));
        logger.info("LibreOffice Path: {}", environment.getProperty("libreoffice_convert"));
        logger.info("Minio URL: {}，bucketName：{}", environment.getProperty("minio.endpoint"), environment.getProperty("minio.bucketName"));
        for (String type : urls.keySet()) {
            String value = urls.get(type);
            logger.info("{} = {}", type, value);
        }
    }
}
