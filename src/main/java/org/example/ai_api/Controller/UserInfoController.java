package org.example.ai_api.Controller;

import org.example.ai_api.Annotation.LoginLogger;
import org.example.ai_api.Bean.Model.JwtLoginRepeat;
import org.example.ai_api.Bean.Entity.UserInfo;
import org.example.ai_api.Bean.ApiRepeat.LoginRepeat;
import org.example.ai_api.Bean.ApiRepeat.LuxlinkRepeat;
import org.example.ai_api.Bean.ApiRequests.LuxlinkLogin;
import org.example.ai_api.Bean.Model.LuxlinkData;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Bean.WebRequest.LoginRequest;
import org.example.ai_api.Service.AttemptService;
import org.example.ai_api.Service.UserInfoService;
import org.example.ai_api.Utils.IPCheck;
//import org.example.ai_api.Utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Map;

import io.jsonwebtoken.Claims;

/**
 * 关于用户信息的接口.
 *
 * @author 10353965
 */
@RestController
@RequestMapping("/UserInfo")
public class UserInfoController {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class.getName());
    /**
     * The User info service.
     */
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private AttemptService attemptService;

    /**
     * oa登录.
     *
     * @param loginRequest oa登录请求体
     * @return 登录结果
     */
    @PostMapping("/login")
    @LoginLogger
    public ResultData<LoginRepeat> login(@RequestBody LoginRequest loginRequest) throws Exception {
        logger.info("UserLogin{}", loginRequest.toString());
        attemptService.isLocked(loginRequest.getUserid());
        LoginRepeat loginRepeat = userInfoService.login(loginRequest);
        String token = "";
        String refreshToken = "";
        if("NG".equals(loginRepeat.getClientStatus())){
            attemptService.recordFailedAttempt(loginRequest.getUserid());
            //token = "";
            //refreshToken = "";
        }else if("PASS".equals(loginRepeat.getClientStatus())){
            attemptService.resetFailedAttempts(loginRequest.getUserid());
            //生成一小时有效的临时token
            //token = JwtUtil.generateToken(loginRequest.getUserid(),2*60*1000L);
            //生成一周的有效的刷新用token
            //refreshToken = JwtUtil.generateToken(loginRequest.getUserid(),7*24*60*60*1000L);
        }
        //JwtLoginRepeat jwtLoginRepeat = new JwtLoginRepeat(token,refreshToken,loginRepeat);
        return ResultData.success("登录成功",loginRepeat);
    }

    /**
     * luxlink登录验证.
     *
     * @param luxlinkLogin luxlink登录请求体
     * @return 登录结果
     */
    @PostMapping("/luxlinkLogin")
    @LoginLogger
    public ResultData<LuxlinkData> luxlinkLogin(@RequestBody LuxlinkLogin luxlinkLogin) throws Exception {
        LuxlinkRepeat repeat = userInfoService.luxlinkLogin(luxlinkLogin);
//        String token = "";
//        String refreshToken = "";
//        if(repeat.getData().isValidate()){
//            token = JwtUtil.generateToken(repeat.getData().getUid(),2*60*1000L);
//            refreshToken = JwtUtil.generateToken(repeat.getData().getUid(),7*24*60*60*1000L);
//        }
//        JwtLoginRepeat jwtLoginRepeat = new JwtLoginRepeat(token,refreshToken,repeat.getData());
        return ResultData.success("登录成功", repeat.getData());
    }

    /**
     * 根据用户id(工号)获得用户信息.
     *
     * @param id 用户id(工号)
     * @return 查询的用户信息
     */
    @PostMapping("/getUserInfoById")
    public ResultData<UserInfo> getUserInfoById(@RequestParam(value = "id") @NotNull(message = "id不可为空") String id) {
        logger.info("getUserInfoById{}", id);
        UserInfo userInfo = userInfoService.findById(id);
        return ResultData.success("查询成功", userInfo);
    }

    /**
     * 检查用户ip是否属于内网.
     *
     * @param request 网络请求
     * @return 用户ip
     */
    @GetMapping("/getUserIP")
    public ResultData<Boolean> getUserIp(HttpServletRequest request) {
        try {
            String ip = userInfoService.getUserIP(request);
            logger.info("getUserIP{}", ip);
            return ResultData.success("ip检查成功", IPCheck.isIpInRanges(ip));
            //10.180.84.65
            //10.180.8.215
        } catch (NullPointerException e) {
            return ResultData.fail("ip获取失败", false);
        }
    }

    /**
     * 提供登录公钥.
     * @return 公钥相关信息
     */
    @GetMapping("/getPublicKey")
    public ResultData<Map<String, String>> getPublicKey() {
        return ResultData.success(userInfoService.getPublicKeyResponse());
    }

    /**
     * 刷新token.
     * @param refreshToken 本体的refresh token(放在header中)
     * @return 刷新后的token
     */
//    @PostMapping("/refreshToken")
//    public ResultData<String> getRefreshToken(@RequestHeader("refreshToken") String refreshToken) {
//        try {
//            // 校验refresh token是否过期
//            if (JwtUtil.isTokenExpired(refreshToken)) {
//                return ResultData.fail("refresh token已过期，请重新登录");
//            }
//            // 解析用户信息
//            Claims claims = JwtUtil.parseToken(refreshToken);
//            String userId = claims.getSubject();
//            logger.info("getRefreshToken{}", userId);
//            // 生成新的access token（1小时有效）
//            String newAccessToken = JwtUtil.generateToken(userId, 2*60 * 1000L);
//            return ResultData.success("token刷新成功", newAccessToken);
//        } catch (Exception e) {
//            return ResultData.fail("refresh token无效，请重新登录");
//        }
//    }
}
