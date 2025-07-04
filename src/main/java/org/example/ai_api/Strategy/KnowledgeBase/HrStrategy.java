package org.example.ai_api.Strategy.KnowledgeBase;

import org.example.ai_api.Config.AIConfig;
import org.springframework.stereotype.Component;

/**
 * 知识库路径选择策略模式接口具体实现 - 人资行政专题.
 */
@Component
public class HrStrategy implements KnowledgeBaseStrategy{

    @Override
    public String getType() {
        return "人资行政专题";
    }

    @Override
    public String getUrl(AIConfig aiConfig) {
        return aiConfig.getCategories().get("HR");
    }
}
