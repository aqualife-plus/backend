package com.aqualifeplus.aqualifeplus.websocket;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@NoLogging
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {
    private final MessageQueueService messageQueueService;
    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final Map<WebSocketSession, Long> map = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            map.put(session, userId);
            System.out.println("Session established for userId : " + userId);
        } else {
            session.close();
            System.err.println("Email not found in session attributes.");
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        try {
            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(payload, Map.class);

            // JSON 데이터를 String으로 변환
            String type = jsonMap.get("type").toString();
            String content = jsonMap.get("message").toString();
            String formattedMessage = "Type: " + type + ", Message: " + content;

            // 메시지를 RabbitMQ 큐로 전송
            messageQueueService.sendMessageToQueue(map.get(session) + " : " + formattedMessage);
        } catch (Exception e) {
            log.info("Failed to parse JSON: " + e.getMessage());
            session.sendMessage(new TextMessage("Invalid JSON format"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public void messageToClients(Map<String, Object> allData) throws IOException {
        for (WebSocketSession session : sessions) {
            String sessionUserId = String.valueOf(map.get(session));
            log.info("session check : " + (sessionUserId != null && sessionUserId.equals(allData.get("keys"))));
            if (sessionUserId != null && sessionUserId.equals(allData.get("keys"))) {
                session.sendMessage(new TextMessage(convertMapToJson(allData)));
            }
        }
    }

    public static String convertMapToJson(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Map을 JSON 문자열로 변환
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // 변환 실패 시 빈 JSON 반환
        }
    }
}
