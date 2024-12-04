package com.aqualifeplus.aqualifeplus.websocket;

import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.NOT_MATCH_NUMBER_FORMAT;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.FishbowlListRealTimeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            messageToClients(getData(String.valueOf(userId)));
            log.info("Session established for userId : " + userId);
        } else {
            session.close();
            log.info("Email not found in session attributes.");
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
                session.sendMessage(new TextMessage(
                        convertFishbowlDTOMapToJson(valueFormatting(allData))));
            }
        }
    }

    private Map<String, FishbowlListRealTimeDto> valueFormatting(Map<String, Object> allData) {
        System.out.println(allData);
        String regex3 = "^([^/]+)/([^/]+)/([^/]+)/([^/]+)$"; // 3개 그룹
        Pattern pattern3 = Pattern.compile(regex3);
        Map<String, FishbowlListRealTimeDto> maps = new HashMap<>();

        for (String key : allData.keySet()) {
            Matcher matcher3 = pattern3.matcher(key);
            if (matcher3.matches()) {
                String s2 = matcher3.group(2);
                String s4 = matcher3.group(4);

                if(!maps.containsKey(s2)) {
                    maps.put(s2, new FishbowlListRealTimeDto());
                }

                switch (s4) {
                    case "tempState" -> maps.get(s2)
                            .setTempState(convertToDouble(allData.get(key)));
                    case "phState" -> maps.get(s2)
                            .setPhState(convertToDouble(allData.get(key)));
                    case "tempStay" -> maps.get(s2)
                            .setTempStay(convertToDouble(allData.get(key)));
                    case "warningMaxPh" -> maps.get(s2)
                            .setWarningMaxPh(convertToDouble(allData.get(key)));
                    case "warningMinPh" -> maps.get(s2)
                            .setWarningMinPh(convertToDouble(allData.get(key)));
                }
            }
        }

        return maps;
    }

    public Map<String, Object> getData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(String.valueOf(userId));
        Map<String, Object> result = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(1); // 동기화를 위한 CountDownLatch

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    result.putAll((Map<String, Object>) snapshot.getValue());
                }
                latch.countDown(); // 작업 완료 후 latch 감소
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown(); // 오류 발생 시에도 latch 감소
                throw new RuntimeException("Firebase data fetch cancelled: " + error.getMessage());
            }
        });

        try {
            latch.await(); // 데이터를 가져올 때까지 대기
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to fetch data from Firebase for userId: " + userId, e);
        }

        System.out.println(result);
        return result;
    }

    public static String convertFishbowlDTOMapToJson(Map<String, ?> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Map을 JSON 문자열로 변환
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // 변환 실패 시 빈 JSON 반환
        }
    }

    private Double convertToDouble(Object value) {
        log.info("Converting value: {} (type: {})", value, value.getClass().getName());
        return switch (value) {
            case Double v -> v;
            case Long l -> l.doubleValue();
            case Integer i -> i.doubleValue();
            default -> throw new CustomException(NOT_MATCH_NUMBER_FORMAT);
        };
    }

}
