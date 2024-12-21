package com.aqualifeplus.aqualifeplus.common.redis;

import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_FALSE_IS_EXIST_FALSE;
import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_FALSE_IS_EXIST_TRUE;
import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_TRUE_IS_EXIST_FALSE;
import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_TRUE_IS_EXIST_TRUE;

import com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    public void saveData(
            RedisTemplate<String, String> redisTemplate,
            String key, String value,
            long expirationTime, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expirationTime, timeUnit);
            log.info(String.valueOf(
                    redisTemplate.getExpire(key, TimeUnit.SECONDS)));
        } catch (RedisConnectionFailureException e) {
            throw new CustomException(ErrorCode.DISCONNECTED_REDIS);
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_KEY_IN_REDIS);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DATA_INTEGRITY_VIOLATION_IN_REDIS);
        } catch (DataAccessException e) {
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS);
        }
    }

    public void saveDataNotTTL(
            RedisTemplate<String, String> redisTemplate,
            String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.info(String.valueOf(
                    redisTemplate.getExpire(key, TimeUnit.SECONDS)));
        } catch (RedisConnectionFailureException e) {
            throw new CustomException(ErrorCode.DISCONNECTED_REDIS);
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_KEY_IN_REDIS);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DATA_INTEGRITY_VIOLATION_IN_REDIS);
        } catch (DataAccessException e) {
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS);
        }
    }

    public String getData(RedisTemplate<String, String> redisTemplate,
                          String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                throw new CustomException(ErrorCode.VAlUE_NOT_FOUND_IN_REDIS);
            }
            return value;
        } catch (RedisConnectionFailureException e) {
            throw new CustomException(ErrorCode.DISCONNECTED_REDIS);
        } catch (DataAccessException e) {
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS);
        }
    }

    public void updateDataForReserve(RedisTemplate<String, String> redisTemplate,
                          String key, long expirationTime) {
        try {
            if (isExists(redisTemplate, key)) {
                redisTemplate.expire(key, expirationTime, TimeUnit.SECONDS);
                log.info(String.valueOf(redisTemplate.getExpire(key, TimeUnit.SECONDS)));
            } else {
                throw new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS);
            }
        } catch (RedisConnectionFailureException e) {
            throw new CustomException(ErrorCode.DISCONNECTED_REDIS);
        } catch (DataAccessException e) {
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS);
        }
    }

    public void deleteReserveUsePatternInRedis(RedisTemplate<String, String> redisTemplate, String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("삭제된 키: " + keys);
        } else {
            throw new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS);
        }
    }

    public void deleteData(RedisTemplate<String, String> redisTemplate, String key) {
        if (key != null && !key.isEmpty()) {
            redisTemplate.delete(key);
            log.info("삭제된 키: " + key);
        } else {
            throw new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS);
        }
    }

    private static final int ADAY = 60 * 60 * 24;

    public String makeKey(Users users, Fishbowl fishbowl, Long reserveId, String reserveType, String onOff) {
        return users.getUserId() + "/" + fishbowl.getFishbowlId() + "/" + reserveType + "/" + reserveId + "/" + onOff;
    }

    public boolean isExists(RedisTemplate<String, String> redisTemplate, String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public int getExpirationTime(LocalTime settingTime, LocalTime nowTime) {
        int expirationTime = (((settingTime.getHour() - nowTime.getHour()) * 60)
                + (settingTime.getMinute() - nowTime.getMinute())) * 60
                - nowTime.getSecond();

        if (expirationTime < 0) {
            expirationTime += ADAY;
        }

        return expirationTime;
    }

    public CheckReserveState checkUpdateStateAndIsExistsKeys(boolean state, boolean isExistKeys) {
        if (state && isExistKeys) {
            return UPDATE_STATE_TRUE_IS_EXIST_TRUE;
        } else if (state) {
            return UPDATE_STATE_TRUE_IS_EXIST_FALSE;
        } else if (isExistKeys) {
            return UPDATE_STATE_FALSE_IS_EXIST_TRUE;
        } else {
            return UPDATE_STATE_FALSE_IS_EXIST_FALSE;
        }
    }
}
