package org.example.ai_api.Strategy.KnowledgeFileSort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 知识库文件排序上下文
 */
@Component
public class KnowledgeFileSortContext {
    private final Map<String, KnowledgeFileSortStrategy> strategyMap;

    @Autowired
    public KnowledgeFileSortContext(List<KnowledgeFileSortStrategy> strategyList) {
        strategyMap = strategyList.stream()
                .collect(
                        Collectors.toMap(
                                KnowledgeFileSortStrategy::getType,
                                Function.identity()
                        )
                );
    }

    public KnowledgeFileSortStrategy getStrategy(String type) {
        return strategyMap.get(type);
    }
}
