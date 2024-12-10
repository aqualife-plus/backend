package com.aqualifeplus.aqualifeplus.co2.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2RequestDto;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2ResponseDto;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2SuccessDto;
import com.aqualifeplus.aqualifeplus.co2.dto.DeleteCo2SuccessDto;
import com.aqualifeplus.aqualifeplus.co2.entity.Co2;
import com.aqualifeplus.aqualifeplus.co2.repository.Co2Repository;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FishbowlRepository;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Co2ServiceImpl implements Co2Service {
    private static final int ADAY = 60 * 60 * 24;

    private final JwtService jwtService;
    private final Co2Repository co2Repository;
    private final UsersRepository usersRepository;
    private final FishbowlRepository fishbowlRepository;

    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;

    @Override
    public List<Co2ResponseDto> co2ReserveList() {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID));
        List<Co2> Co2List = co2Repository
                .findAllByFishbowlAndUsers(fishbowl, users);

        return Co2List.stream()
                .map(Co2ResponseDto::toResponseDto)
                .toList();
    }

    @Override
    public Co2ResponseDto co2Reserve(Long idx) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        Co2 co2 = co2Repository.findByIdAndUsers(idx, users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CO2_RESERVE));

        return Co2ResponseDto.toResponseDto(co2);
    }

    @Override
    @Transactional
    public Co2SuccessDto co2CreateReserve(Co2RequestDto co2RequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID));

        Co2 saveCo2 = co2Repository.save(Co2.builder()
                .co2ReserveState(co2RequestDto.isCo2ReserveState())
                .co2StartTime(co2RequestDto.getCo2StartTime())
                .co2EndTime(co2RequestDto.getCo2EndTime())
                .users(users)
                .fishbowl(fishbowl)
                .build());

        createCo2ReserveInRedis(
                users.getUserId(), fishbowl.getFishbowlId(),
                "co2", "on", saveCo2.getId(), saveCo2.getCo2StartTime());
        createCo2ReserveInRedis(
                users.getUserId(), fishbowl.getFishbowlId(),
                "co2", "off", saveCo2.getId(), saveCo2.getCo2EndTime());

        return Co2SuccessDto.builder()
                .success(true)
                .co2ResponseDto(Co2ResponseDto.toResponseDto(saveCo2))
                .build();
    }

    private void createCo2ReserveInRedis(Long userId, String fishbowlId,
                                         String reserveType, String onOff,
                                         Long reserveId, LocalTime settingTime) {
        int expirationTime = getExpirationTime(settingTime, LocalTime.now());
        if (expirationTime < 0) {
            expirationTime += ADAY;
        }

        redisTemplateForFishbowlSettings
                .opsForValue()
                .set(userId + "/" + fishbowlId + "/" + reserveType + "/" + reserveId + "/" + onOff,
                        settingTime + " " + expirationTime,
                        expirationTime, TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public Co2SuccessDto co2UpdateReserve(Long idx, Co2RequestDto co2RequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        Co2 targetCo2 = co2Repository.findByIdAndUsers(idx, users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CO2_RESERVE));

        targetCo2.setCo2ReserveState(co2RequestDto.isCo2ReserveState());
        targetCo2.setCo2StartTime(co2RequestDto.getCo2StartTime());
        targetCo2.setCo2EndTime(co2RequestDto.getCo2EndTime());

        return Co2SuccessDto.builder()
                .success(true)
                .co2ResponseDto(
                        Co2ResponseDto.toResponseDto(targetCo2))
                .build();
    }

    @Override
    @Transactional
    public DeleteCo2SuccessDto co2DeleteReserve(Long idx) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        co2Repository.deleteByIdAndUsers(idx, users);

        return DeleteCo2SuccessDto.builder()
                .success(true)
                .build();
    }

    private static int getExpirationTime(LocalTime settingTime, LocalTime nowTime) {
        return (((settingTime.getHour() - nowTime.getHour()) * 60)
                + (settingTime.getMinute() - nowTime.getMinute())) * 60
                - nowTime.getSecond();
    }
}
