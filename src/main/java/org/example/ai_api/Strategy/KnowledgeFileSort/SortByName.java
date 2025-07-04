package org.example.ai_api.Strategy.KnowledgeFileSort;

import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SortByName implements KnowledgeFileSortStrategy{

    @Override
    public String getType() {
        return "name";
    }

    @Override
    public List<KnowledgeFileInfo> sort(List<KnowledgeFileInfo> list) {
        return list.stream()
                .sorted(
                        Comparator.comparing(KnowledgeFileInfo::getFileName)
                )
                .collect(Collectors.toList());
    }
}
