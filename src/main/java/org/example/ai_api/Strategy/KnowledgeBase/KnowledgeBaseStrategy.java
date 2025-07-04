package org.example.ai_api.Strategy.KnowledgeBase;

import org.example.ai_api.Config.AIConfig;

/**
 * 知识库路径选择策略模式接口.
 */
public interface KnowledgeBaseStrategy {
    String getType(); // 返回策略对应的类型标识
    String getUrl(AIConfig aiConfig); // 获取URL的核心方法
}
