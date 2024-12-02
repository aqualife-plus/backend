package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtHandshakeInterceptor;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import com.aqualifeplus.aqualifeplus.websocket.ChatHandler;
import com.aqualifeplus.aqualifeplus.websocket.MessageQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@NoLogging
@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final MessageQueueService messageQueueService;
    private final JwtService jwtService;
    private final UsersService usersService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/ws")
                .addInterceptors(new JwtHandshakeInterceptor(jwtService, usersService))
                .setAllowedOrigins("*");
    }

    @Bean
    public ChatHandler chatHandler() {
        return new ChatHandler(messageQueueService);  // 생성자에 channel과 messageQueueService 전달
    }
}
