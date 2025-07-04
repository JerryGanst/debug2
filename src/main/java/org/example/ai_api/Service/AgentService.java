package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.Agent;
import org.example.ai_api.Bean.Entity.AgentChatInfo;
import org.example.ai_api.Exception.DataNotComplianceException;
import org.example.ai_api.Exception.NotFoundException;
import org.example.ai_api.Repository.AgentChatRepository;
import org.example.ai_api.Repository.AgentRepository;
import org.example.ai_api.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//智能体相关服务
@Service
public class AgentService {
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentChatRepository agentChatRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存或修改智能体
     * @param agent 智能体
     * @return 保存后的智能体
     */
    public Agent saveAgent(Agent agent) {
        if(!isAgentNameUnique(agent)){
            throw new DataNotComplianceException("智能体名称已存在");
        }
        if (agent.getId().isEmpty()) {
            agent.setId(null);
        }
        agent.setUpdateTime(Utils.getNowDate());
        if (agent.getCreateTime() == null) {
            agent.setCreateTime(Utils.getNowDate());
        }
        agent.setLastOperationTime(Utils.getNowDate());
        return agentRepository.save(agent);
    }

    /**
     * 根据id查询智能体
     * @param id 智能体id
     * @return 智能体信息
     */
    public Agent findAgentById(String id) {
        return agentRepository.findById(id).orElseThrow(() -> new NotFoundException("不存在对应id的智能体"));
    }

    /**
     * 根据用户id查询智能体
     * @param userId 用户id
     * @return 智能体信息
     */
    public List<Agent> findAgentByUserId(String userId) {
        return agentRepository.findAgentsByUserId(userId);
    }

    /**
     * 根据关键词搜索智能体标题
     * @param agents 智能体列表
     * @param keyword 关键字
     * @return 搜索结果
     */
    public List<Agent> searchAgentByPersonaName(List<Agent> agents,String keyword) {
        if (agents== null||agents.isEmpty()){
            return new ArrayList<>();
        }
        List<Agent> result;
        if("".equals(keyword)){
            result = agents;
        }else {
            result = agents.stream()
                        .filter(agent -> agent.getPersona().getName().contains(keyword))
                        .collect(Collectors.toList());
        }
        return result.stream()
                .sorted(Comparator.comparing(Agent::getUpdateTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 删除智能体，并同步删除智能体聊天记录
     * @param id 智能体id
     */
    public void deleteAgentById(String id) {
        //删除智能体
        agentRepository.deleteById(id);
        //同步删除智能体聊天记录
        agentChatRepository.deleteByAgentId(id);
    }

    /**
     * 根据智能体id和用户id获得聊天记录
     * @param agentId 智能体id
     * @param userId  用户id
     * @return 聊天记录
     */
    public List<AgentChatInfo> findAgentChatByAgentIdAndUserId(String agentId, String userId) {
        return agentChatRepository.findAgentChatsByAgentIdAndUserId(agentId, userId);
    }

    /**
     * 根据id获得智能体聊天记录
     */
    public AgentChatInfo findAgentChatById(String id) {
        return agentChatRepository.findById(id).orElseThrow(() -> new NotFoundException("不存在对应id的智能体聊天记录"));
    }

    /**
     * 删除智能体聊天记录
     * @param agentChatId 聊天记录id
     * @param userId   用户id
     */
    public void deleteAgentChatById(String agentChatId, String userId) {
        AgentChatInfo agentChatInfo = agentChatRepository.findById(agentChatId).orElseThrow(() -> new NotFoundException("不存在对应id的智能体聊天记录"));
        if (!agentChatInfo.getUserId().equals(userId)) {
            throw new NotFoundException("聊天记录不属于该用户");
        }
        agentChatRepository.deleteById(agentChatId);
    }

    /**
     * 保存智能体聊天记录
     * @param agentChatInfo 智能体聊天记录
     * @return 保存结果
     */
    public AgentChatInfo saveAgentChat(AgentChatInfo agentChatInfo) {
        if (agentChatInfo.getId().isEmpty()) {
            agentChatInfo.setId(null);
            agentChatInfo.setCreateTime(Utils.getNowDate());
        }else {
            agentChatInfo.setUpdateTime(Utils.getNowDate());
        }
        Agent agent = agentRepository.findById(agentChatInfo.getAgentId()).orElseThrow(() -> new NotFoundException("不存在对应id的智能体"));
        agent.setLastOperationTime(Utils.getNowDate());
        agentRepository.save(agent);
        return agentChatRepository.save(agentChatInfo);
    }

    public boolean isAgentNameUnique(Agent agent) {
        // 1. 参数检查
        if (agent.getUserId() == null || agent.getPersona() == null) {
            throw new IllegalArgumentException("userId和persona不能为空");
        }

        // 2. 构建查询条件（同一用户下相同名称）
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(agent.getUserId()));
        query.addCriteria(Criteria.where("persona.name").is(agent.getPersona().getName()));

        // 3. 排除当前对象（修改场景）
        if (agent.getId() != null && !agent.getId().isEmpty()) {
            query.addCriteria(Criteria.where("id").ne(agent.getId()));
        }

        // 4. 执行存在性检查
        return !mongoTemplate.exists(query, Agent.class);
    }
}
