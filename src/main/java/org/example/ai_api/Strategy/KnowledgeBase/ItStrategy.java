package org.example.ai_api.Strategy.KnowledgeBase;

import org.example.ai_api.Config.AIConfig;
import org.springframework.stereotype.Component;

/**
 * 知识库路径选择策略模式接口具体实现 - IT专题.
 */
@Component
public class ItStrategy implements KnowledgeBaseStrategy{

    @Override
    public String getType() {
        return "IT专题";
    }

    @Override
    public String getUrl(AIConfig aiConfig) {
        return aiConfig.getCategories().get("IT");
    }
}
