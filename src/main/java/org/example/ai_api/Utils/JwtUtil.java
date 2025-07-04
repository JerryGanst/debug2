//package org.example.ai_api.Utils;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//    private static String SECRET_KEY; // 建议放到配置文件
//
//    public static void setSecretKey(String key) {
//        SECRET_KEY = key;
//    }
//
//    // 生成token，支持自定义有效时间（单位：毫秒）
//    public static String generateToken(String username, long expireMillis) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expireMillis))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//
//    // 解析token
//    public static Claims parseToken(String token) {
//        return Jwts.parser()
//                .setSigningKey(SECRET_KEY)
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // 校验token是否过期
//    public static boolean isTokenExpired(String token) {
//        Claims claims = parseToken(token);
//        return claims.getExpiration().before(new Date());
//    }
//}
