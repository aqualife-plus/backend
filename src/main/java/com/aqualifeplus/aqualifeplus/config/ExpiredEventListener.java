package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.co2.service.Co2Service;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseRealTimeRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredEventListener implements MessageListener {
    private final int ADAY = 60 * 60 * 24;

    private final FirebaseRealTimeRepository firebaseRealTimeRepository;
    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;
    private final Co2Service co2Service;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        System.out.println("Expired key detected: " + expiredKey);

        // 비즈니스 로직 실행
        handleExpiredKey(expiredKey);
    }

    private void handleExpiredKey(String key) {
        //여기서 key의 값에 따라서 메소드 분리, 만료시간 지정하기
        if (key.contains("filter")) {
            System.out.println("filter입니다.");
        } else if (key.contains("co2")) {
            String[] strArr = key.split("/");
            firebaseRealTimeRepository.updateOnOff(
                    strArr[0], strArr[1], "co2State", strArr[4].equals("on"));
            redisTemplateForFishbowlSettings.opsForValue().set(key, "", ADAY, TimeUnit.SECONDS);
            log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
        } else if (key.contains("light")) {
            String[] strArr = key.split("/");
            firebaseRealTimeRepository.updateOnOff(
                    strArr[0], strArr[1], "lightState", strArr[4].equals("on"));
            redisTemplateForFishbowlSettings.opsForValue().set(key, "", ADAY, TimeUnit.SECONDS);
            log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
        } else {
            throw new RuntimeException("예상치 못한 데이터입니다.");
        }
    }
}
