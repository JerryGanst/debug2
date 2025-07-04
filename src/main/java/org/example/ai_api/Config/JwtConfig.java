//package org.example.ai_api.Config;
//
//import org.example.ai_api.Utils.JwtUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
//@Component
//public class JwtConfig {
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @PostConstruct
//    public void init() {
//        JwtUtil.setSecretKey(secret);
//    }
//}
