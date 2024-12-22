package com.aqualifeplus.aqualifeplus.websocket;

import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.DISCONNECTED_FIREBASE_SERVER;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.FIREBASE_ERROR;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.NETWORK_FIREBASE_ERROR;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.NOT_MATCH_NUMBER_FORMAT;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.PERMISSION_DENIED_FIREBASE_SERVER;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.THREAD_INTERRUPTED;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.firebase.dto.FishbowlRealTimeDto;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.Map.Entry;
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
    private final Map<WebSocketSession, SessionInfoDto> map = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            map.put(session, new SessionInfoDto(userId, "none"));
            messageToClientsFirstConnect(String.valueOf(userId));
            log.info("Session established for userId : " + userId);
        } else {
            session.close(CloseStatus.BAD_DATA);
            log.warn("User ID not found in session attributes.");
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        try {
            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap =
                    objectMapper.readValue(
                            payload,
                            new TypeReference<Map<String, Object>>() {
                            });

            // JSON 데이터를 String으로 변환
            String fishbowlToken = jsonMap.get("fishbowlToken").toString();
            if (fishbowlToken.equals("none")) {
                setFishbowlIdInSession(session, "none");
                log.info("User unSelected fishbowl");
                return;
            }

            setFishbowlIdInSession(session, fishbowlToken);
            log.info("User selected fishbowl: " + fishbowlToken);

            if (checkMessage(session, jsonMap, fishbowlToken)) {
                return;
            }

            String type = jsonMap.get("type").toString();
            String content = jsonMap.get("content").toString();
            String path = map.get(session).getUserId() + "/" + map.get(session).getFishbowlId();
            String formattedMessage = "Type: " + type + ", Message: " + content;

            // 메시지를 RabbitMQ 큐로 전송
            messageQueueService.sendMessageToQueue(session + "<>" + path + "<>" + formattedMessage);
        } catch (Exception e) {
            log.info("Failed to parse JSON: {}" ,e.getMessage());
            session.sendMessage(new TextMessage("Invalid JSON format"));
        }
    }

    private static boolean checkMessage(WebSocketSession session, Map<String, Object> jsonMap, String fishbowlToken)
            throws IOException {
        if (jsonMap.get("type") == null && jsonMap.get("content") == null) {
            log.info("in : {}", fishbowlToken);
            return true;
        } else if (jsonMap.get("type") == null) {
            directMessageToClientForError("type not found in websocket message", session);
            log.error("type not found in websocket message");
            return true;
        } else if (jsonMap.get("content") == null) {
            directMessageToClientForError("content not found in websocket message", session);
            log.error("content not found in websocket message");
            return true;
        }
        return false;
    }

    private static void directMessageToClientForError(String value, WebSocketSession session) throws IOException {
        Map<String, String> m = new HashMap<>();
        m.put("error", value);
        System.out.println(convertFishbowlDTOMapToJson(m));
        session.sendMessage(new TextMessage(convertFishbowlDTOMapToJson(m)));
    }

    private void setFishbowlIdInSession(WebSocketSession session, String fishbowlToken) {
        SessionInfoDto sessionInfo = map.get(session);
        if (sessionInfo != null) {
            sessionInfo.setFishbowlId(fishbowlToken); // 어항 ID 업데이트
            map.put(session, sessionInfo); // 변경된 DTO 저장
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 에러 로그 기록
        log.error("WebSocket transport error for session ID: " + session.getId(), exception);

        // 세션 종료
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }

        // 세션과 관련된 리소스 정리
        sessions.remove(session);
        map.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public void messageToClients(Map<String, Object> allData) throws IOException {
        for (WebSocketSession session : sessions) {
            String sessionUserId = String.valueOf(map.get(session).getUserId());
            log.info("session check : " + (sessionUserId != null && sessionUserId.equals(allData.get("userId"))));
            // 세션 유무 & 세션아이디랑 현재 데이터의 userId랑 같은 지 check
            if (sessionUserId != null && sessionUserId.equals(allData.get("userId"))) {
                sendMessageToClient(allData, session);
            }
        }
    }

    private void sendMessageToClient(Map<String, Object> allData, WebSocketSession session) throws IOException {
        if (map.get(session).getFishbowlId().equals("none")) {
            session.sendMessage(new TextMessage(
                    convertFishbowlDTOMapToJson(valueFormattingUseFishbowlList(allData))));
        } else {
            session.sendMessage(new TextMessage(
                    convertFishbowlDTOMapToJson(valueFormattingUseFishbowlList(allData),
                            map.get(session).getFishbowlId())));
        }
    }

    public void sendMessage(String sessionStr, String message) throws IOException {
        for (WebSocketSession session : sessions) {
            log.info("session check : " + (sessionStr != null && sessionStr.equals(session.toString())));
            // 세션 유무 & 세션아이디랑 현재 데이터의 userId랑 같은 지 check
            if (sessionStr != null && sessionStr.equals(session.toString())) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    public void messageToClientsFirstConnect(String userId) throws IOException {
        for (WebSocketSession session : sessions) {
            String sessionUserId = String.valueOf(map.get(session).getUserId());
            Map<String, FishbowlRealTimeDto> data = getData(userId);
            log.info("session check : " + (sessionUserId != null && sessionUserId.equals(userId)));
            if (sessionUserId != null && sessionUserId.equals(userId)) {
                session.sendMessage(new TextMessage(
                        convertFishbowlDTOMapToJson(data)));
            }
        }
    }

    private Map<String, FishbowlRealTimeDto> valueFormattingUseFishbowlList(Map<String, Object> allData) {
        String regex3 = "^([^/]+)/([^/]+)/([^/]+)/([^/]+)$"; // 3개 그룹
        Pattern pattern3 = Pattern.compile(regex3);
        Map<String, FishbowlRealTimeDto> maps = new HashMap<>();

        for (String key : allData.keySet()) {
            Matcher matcher3 = pattern3.matcher(key);
            if (matcher3.matches()) {
                String fishbowlId = matcher3.group(2);
                String fishbowlDept = matcher3.group(4);

                if (!maps.containsKey(fishbowlId)) {
                    maps.put(fishbowlId, new FishbowlRealTimeDto());
                }

                switch (fishbowlDept) {
                    case "tempState" -> maps.get(fishbowlId)
                            .setTempState(convertToDouble(allData.get(key)));
                    case "phState" -> maps.get(fishbowlId)
                            .setPhState(convertToDouble(allData.get(key)));
                    case "tempStay" -> maps.get(fishbowlId)
                            .setTempStay(convertToDouble(allData.get(key)));
                    case "warningMaxPh" -> maps.get(fishbowlId)
                            .setWarningMaxPh(convertToDouble(allData.get(key)));
                    case "warningMinPh" -> maps.get(fishbowlId)
                            .setWarningMinPh(convertToDouble(allData.get(key)));
                    case "co2State" -> maps.get(fishbowlId)
                            .setCo2State((Boolean) allData.get(key));
                    case "lightState" -> maps.get(fishbowlId)
                            .setLightState((Boolean) allData.get(key));
                    case "filterState" -> maps.get(fishbowlId)
                            .setFilterState((Long) allData.get(key));
                }
            }
        }

        return maps;
    }

    public Map<String, FishbowlRealTimeDto> getData(String userId) {
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
                latch.countDown(); // 오류 발생 시에도 latch 감소?
                firebaseCancelledErrorHandle(error);
            }
        });

        try {
            latch.await(); // 데이터를 가져올 때까지 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 스레드 상태 복원
            throw new CustomException(THREAD_INTERRUPTED);
        }

        Map<String, FishbowlRealTimeDto> maps = new HashMap<>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String fishbowlId = entry.getKey();
            maps.put(fishbowlId, ObjectMapToDtoMap(entry));
        }

        return maps;
    }

    private static void firebaseCancelledErrorHandle(DatabaseError error) {
        switch (error.getCode()) {
            case DatabaseError.DISCONNECTED:
                throw new CustomException(DISCONNECTED_FIREBASE_SERVER);
            case DatabaseError.PERMISSION_DENIED:
                throw new CustomException(PERMISSION_DENIED_FIREBASE_SERVER);
            case DatabaseError.NETWORK_ERROR:
                throw new CustomException(NETWORK_FIREBASE_ERROR);
            default:
                throw new CustomException(FIREBASE_ERROR);
        }
    }

    private FishbowlRealTimeDto ObjectMapToDtoMap(Entry<String, Object> entry) {
        Map<String, Object> FishbowlDtoObjectType = (Map<String, Object>) entry.getValue();

        return FishbowlRealTimeDto.builder()
                .warningMinPh(
                        convertToDouble(
                                ((Map<String, Object>) FishbowlDtoObjectType.get("ph")).get("warningMinPh")))
                .warningMaxPh(
                        convertToDouble(
                                ((Map<String, Object>) FishbowlDtoObjectType.get("ph")).get("warningMaxPh")))
                .phState(
                        convertToDouble(
                                ((Map<String, Object>) FishbowlDtoObjectType.get("now")).get("phState")))
                .tempState(
                        convertToDouble(
                                ((Map<String, Object>) FishbowlDtoObjectType.get("now")).get("tempState")))
                .tempStay(
                        convertToDouble(
                                ((Map<String, Object>) FishbowlDtoObjectType.get("temp")).get("tempStay")))
                .co2State(
                        (Boolean) ((Map<String, Object>) FishbowlDtoObjectType.get("now")).get("co2State"))
                .filterState(
                        (Long) ((Map<String, Object>) FishbowlDtoObjectType.get("now")).get("filterState"))
                .lightState(
                        (Boolean) ((Map<String, Object>) FishbowlDtoObjectType.get("now")).get("lightState"))
                .build();
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

    public static String convertFishbowlDTOMapToJson(Map<String, ?> map, String fishbowlId) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Map을 JSON 문자열로 변환
            return objectMapper.writeValueAsString(map.get(fishbowlId));
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
