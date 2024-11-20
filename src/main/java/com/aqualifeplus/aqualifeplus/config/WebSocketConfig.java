package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.aqualifeplus.aqualifeplus.websocket.ChatHandler;
import com.aqualifeplus.aqualifeplus.websocket.MessageQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@NoLogging
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private MessageQueueService messageQueueService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/ws/**").setAllowedOrigins("*");
    }

    @Bean
    public ChatHandler chatHandler() {
        return new ChatHandler(messageQueueService);  // 생성자에 channel과 messageQueueService 전달
    }
}
