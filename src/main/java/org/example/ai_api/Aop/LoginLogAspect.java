package org.example.ai_api.Aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.ai_api.Bean.Entity.LoginLog;
import org.example.ai_api.Bean.Model.LuxlinkData;
import org.example.ai_api.Bean.Entity.UserInfo;
import org.example.ai_api.Bean.ApiRequests.OaLoginRequest;
import org.example.ai_api.Bean.ApiRequests.LuxlinkLogin;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Service.UserInfoService;
import org.example.ai_api.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 针对用户的登录进行日志记录.
 */
@Aspect
@Component
public class LoginLogAspect {
    /**
     * The User info service.
     */
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 定义切入点：标注@LogOper的方法.
     */
    @Pointcut("@annotation(org.example.ai_api.Annotation.LoginLogger)")
    public void logPointCut() {}

    /**
     *  后置通知：仅当方法正常返回时记录.
     *
     * @param joinPoint the join point
     * @param result    the result
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        // 从参数中提取用户名
        String userid = extractUsername(args,result);
        // 构建日志实体
        LoginLog loginLog = new LoginLog();
        //当前用户id在登录日志中是否存在
        if(userInfoService.getLoginLogByUserId(userid) == null) {
            //根据id获得用户信息
            UserInfo userInfo = userInfoService.findById(userid);
            loginLog.setUserId(userid);
            if(userInfo != null) {
                loginLog.setName(userInfo.getName());
                loginLog.setDepartment(userInfo.getDepartment());
            }
        }else {
            loginLog = userInfoService.getLoginLogByUserId(userid);
        }
        loginLog.setLastLoginTime(Utils.getNowDate());
        //保存到数据库
        userInfoService.saveLoginLog(loginLog);
    }

    /**
     *  从参数中提取用户名（需根据实际参数结构调整）.
     *
     * @param args   the args
     * @param result the result
     * @return the string
     */
    private String extractUsername(Object[] args,Object result) {
        for (Object arg : args) {
            if (arg instanceof OaLoginRequest) {
                return ((OaLoginRequest) arg).getUserid();
            } else if (arg instanceof LuxlinkLogin) {
                return ((ResultData<LuxlinkData>) result).getData().getUid();
            }
        }
        return "unknown";
    }
}