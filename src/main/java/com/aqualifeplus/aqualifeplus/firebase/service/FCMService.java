package com.aqualifeplus.aqualifeplus.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FCMService {
    private static final Logger log = LoggerFactory.getLogger(FCMService.class);

    public void sendNotification(String token, String title, String body) {
        try {
            // 메시지 생성
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패: {}, ErrorCode: {}", e.getMessage(), e.getErrorCode(), e);
        } catch (Exception e) {
            log.error("예상치 못한 에러로 전송 실패: {}", e.getMessage(), e);
        }
    }
}
