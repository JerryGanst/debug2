package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.Target;
import org.example.ai_api.Repository.TargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TargetService {
    @Autowired
    private TargetRepository targetRepository;

    public Target save(Target target) {
        Target temp = targetRepository.findByTargetName(target.getTargetName());
        if (temp != null) {
            target.setId(temp.getId());
        }
        return targetRepository.save(target);
    }

    public List<Target> findAll() {
        return targetRepository.findAll();
    }

    public void delete(Target target) {
        targetRepository.delete(target);
    }

    public Map<String, Target> targetMap() {
        return targetRepository.findAll().stream()
                .collect(Collectors.toMap(Target::getTargetName, target -> target));
    }

}
