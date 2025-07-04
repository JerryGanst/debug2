package org.example.ai_api.Strategy.KnowledgeBase;

import org.example.ai_api.Exception.DataNotComplianceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 知识库连接路径策略模式路由选择.
 */
@Component
public class KnowledgeBaseContext {
    private final Map<String, KnowledgeBaseStrategy> strategyMap;

    @Autowired
    public KnowledgeBaseContext(List<KnowledgeBaseStrategy> strategies) {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(KnowledgeBaseStrategy::getType, Function.identity()));
    }

    public KnowledgeBaseStrategy getStrategy(String type) {
        KnowledgeBaseStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new DataNotComplianceException("知识库问答未知类型: " + type);
        }
        return strategy;
    }
}
