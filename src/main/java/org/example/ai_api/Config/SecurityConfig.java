//package org.example.ai_api.Config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.example.ai_api.Filter.JwtAuthenticationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Configuration
//public class SecurityConfig {
//
//    @Autowired
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//            .cors()
//            .and()
//            .authorizeRequests(authorize -> authorize
//                .antMatchers(HttpMethod.OPTIONS).permitAll()
//                .antMatchers("/UserInfo/login", "/UserInfo/luxlinkLogin", "/UserInfo/refreshToken", "/UserInfo/getUserIP", "/UserInfo/getPublicKey").permitAll()
//                .anyRequest().authenticated()
//            )
//            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }
//}