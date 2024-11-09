package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.service.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.filter.JwtAuthenticationFilter;
import com.aqualifeplus.aqualifeplus.service.JwtService;
import com.aqualifeplus.aqualifeplus.handler.OAuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final OAuthSuccessHandler
            oAuthSuccessHandler;
    private final CustomOAuthUserService customOAuthUserService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/users/login", "/users/signup", "/users/google/login", "/users/naver/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuthUserService))
                        .successHandler(oAuthSuccessHandler) // OAuth2 성공 시 커스텀 핸들러 사용
                );

        return http.build();
    }
}
