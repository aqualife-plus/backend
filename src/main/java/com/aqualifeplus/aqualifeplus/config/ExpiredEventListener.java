package com.aqualifeplus.aqualifeplus.config;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class ExpiredEventListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        System.out.println("Expired key detected: " + expiredKey);

        // 비즈니스 로직 실행
        handleExpiredKey(expiredKey);
    }

    private void handleExpiredKey(String key) {
        // 예: 키에서 사용자 ID 추출 및 관련 작업 수행
        System.out.println("Handling expiration for key: " + key);
    }
}
