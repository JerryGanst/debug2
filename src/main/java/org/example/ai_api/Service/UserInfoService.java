package org.example.ai_api.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ai_api.Bean.Entity.LoginLog;
import org.example.ai_api.Bean.Entity.RSAKeyPair;
import org.example.ai_api.Bean.Entity.UserInfo;
import org.example.ai_api.Bean.ApiRepeat.LoginRepeat;
import org.example.ai_api.Bean.ApiRepeat.LuxlinkRepeat;
import org.example.ai_api.Bean.ApiRequests.OaLoginRequest;
import org.example.ai_api.Bean.ApiRequests.LuxlinkLogin;
import org.example.ai_api.Bean.WebRequest.LoginRequest;
import org.example.ai_api.Exception.ThirdServiceException;
import org.example.ai_api.Exception.NotAccessedException;
import org.example.ai_api.Exception.ThirdPartyDataException;
import org.example.ai_api.Repository.LoginLogRepository;
import org.example.ai_api.Repository.UserInfoViewRepository;
import org.example.ai_api.Utils.RSAUtil;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;


/**
 * The type User info service.
 */
@Service
public class UserInfoService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class.getName());
    @Autowired
    private RSAKeyService rsaKeyService;
    /**
     * The Luxlink key.
     */
    @Value("${luxlink_key}")
    private String luxlink_key;
    /**
     * The Cmd.
     */
    @Value("${cmd}")
    private String cmd;
    /**
     * The Luxlink url.
     */
    @Value("${luxlink_url}")
    private String luxlink_url;
    /**
     * The Oa url.
     */
    @Value("${oa_url}")
    private String oa_url;
    /**
     * The User info view repository.
     */
    @Autowired
    private UserInfoViewRepository userInfoViewRepository;
    @Autowired
    private LoginLogRepository loginLogRepository;

    /**
     * 根据id(工号)获得用户信息.
     *
     * @param id 用户id(工号)
     * @return 用户信息
     */
    public UserInfo findById(String id) {
        logger.info("根据id{}获得用户信息", id);
        return userInfoViewRepository.findById(id);
    }

    /**
     * oa登录.
     *
     * @param request oa登录请求体
     * @return 登陆结果
     */
    public LoginRepeat login(LoginRequest request) throws Exception {
        logger.info("用户{}从OA登录", request.getUserid());
        PrivateKey privateKey = rsaKeyService.getPrivateKeyByRequestId(request.getRequestId());
        if (privateKey == null) {
            throw new NotAccessedException("私钥不存在或已过期");
        }
        OaLoginRequest oaLoginRequest = new OaLoginRequest();
        oaLoginRequest.setUserid(request.getUserid());
        String password = RSAUtil.decrypt(request.getPassword(), privateKey);
        oaLoginRequest.setPassword(password);
        logger.info("oa登录请求体{}", oaLoginRequest);
        return WebClient.builder().defaultHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(oa_url)
                .build()
                .post()
                .body(BodyInserters.fromValue(oaLoginRequest))
                .retrieve()
                .bodyToMono(LoginRepeat.class)
                .blockOptional()
                .orElseThrow(() -> new ThirdPartyDataException("OA登录返回体为空"));
    }

    /**
     * luxlink登录.
     *
     * @param luxlinkLogin luxlink登录请求体
     * @return 登录结果
     * @throws Exception the exception
     */
    public LuxlinkRepeat luxlinkLogin(LuxlinkLogin luxlinkLogin) throws Exception {
        logger.info("luxlink登录");
        validateAccessKey(luxlinkLogin.getAccess_key());
        String response = connectLuxlink(luxlinkLogin.getTokenId());
        return parseResponse(response);
    }

    /**
     * 获得用户ip.
     *
     * @param request http请求
     * @return 字符串格式的用户ip
     */
    public String getUserIP(HttpServletRequest request) {
        logger.info("getUserIP");
        String ip = Utils.getIP(request);
        if (ip == null) {
            throw new NullPointerException("ip is null");
        }
        //logger.info("{}{}", ip, NetUtil.isInnerIP(ip));
        return ip;
    }

    /**
     * 保存用户的登录日志.
     *
     * @param loginLog 登录日志对象
     */
    public void saveLoginLog(LoginLog loginLog) {
        loginLogRepository.save(loginLog);
    }

    /**
     * 根据用户id获得登录日志.
     *
     * @param userId 用户id
     * @return 对应id的登录日志
     */
    public LoginLog getLoginLogByUserId(String userId) {
        return loginLogRepository.findByUserId(userId);
    }

    // 获取公钥并存放到数据库
    public Map<String, String> getPublicKeyResponse() {
        try {
            RSAKeyPair keyPair = rsaKeyService.generateRSAKeyPair();
            Map<String,String> map = new HashMap<>();
            map.put("publicKey", keyPair.getPublicKey());
            map.put("requestId", keyPair.getRequestId());
            map.put("expiresAt", keyPair.getExpiresAt().toString());
            return map;
        } catch (Exception e) {
            throw new RuntimeException("公钥生成失败", e);
        }
    }


    /**
     * 检查luxlink密钥.
     *
     * @param accessKey 密钥
     */
    private void validateAccessKey(String accessKey) {
        if (!luxlink_key.equals(accessKey)) {
            throw new NotAccessedException("key值不匹配");
        }
    }

    /**
     * 链接到luxlink网站.
     *
     * @param tokenId luxlink的tokenId
     * @return luxlink返回的json串
     * @throws Exception the exception
     */
    private String connectLuxlink(String tokenId) throws Exception {
        String query = String.format(
                "cmd=%s&access_key=%s&tokenId=%s",
                URLEncoder.encode(cmd, "UTF-8"),
                URLEncoder.encode(luxlink_key, "UTF-8"),
                URLEncoder.encode(tokenId, "UTF-8")
        );
        URL url = new URL(luxlink_url + "?" + query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            logger.info(conn.getResponseMessage());
            logger.info(String.valueOf(conn.getResponseCode()));
            throw new ThirdServiceException("连接luxlink失败");
        }
    }

    /**
     * 解析luxlink的json串.
     *
     * @param response 字符串格式json串
     * @return 解析后的luxlink返回体
     * @throws Exception the exception
     */
    private LuxlinkRepeat parseResponse(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        LuxlinkRepeat luxlinkRepeat = mapper.readValue(response, LuxlinkRepeat.class);
        if (!luxlinkRepeat.getData().isValidate()) {
            throw new ThirdPartyDataException("luxlink数据解析失败");
        }
        logger.info("用户{}luxlink登录成功", luxlinkRepeat.getData().getUid());
        return luxlinkRepeat;
    }
}
