package com.aqualifeplus.aqualifeplus.firebase.service;

import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.NOT_MATCH_NUMBER_FORMAT;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.firebase.dto.FishbowlRealTimeDto;
import com.aqualifeplus.aqualifeplus.firebase.dto.WarningDto;
import com.aqualifeplus.aqualifeplus.websocket.ChatHandler;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseRealTimeService {
    private final ChatHandler webSocketHandler;
    private final FCMService fcmService;

    @Autowired
    public FirebaseRealTimeService(@Lazy ChatHandler webSocketHandler, FCMService fcmService) {
        this.webSocketHandler = webSocketHandler;
        this.fcmService = fcmService;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Firebase에 데이터 변경 시 리스너 추가
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                log.info("add");
                traverseData(snapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                log.info("change");
                traverseData(snapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                log.info("remove");
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                // 필요한 경우 이동 처리 로직 추가
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 오류 처리 로직 추가
            }
        });
    }

    private void traverseData(DataSnapshot snapshot) {
        Map<String, Object> allData = traverseDataSnapshot(snapshot); // 모든 데이터를 수집
        log.info("Collected data: {}", allData);

        valueFormattingUseFishbowlList(allData);

        try {
            webSocketHandler.messageToClients(allData);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> traverseDataSnapshot(DataSnapshot snapshot) {
        Map<String, Object> allData = new HashMap<>();

        allData.put("userId", snapshot.getKey().trim());
        collectAllData(snapshot, "", allData); // 루트 노드부터 탐색 시작

        return allData;
    }

    private void collectAllData(DataSnapshot snapshot, String currentPath, Map<String, Object> allData) {
        String path = currentPath.isEmpty() ? snapshot.getKey() : currentPath + "/" + snapshot.getKey();

        if (snapshot.hasChildren()) {
            for (DataSnapshot child : snapshot.getChildren()) {
                collectAllData(child, path, allData); // 자식 노드 재귀 탐색
            }
        } else {
            allData.put(currentPath + "/" + snapshot.getKey(), snapshot.getValue()); // 최종 데이터를 경로와 함께 저장
        }
    }

    private void valueFormattingUseFishbowlList(Map<String, Object> allData) {
        String regex3 = "^([^/]+)/([^/]+)/([^/]+)/([^/]+)$"; // 3개 그룹
        Pattern pattern3 = Pattern.compile(regex3);
        Map<String, WarningDto> warningDtoMap = new HashMap<>();

        for (String key : allData.keySet()) {
            Matcher matcher3 = pattern3.matcher(key);

            if (matcher3.matches()) {
                String fishbowlId = matcher3.group(2);
                String fishbowlDept = matcher3.group(4);

                if (!warningDtoMap.containsKey(fishbowlId)) {
                    warningDtoMap.put(fishbowlId, new WarningDto());
                }

                switch (fishbowlDept) {
                    case "deviceToken" -> warningDtoMap.get(fishbowlId)
                            .setDeviceToken((String) allData.get(key));
                    case "tempState" -> {
                       warningDtoMap.get(fishbowlId)
                                .setTempState(convertToDouble(allData.get(key)));
                    }
                    case "phState" -> {
                        warningDtoMap.get(fishbowlId)
                                .setPhState(convertToDouble(allData.get(key)));
                    }
                    case "tempStay" -> {
                       warningDtoMap.get(fishbowlId)
                                .setTempStay(convertToDouble(allData.get(key)));
                    }
                    case "warningMaxPh" -> {
                        warningDtoMap.get(fishbowlId)
                                .setWarningMaxPh(convertToDouble(allData.get(key)));
                    }
                    case "warningMinPh" -> {
                       warningDtoMap.get(fishbowlId)
                                .setWarningMinPh(convertToDouble(allData.get(key)));
                    }
                }
            }
        }

        for (String key : warningDtoMap.keySet()) {
            String message = "";
            if (warningDtoMap.get(key).getTempState() != warningDtoMap.get(key).getTempStay()) {
                message += "온도가 한계를 넘었습니다\n";
            }
            if (warningDtoMap.get(key).getWarningMinPh() > warningDtoMap.get(key).getPhState() ||
                    warningDtoMap.get(key).getPhState() > warningDtoMap.get(key).getWarningMaxPh()) {
                message += "ph가 한계를 넘었습니다";
            }

            if (!message.isEmpty()) {
                fcmService.sendNotification(warningDtoMap.get(key).getDeviceToken(), "warning", message);
            }
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

