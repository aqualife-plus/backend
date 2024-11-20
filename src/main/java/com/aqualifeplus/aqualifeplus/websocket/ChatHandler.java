package com.aqualifeplus.aqualifeplus.websocket;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@NoLogging
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {
    private final MessageQueueService messageQueueService;
    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final Map<WebSocketSession, String> map = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        map.put(session, session.getUri().getPath());
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
            System.err.println("Failed to parse JSON: " + e.getMessage());
            session.sendMessage(new TextMessage("Invalid JSON format"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    // Firebase에서 받은 메시지를 모든 WebSocket 클라이언트에 브로드캐스트
    public void broadcastMessageToClients(Map<String, Object> allData, String dir) throws IOException {
        for (WebSocketSession session : sessions) {
            String sessionPath = map.get(session);
            if (sessionPath != null && sessionPath.equals("/ws/" + allData.get("keys"))) {
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
