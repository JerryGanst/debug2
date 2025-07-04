package org.example.ai_api.Service;

import org.example.ai_api.Bean.ApiRequests.*;
import org.example.ai_api.Bean.Entity.Agent;
import org.example.ai_api.Bean.Entity.FileUpload;
import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;
import org.example.ai_api.Bean.Model.*;
import org.example.ai_api.Bean.WebRequest.*;
import org.example.ai_api.Bean.ApiRepeat.*;
import org.example.ai_api.Config.AIConfig;
import org.example.ai_api.Exception.*;
import org.example.ai_api.Repository.KnowledgeFileRepository;
import org.example.ai_api.Strategy.KnowledgeBase.KnowledgeBaseContext;
import org.example.ai_api.Strategy.KnowledgeBase.KnowledgeBaseStrategy;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI Api 相关服务.
 */
@Service
public class ApiService {
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class.getName());
    private final Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();
    @Autowired
    private AIConfig aiConfig;
    @Autowired
    private FileUploadInfoService fileUploadInfoService;
    @Autowired
    private KnowledgeBaseContext knowledgeBaseContext;
    @Autowired
    private AgentService agentService;
    @Autowired
    private FileService fileService;
    @Autowired
    @Qualifier("SyncWebClient")
    private WebClient syncWebClient;
    @Autowired
    @Qualifier("StreamWebClient")
    private WebClient streamWebClient;
    @Autowired
    private KnowledgeFileRepository knowledgeFileRepository;

    /**
     * 知识库问答.
     *
     * @param requestBody 请求结构体
     * @param type        提问种类
     * @param userId      提问的用户id
     * @return 问答结果(流式)
     */
    public Flux<ServerSentEvent<QueryRepeat>> query(QueryRequest requestBody, String type, String userId, int model) {
        KnowledgeBaseStrategy strategy = knowledgeBaseContext.getStrategy(type);
        String url = strategy.getUrl(aiConfig);
        //法务部分模型临时写死
        if ("法务专题".equals(type)) {
            requestBody.setModel("reasoning");
        } else {
            requestBody.setModel(aiConfig.getModels().get(model));
        }
        logger.info("requestBody:{}", requestBody);
        logger.info("url:{}", url);
        return handleStreamRequest(requestBody, url, userId, QueryRepeat.class)
                .filter(qa -> {
                    assert qa.data() != null;
                    return !qa.data().getContent().contains("Result");
                });
    }

    /**
     * AI总结.
     *
     * @param request 请求体
     * @return 总结结果
     */
    public SummaryRepeat summary(SummarizeRequest request) {
        return handleSyncRequest(request, aiConfig.getCategories().get("summary"), SummaryRepeat.class);
    }

    /**
     * AI翻译.
     *
     * @param request 请求体
     * @return 翻译结果
     */
    public TranslateRepeat translate(TranslateRequest request) {
        return handleSyncRequest(request, aiConfig.getCategories().get("translate"), TranslateRepeat.class);
    }

    /**
     * AI翻译.(流式响应)
     *
     * @param request 请求体
     * @return 翻译结果
     */
    public Flux<ServerSentEvent<TranslateRepeat>> translateStream(TranslateRequest request) {
        return handleStreamRequest(request, aiConfig.getCategories().get("translate"), request.getUser_id(),TranslateRepeat.class);
    }

    /**
     * 非流式问答.
     *
     * @param request 问答请求体
     * @return 对话结果(非流式)
     */
    public ChatRepeat chat(ChatRequest request) {
        return handleSyncRequest(request, "http://10.180.39.150:9005/chat", ChatRepeat.class);
    }

    /**
     * 流式问答.
     *
     * @param request 问答请求体
     * @param userId  用户id
     * @return 对话结果(流式)
     */
    public Flux<ServerSentEvent<ChatStreamRepeat>> chatStream(ChatRequest request, String userId, int model) {
        request.setModel(aiConfig.getModels().get(model));
        logger.info("chatStream");
        return handleStreamRequest(request, aiConfig.getCategories().get("chat"), userId, ChatStreamRepeat.class);
    }

    /**
     * 智能体对话.
     * @param request 智能体对话请求体
     * @return 智能体对话结果(流式)
     */
    public Flux<ServerSentEvent<ChatStreamRepeat>> agentChat(AgentChatRequest request) {
        logger.info("agentChat");
        return handleStreamRequest(request, aiConfig.getCategories().get("chat"), request.getUserId(), ChatStreamRepeat.class);
    }

    /**
     * 智能体设定生成
     * @param agentConfig 已有智能体设定
     * @return 智能体设定
     */
    public String generateAgentSetting(AgentConfig agentConfig) {
        logger.info("generateAgentSetting");
        return handleSyncRequest(agentConfig, aiConfig.getCategories().get("agentSetting"), String.class);
    }

    /**
     * 个人知识库问答.
     * @param request 问答请求体
     * @return 个人知识库问答结果
     */
    public String personalRag(PersonalRagRequest request){
        logger.info("personalRag {}",request.getUserId());
        return handleSyncRequest(request, aiConfig.getCategories().get("personalRag"), String.class);
    }

    /**
     * 停止流式返回.
     *
     * @param userId 用户id
     * @return 是否成功停止
     */
    public String stop(String userId) {
        if (userId == null) {
            throw new BadRequestException("用户id不可为空");
        }
        Subscription subscription = subscriptionMap.remove(userId);
        if (subscription != null) {
            subscription.cancel();
            return "请求已停止";
        } else {
            throw new RequestStateConflictException("请求已完成或不存在");
        }
    }

    /**
     * 对前端的翻译请求进行预处理.
     *
     * @param translate 前端翻译请求
     * @return 预处理后的翻译请求
     * @throws Exception 处理过程中的异常
     */
    public TranslateRequest processTranslate(Translate translate) throws Exception {
        TranslateRequest translateRequest = new TranslateRequest();
        translateRequest.setTarget_language(translate.getTarget_language());
        translateRequest.setUser_id(translate.getUser_id());
        FileId fileId = translate.getFile();
        if (fileId == null|| fileId.getFileId() == null||fileId.getFileId().isEmpty()) {
            translateRequest.setSource_text(translate.getSource_text());
        } else {
            String fileContent = fileId.isLocal()?
                    fileUploadInfoService.getContentById(fileId.getFileId()):
                    fileService.getContentById(fileId.getFileId());
            logger.info("翻译文件文本获取完成");
            translateRequest.setSource_text(fileContent);
        }
        return translateRequest;
    }

    /**
     * 对前端的总结请求进行预处理.
     *
     * @param summary 前端翻译请求
     * @return 预处理后的总结请求
     * @throws Exception 处理过程中的异常
     */
    public SummarizeRequest processSummary(Summary summary) throws Exception {
        SummarizeRequest summarizeRequest = new SummarizeRequest();
        summarizeRequest.setUser_id(summary.getUser_id());
        FileId fileId = summary.getFile();
        if (fileId == null|| fileId.getFileId() == null||fileId.getFileId().isEmpty()) {
            summarizeRequest.setQuestion(summary.getQuestion());
        } else {
            String fileContent = fileId.isLocal()?
                    fileUploadInfoService.getContentById(fileId.getFileId()):
                    fileService.getContentById(fileId.getFileId());
            summarizeRequest.setQuestion(fileContent);
        }
        return summarizeRequest;
    }

    /**
     * 对前端的聊天请求进行预处理.
     *
     * @param chatStream 前端聊天请求
     * @return 预处理后的聊天请求
     * @throws Exception 处理过程中的异常
     */
    public ChatRequest processChat(ChatStream chatStream) throws Exception {
        ChatRequest chatRequest = createBaseChatRequest(chatStream);
        processNewFiles(chatStream, chatRequest);
        processHistoricalFiles(chatStream);
        return chatRequest;
    }

    /**
     * 对前端的智能体对话请求进行预处理.
     *
     * @param agentChat 前端聊天请求
     * @return 预处理后的聊天请求
     * @throws Exception 处理过程中的异常
     */
    public AgentChatRequest processAgentChat(AgentChat agentChat) throws Exception {
        AgentChatRequest agentChatRequest = createBaseAgentChatRequest(agentChat);
        processNewFiles(agentChat, agentChatRequest);
        processHistoricalFiles(agentChat);
        return agentChatRequest;
    }

    /**
     * 根据前端请求构造基本的个人知识库问答请求对象
     * @param personalRag 前端个人知识库问答请求
     * @return 基本个人知识库问答请求
     */
    public PersonalRagRequest processPersonalRag(PersonalRag personalRag) {
        PersonalRagRequest personalRagRequest = new PersonalRagRequest(personalRag);
        List<String> filePaths = knowledgeFileRepository.findByUploaderIdAndIsPublic(personalRag.getUserId(), false).stream()
                        .map(KnowledgeFileInfo::getConvertPath).collect(Collectors.toList());
        personalRagRequest.setFilePath(filePaths);
        return personalRagRequest;
    }

    /**
     * 根据前端请求构造基本的智能体对话请求对象
     *
     * @param agentChat 前端智能体聊天请求
     * @return 基本智能体聊天请求
     */
    private AgentChatRequest createBaseAgentChatRequest(AgentChat agentChat) {
        AgentChatRequest agentChatRequest = new AgentChatRequest();
        agentChatRequest.setStream(true);
        agentChatRequest.setUserId(agentChat.getUserId());
        agentChatRequest.setMessages(agentChat.getMessages());
        agentChatRequest.setModel(aiConfig.getModels().get(agentChat.getModel()));
        AgentConfig agentConfig = createAgentConfig(agentService.findAgentById(agentChat.getAgentId()));
        agentChatRequest.setAgentConfig(agentConfig);
        return agentChatRequest;
    }

    /**
     * 根据智能体信息创建提供到AI侧的智能体配置
     *
     * @param agent 智能体
     * @return 智能体配置
     */
    private AgentConfig createAgentConfig(Agent agent) {
        AgentConfig agentConfig = new AgentConfig();
        Persona persona = agent.getPersona();
        agentConfig.setAgentName(persona.getName());
        agentConfig.setAgentRole(persona.getRole());
        agentConfig.setAgentTone(persona.getTone());
        agentConfig.setAgentDescription(persona.getDescription());
        return agentConfig;
    }

    /**
     * 创建基础的聊天请求对象
     */
    private ChatRequest createBaseChatRequest(ChatStream chatStream) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setStream(true);
        chatRequest.setUserId(chatStream.getUserId());
        chatRequest.setMessages(chatStream.getMessages());
        return chatRequest;
    }

    /**
     * 处理新上传的文件-通用对话
     */
    private void processNewFiles(ChatStream chatStream, ChatRequest chatRequest) throws Exception {
        if (chatStream.getFileIds() == null || chatStream.getFileIds().isEmpty()) {
            chatRequest.setFile(null);
            return;
        }
        List<String> fileContents = getFileContents(chatStream.getFileIds());
        chatRequest.setFile(fileContents);
    }

    /**
     * 处理新上传的文件-智能体对话
     */
    private void processNewFiles(AgentChat agentChat, AgentChatRequest agentChatRequest) throws Exception {
        if (agentChat.getFileIds() == null || agentChat.getFileIds().isEmpty()) {
            agentChatRequest.setFile(null);
            return;
        }
        List<String> fileContents = getFileContents(agentChat.getFileIds());
        agentChatRequest.setFile(fileContents);
    }

    /**
     * 获取文件内容列表
     */
    private List<String> getFileContents(List<FileId> fileIds) throws Exception {
        List<String> fileContents = new ArrayList<>();
        for (FileId fileId : fileIds) {
            if (fileId.isLocal()){
                fileContents.add(fileUploadInfoService.getContentById(fileId.getFileId()));
            }else {
                fileContents.add(fileService.getContentById(fileId.getFileId()));
            }
        }
        return fileContents;
    }

    /**
     * 处理历史记录中的文件-通用对话
     */
    private void processHistoricalFiles(ChatStream chatStream) throws Exception {
        for (int index = 0; index < chatStream.getMessages().size() - 1; index++) {
            ChatMessage message = chatStream.getMessages().get(index);
            if (isUserMessageWithFiles(message)) {
                appendFilesToContent(message);
            }
        }
    }

    /**
     * 处理历史记录中的文件-智能体对话
     */
    private void processHistoricalFiles(AgentChat agentChat) throws Exception {
        for (int index = 0; index < agentChat.getMessages().size() - 1; index++) {
            ChatMessage message = agentChat.getMessages().get(index);
            if (isUserMessageWithFiles(message)) {
                appendFilesToContent(message);
            }
        }
    }

    /**
     * 判断是否为包含文件的用户消息
     */
    private boolean isUserMessageWithFiles(ChatMessage message) {
        return "user".equals(message.getRole()) && message.getUploads() != null;
    }

    /**
     * 将文件内容追加到消息中
     */
    private void appendFilesToContent(ChatMessage message) throws Exception {
        List<FileUpload> files = message.getUploads();
        if (files == null || files.isEmpty()) {
            return;
        }
        List<String> fileContents = new ArrayList<>();
        for (FileUpload file : files) {
            if(file.isLocal()){
                fileContents.add(fileUploadInfoService.getContentById(file.getFileId()));
            }else {
                fileContents.add(fileService.getContentById(file.getFileId()));
            }
        }
        String fileSection = buildFileSection(fileContents);
        String originalContent = message.getContent() != null ? message.getContent() : "";
        message.setContent(originalContent + fileSection);
    }

    /**
     * 构建文件内容部分
     */
    private String buildFileSection(List<String> files) {
        StringJoiner fileJoiner = new StringJoiner("\n\n");
        for (int i = 0; i < files.size(); i++) {
            fileJoiner.add("文件" + (i + 1) + "：\n" + files.get(i));
        }

        return "\n\n#####用户提供的文件内容开始#####\n\n"
                + fileJoiner
                + "\n\n#####用户提供的文件内容结束#####\n\n";
    }

    private <T> Flux<ServerSentEvent<T>> handleStreamRequest(
            Object requestBody,
            String url,
            String userId,
            Class<T> responseType
    ) {
        return streamWebClient
                .post()
                .uri(url)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(BodyInserters.fromValue(requestBody))
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMapMany(errorBody ->
                                        // 抛出异常，触发全局错误处理
                                        Flux.error(new StreamApiException(
                                                response.rawStatusCode(),
                                                errorBody
                                        ))
                                );
                    }
                    return response.bodyToFlux(responseType)
                            .map(item -> ServerSentEvent.builder(item).build())
                            .doOnSubscribe(subscription -> subscriptionMap.put(userId, subscription))
                            .doOnTerminate(() -> subscriptionMap.remove(userId))
                            .doOnCancel(() -> subscriptionMap.remove(userId))
                            .doOnError(e -> subscriptionMap.remove(userId));
                });
    }

    private <T> T handleSyncRequest(Object request, String url, Class<T> responseType) {
        return syncWebClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> {
                                    int statusCode = response.rawStatusCode();
                                    logger.warn("请求失败，错误码：{}, 错误信息：{}", statusCode, error);
                                    // 针对5xx错误创建可重试异常
                                    if (statusCode >= 500 && statusCode < 600) {
                                        return Mono.error(new RetryableApiException(statusCode, error));
                                    } else {
                                        return Mono.error(new SyncApiException(statusCode, error));
                                    }
                                })
                )
                .bodyToMono(responseType)
                // 添加重试机制 (只重试5xx错误和网络异常)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable ->
                                throwable instanceof RetryableApiException || throwable instanceof WebClientRequestException
                        )
                        .doAfterRetry(retrySignal ->
                                logger.debug("重试次数: {}", retrySignal.totalRetries())
                        )
                )
                .blockOptional()
                .orElseThrow(() -> new ThirdPartyDataException("返回体为空"));
    }
}
