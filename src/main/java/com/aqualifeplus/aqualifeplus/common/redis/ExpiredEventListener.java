package com.aqualifeplus.aqualifeplus.common.redis;

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

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Expired key detected: " + expiredKey);

        // 비즈니스 로직 실행
        handleExpiredKey(expiredKey);
    }

    private void handleExpiredKey(String key) {
        //여기서 key의 값에 따라서 메소드 분리, 만료시간 지정하기
        if (key.contains("filter")) {
            updateFilterUseReserve(key);
        } else if (key.contains("co2")) {
            updateOnOffUseReserve(key, "co2State");
        } else if (key.contains("light")) {
            updateOnOffUseReserve(key, "lightState");
        } else {
            throw new RuntimeException("예상치 못한 데이터입니다.");
        }
    }

    private void updateOnOffUseReserve(String key, String type) {
        String[] strArr = key.split("/");
        firebaseRealTimeRepository.updateOnOff(
                strArr[0], strArr[1], type, strArr[4].equals("on"));
        redisTemplateForFishbowlSettings.opsForValue().set(key, "", ADAY, TimeUnit.SECONDS);
        log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
    }

    private void updateFilterUseReserve(String key) {
        String[] strArr = key.split("/");
        firebaseRealTimeRepository.updateFilter(strArr[0], strArr[1]);
        redisTemplateForFishbowlSettings.opsForValue().set(key, "", ADAY * 7, TimeUnit.SECONDS);
        log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
    }
}
