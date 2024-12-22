package com.aqualifeplus.aqualifeplus.common.redis;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.firebase.service.FCMService;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import com.aqualifeplus.aqualifeplus.websocket.MessageQueueService;
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

    private final FCMService fcmService;
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;
    private final RedisTemplate<String, String> redisTemplateForTokens;
    private final MessageQueueService messageQueueService;

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
            throw new CustomException(ErrorCode.NOT_CORRECT_EXPIRED_KEY_IN_REDIS);
        }
    }

    private void updateOnOffUseReserve(String key, String type) {
        String[] strArr = key.split("/");
        String path = strArr[0] + "/" + strArr[1];
        String formattedMessage = "Type: " + type + ", Message: " + strArr[4].equals("on");

        messageQueueService.sendMessageToQueue("" + "<>" + path + "<>" + formattedMessage);
        redisTemplateForFishbowlSettings.opsForValue().set(key, "", ADAY, TimeUnit.SECONDS);

        Users users = usersRepository.findById(Long.parseLong(strArr[0]))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        fcmService.sendNotification(
                redisTemplateForTokens.opsForValue().get("users : androidToken : " + users.getEmail()),
                type,
                type + "이 " + strArr[4] + "되었습니다.");
        log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
    }

    private void updateFilterUseReserve(String key) {
        String[] strArr = key.split("/");
        String path = strArr[0] + "/" + strArr[1];
        String formattedMessage = "Type: " + "filter" + ", Message: " + strArr[4].equals("on");

        messageQueueService.sendMessageToQueue("" + "<>" + path + "<>" + formattedMessage);

        redisTemplateForFishbowlSettings.opsForValue().set(key, "", ADAY * 7, TimeUnit.SECONDS);

        Users users = usersRepository.findById(Long.parseLong(strArr[0]))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        fcmService.sendNotification(
                redisTemplateForTokens.opsForValue().get("users : androidToken : " + users.getEmail()),
                "환수",
                "환수가 시작되었습니다."
        );
        log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
    }
}
