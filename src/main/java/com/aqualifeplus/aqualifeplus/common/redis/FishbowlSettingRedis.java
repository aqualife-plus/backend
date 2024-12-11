package com.aqualifeplus.aqualifeplus.common.redis;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FishbowlSettingRedis {
    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;


    public void createCo2LightReserveInRedis(String key, int expirationTime) {
        redisTemplateForFishbowlSettings
                .opsForValue()
                .set(key, "", expirationTime, TimeUnit.SECONDS);
        log.info(String.valueOf(
                redisTemplateForFishbowlSettings.getExpire(
                        key, TimeUnit.SECONDS)));
    }

    public void updateCo2LightReserveInRedis(String key, int expirationTime) {
        if (isExists(key)) {
            redisTemplateForFishbowlSettings.expire(key, expirationTime, TimeUnit.SECONDS);
            log.info(String.valueOf(redisTemplateForFishbowlSettings.getExpire(key, TimeUnit.SECONDS)));
        } else {
            //수정해야 함
            throw new RuntimeException("해당 예약이 없습니다.");
        }
    }


    public void deleteCo2LightReserveInRedis(String pattern) {
        Set<String> keys = redisTemplateForFishbowlSettings.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplateForFishbowlSettings.delete(keys);
            log.info("삭제된 키: " + keys);
        } else {
            log.info("삭제할 키가 없습니다.");
        }
    }

    public boolean isExists(String key) {
        return Boolean.TRUE.equals(redisTemplateForFishbowlSettings.hasKey(key));
    }
}
