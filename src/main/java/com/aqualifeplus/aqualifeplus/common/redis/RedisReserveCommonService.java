package com.aqualifeplus.aqualifeplus.common.redis;

import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisReserveCommonService {
    private static final int ADAY = 60 * 60 * 24;

    public String makeKey(Users users, Fishbowl fishbowl, Long reserveId, String reserveType, String onOff) {
        return users.getUserId() + "/" + fishbowl.getFishbowlId() + "/" + reserveType + "/" + reserveId + "/" + onOff;
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
}
