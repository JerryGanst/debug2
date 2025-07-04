package org.example.ai_api.Strategy.KnowledgeFileSort;

import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;

import java.util.List;

/**
 * 知识库文件排序策略
 */
public interface KnowledgeFileSortStrategy {
    String getType();
    List<KnowledgeFileInfo> sort(List<KnowledgeFileInfo> list);
}
