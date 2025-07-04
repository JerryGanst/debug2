package org.example.ai_api.Controller;

import org.example.ai_api.Bean.Entity.Agent;
import org.example.ai_api.Bean.Entity.AgentChatInfo;
import org.example.ai_api.Bean.Model.AgentConfig;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Bean.WebRequest.FeedBack;
import org.example.ai_api.Service.AgentService;
import org.example.ai_api.Service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * 智能体相关接口
 *
 * @author 10353965
 */
@RestController
@RequestMapping("/Agent")
public class AgentController {
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    @Autowired
    private AgentService agentService;
    @Autowired
    private ApiService apiService;

    /**
     * 保存或修改智能体
     *
     * @param agent 智能体
     * @return 保存后的智能体
     */
    @PostMapping("/saveAgent")
    public ResultData<Agent> saveAgent(@RequestBody Agent agent) {
        return ResultData.success("保存成功", agentService.saveAgent(agent));
    }

    /**
     * 删除智能体
     *
     * @param agentId 智能体id
     * @return 删除结果
     */
    @PostMapping("/deleteAgentById")
    public ResultData<Agent> deleteAgent(@RequestParam("agentId") String agentId) {
        agentService.deleteAgentById(agentId);
        return ResultData.success("删除成功");
    }

    /**
     * 根据id查询智能体
     *
     * @param agentId 智能体id
     * @return 智能体信息
     */
    @PostMapping("/findAgentById")
    public ResultData<Agent> findAgentById(@RequestParam("agentId") String agentId) {
        return ResultData.success("查询成功", agentService.findAgentById(agentId));
    }

    /**
     * 根据用户id查询智能体
     *
     * @param userId 用户id
     * @param keyword 关键词
     * @return 智能体信息
     */
    @PostMapping("/findAgentByUserId")
    public ResultData<List<Agent>> findAgentByUserId(
            @RequestParam("userId") String userId,
            @RequestParam(value = "keyword",defaultValue = "") String keyword
    ) {
        List<Agent> result = agentService.findAgentByUserId(userId);
        result = agentService.searchAgentByPersonaName(result,keyword);
        result.sort(
                Comparator.comparing(Agent::getLastOperationTime,
                        Comparator.nullsLast(                  // 空值排在最后
                                Comparator.reverseOrder()           // 非空值按时间倒序（最近时间在前）
                        )
                )
        );
        return ResultData.success("查询成功", result);
    }

    /**
     * 根据用户id和智能体id获得聊天记录
     *
     * @param agentId 智能体id
     * @param userId  用户id
     * @return 聊天记录
     */
    @PostMapping("/findAgentChat")
    public ResultData<List<AgentChatInfo>> findAgentChat(@RequestParam("agentId") String agentId, @RequestParam("userId") String userId) {
        return ResultData.success("查询成功", agentService.findAgentChatByAgentIdAndUserId(agentId, userId));
    }

    /**
     * 根据id删除聊天记录
     *
     * @param agentChatId 聊天记录id
     * @param userId      用户id
     * @return 删除结果
     */
    @PostMapping("/deleteAgentChatById")
    public ResultData<String> deleteAgentChatById(@RequestParam("agentChatId") String agentChatId, @RequestParam("userId") String userId) {
        agentService.deleteAgentChatById(agentChatId, userId);
        return ResultData.success("删除成功");
    }

    /**
     * 保存聊天记录
     *
     * @param agentChatInfo 聊天记录
     * @return 保存结果
     */
    @PostMapping("/saveAgentChat")
    public ResultData<AgentChatInfo> saveAgentChat(@RequestBody AgentChatInfo agentChatInfo) {
        return ResultData.success("保存成功", agentService.saveAgentChat(agentChatInfo));
    }

    /**
     * 智能体设定生成
     * @param agentConfig 已有智能体设定
     * @return 智能体设定
     */
    @PostMapping("/generateAgentDescription")
    public ResultData<String> generateAgentSetting(@RequestBody AgentConfig agentConfig) {
        return ResultData.success("生成成功", apiService.generateAgentSetting(agentConfig));
    }

    /**
     * 智能体对话评价
     * @param feedBack 评价
     * @return 评价结果
     */
    @PostMapping("/feedback")
    @ResponseBody
    public ResultData<String> feedback(@RequestBody FeedBack feedBack) {
        logger.info("feedback request: {}", feedBack.toString());
        AgentChatInfo agentChatInfo = agentService.findAgentChatById(feedBack.getId());
        if (agentChatInfo == null) {
            return ResultData.fail("对应的消息记录不存在");
        }
        agentChatInfo.setFeedback(feedBack.getFeedback());
        agentService.saveAgentChat(agentChatInfo);
        return ResultData.success("评价成功");
    }


}
