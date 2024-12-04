package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtAuthenticationFilter;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh-token",
                                "/users/signup",
                                "/auth/google/login", "/oauth2/authorization/google/**",
                                "/auth/naver/login", "/oauth2/authorization/naver/**",
                                "/users/check-email").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuthUserService))
                        .successHandler(oAuthSuccessHandler)
                );

        return http.build();
    }
}
