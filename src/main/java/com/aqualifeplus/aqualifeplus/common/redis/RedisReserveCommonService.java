package com.aqualifeplus.aqualifeplus.common.redis;

import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_FALSE_IS_EXIST_FALSE;
import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_FALSE_IS_EXIST_TRUE;
import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_TRUE_IS_EXIST_FALSE;
import static com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState.UPDATE_STATE_TRUE_IS_EXIST_TRUE;

import com.aqualifeplus.aqualifeplus.common.enum_type.CheckReserveState;
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

    public CheckReserveState checkUpdateStateANDIsExistsKeys(boolean state, boolean isExistKeys) {
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
