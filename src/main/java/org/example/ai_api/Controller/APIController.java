package org.example.ai_api.Controller;


import org.example.ai_api.Annotation.RateLimiter;
import org.example.ai_api.Bean.Entity.FileUpload;
import org.example.ai_api.Bean.Model.AgentConfig;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Bean.WebRequest.*;
import org.example.ai_api.Bean.ApiRepeat.*;
import org.example.ai_api.Bean.ApiRequests.*;
import org.example.ai_api.Service.ApiService;
import org.example.ai_api.Service.FileUploadInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AI模型功能接口.
 *
 * @author 10353965
 */
@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/AI")
public class APIController {
    private static final Logger logger = LoggerFactory.getLogger(APIController.class.getName());
    @Autowired
    private ApiService apiService;
    @Autowired
    private FileUploadInfoService fileUploadInfoService;
    /**
     * 知识库问答
     *
     * @param query 问答请求体
     * @return AI模型结果(流式返回)
     */
    @PostMapping(value = "/query")
    @ResponseBody
    @RateLimiter()
    public Flux<ServerSentEvent<QueryRepeat>> query(@RequestBody Query query) {
        logger.info("query:{}", query.toString());
        // 构建请求参数
        QueryRequest requestBody = new QueryRequest();
        requestBody.setQuestion(query.getQuestion());
        requestBody.setUser_id(query.getUser_id());
        return apiService.query(requestBody, query.getType(), query.getUser_id(), query.getModel());
    }

    /**
     * AI总结.
     *
     * @param summary 总结请求体
     * @return 模型总结后的结果
     */
    @PostMapping(value = "/summarize")
    @ResponseBody
    @RateLimiter()
    public ResultData<SummaryRepeat> summarize(@RequestBody Summary summary) throws Exception {
        logger.info("summarize:{}", summary.getUser_id());
        SummarizeRequest summarizeRequest = apiService.processSummary(summary);
        return ResultData.success("总结完成", apiService.summary(summarizeRequest));
    }

//    /**
//     * AI翻译.
//     *
//     * @param translate 翻译请求体
//     * @return 模型翻译后的结果
//     */
//    @PostMapping(value = "/translate")
//    @ResponseBody
//    @RateLimiter()
//    public ResultData<String> translate(@RequestBody Translate translate) throws Exception {
//        logger.info("translate:{}", translate.getUser_id());
//        TranslateRequest translateRequest = apiService.processTranslate(translate);
//        return ResultData.success("翻译成功",apiService.translate(translateRequest).getTranslation_result());
//    }

    /**
     * AI翻译.(流式）
     *
     * @param translate 翻译请求体
     * @return 模型翻译后的结果
     */
    @PostMapping(value = "/translateStream")
    @ResponseBody
    @RateLimiter()
    public Flux<ServerSentEvent<TranslateRepeat>> translateStream(@RequestBody Translate translate) throws Exception {
        logger.info("translate:{}", translate.getUser_id());
        TranslateRequest translateRequest = apiService.processTranslate(translate);
        return apiService.translateStream(translateRequest);
    }

    /**
     * 多轮问答(非流式).
     *
     * @param chatRequest 对话请求体
     * @return 对话结果(非流式)
     */
    @PostMapping(value = "/chat")
    @ResponseBody
    @RateLimiter()
    public ResultData<ChatRepeat> chatRepeat(@RequestBody ChatRequest chatRequest) {
        logger.info("chatRequest:{}", chatRequest.toString());
        return ResultData.success(apiService.chat(chatRequest));
    }

    /**
     * 多轮问答(流式).
     *
     * @param chatStream 对话请求体
     * @return 对话结果(流式)
     */
    @PostMapping(value = "/chatStream")
    @ResponseBody
    @RateLimiter()
    public Flux<ServerSentEvent<ChatStreamRepeat>> chatRepeatStream(@RequestBody ChatStream chatStream) throws Exception {
        logger.info("chatStreamRequest:{}", chatStream.getUserId());
        ChatRequest chatRequest = apiService.processChat(chatStream);
        return apiService.chatStream(chatRequest, chatRequest.getUserId(), chatStream.getModel());
    }

    /**
     * 智能体对话
     *
     * @param agentChat 智能体对话请求体
     * @return 智能体对话结果(流式)
     * @throws Exception 异常
     */
    @PostMapping("/agentChat")
    @RateLimiter()
    public Flux<ServerSentEvent<ChatStreamRepeat>> agentChat(@RequestBody AgentChat agentChat) throws Exception {
        logger.info("agentChat");
        AgentChatRequest request = apiService.processAgentChat(agentChat);
        logger.info("agentChatRequest:{}", request.toString());
        return apiService.agentChat(request);
    }

    @PostMapping("/personalRag")
    @RateLimiter()
    public ResultData<String> personalRag(@RequestBody PersonalRag request) throws Exception {
        logger.info("personalRag");
        PersonalRagRequest personalRagRequest = apiService.processPersonalRag(request);
        return ResultData.success("回答完成",apiService.personalRag(personalRagRequest));
    }

    /**
     * 停止流式返回.
     *
     * @param userId 用户id
     * @return 是否成功停止
     */
    @PostMapping("/stop")
    @RateLimiter()
    public ResultData<String> stopRequest(@RequestParam("userId") String userId) {
        logger.info("stop:{}", userId);
        return ResultData.success(apiService.stop(userId));
    }

    /**
     * 对话过程上传文件.(临时文件夹)
     *
     * @param files 文件本体
     * @return 上传结果，包含上传后的文件信息数组
     * @throws IOException the io exception
     */
    @PostMapping("/fileUpload")
    public ResultData<List<FileUpload>> uploadFile(@RequestPart("files") List<MultipartFile> files,@RequestParam("local") boolean local) throws Exception {
        logger.info("uploadFile");
        List<FileUpload> fileUploads = new ArrayList<>();
        for (MultipartFile file : files) {
            FileUpload fileUpload = fileUploadInfoService.processFile(file,local);
            fileUploads.add(fileUpload);
        }
        return ResultData.success("上传成功", fileUploadInfoService.saveAll(fileUploads));
    }

    /**
     * 根据用户填写的智能体信息生成智能体描述
     * @param agentConfig 用户填写的智能体信息
     * @return 智能体描述
     */
    @PostMapping("/agentSettingGenerate")
    public ResultData<String> agentSettingGenerate(@RequestBody AgentConfig agentConfig) {
        logger.info("agentSettingGenerate{}", agentConfig.toString());
        return ResultData.success("生成成功", apiService.generateAgentSetting(agentConfig));
    }

    @PostMapping("/healthCheck")
    public ResultData<String> healthCheck() {
        return ResultData.success("ok");
    }
}
