package org.example.ai_api.Strategy.KnowledgeBase;

import org.example.ai_api.Config.AIConfig;
import org.springframework.stereotype.Component;

/**
 * 知识库路径选择策略模式接口具体实现 - 法务专题
 */
@Component
public class LawStrategy implements KnowledgeBaseStrategy{
    @Override
    public String getType() {
        return "法务专题";
    }

    @Override
    public String getUrl(AIConfig aiConfig) {
        return aiConfig.getCategories().get("Law");
    }
}
