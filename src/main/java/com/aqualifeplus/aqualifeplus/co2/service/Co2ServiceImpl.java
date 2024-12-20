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
import com.aqualifeplus.aqualifeplus.common.redis.RedisService;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FishbowlRepository;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Co2ServiceImpl implements Co2Service {
    private final JwtService jwtService;
    private final Co2Repository co2Repository;
    private final UsersRepository usersRepository;
    private final FishbowlRepository fishbowlRepository;

    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;

    @Override
    public List<Co2ResponseDto> co2ReserveList() {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        List<Co2> Co2List = co2Repository.findAllByFishbowl(fishbowl);

        return Co2List.stream()
                .map(Co2ResponseDto::toResponseDto)
                .toList();
    }

    @Override
    public Co2ResponseDto co2Reserve(Long idx) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Co2 co2 = co2Repository.findByIdAndFishbowl(idx, fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CO2_RESERVE));

        return Co2ResponseDto.toResponseDto(co2);
    }

    @Override
    @Transactional
    public Co2SuccessDto co2CreateReserve(Co2RequestDto co2RequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Co2 saveCo2 = co2Repository.save(
                Co2.builder()
                        .co2ReserveState(co2RequestDto.getCo2ReserveState())
                        .co2StartTime(co2RequestDto.getCo2StartTime())
                        .co2EndTime(co2RequestDto.getCo2EndTime())
                        .fishbowl(fishbowl)
                        .build()
        );

        if (saveCo2.isCo2ReserveState()) {
            redisService.saveData(redisTemplateForFishbowlSettings,
                    redisService.makeKey(users, fishbowl, saveCo2.getId(), "co2", "on"),
                    "",
                    redisService.getExpirationTime(saveCo2.getCo2StartTime(), LocalTime.now()),
                    TimeUnit.SECONDS);
            redisService.saveData(redisTemplateForFishbowlSettings,
                    redisService.makeKey(users, fishbowl, saveCo2.getId(), "co2", "off"),
                    "",
                    redisService.getExpirationTime(saveCo2.getCo2EndTime(), LocalTime.now()),
                    TimeUnit.SECONDS);
        }

        return Co2SuccessDto.builder()
                .success(true)
                .co2ResponseDto(
                        Co2ResponseDto.toResponseDto(saveCo2))
                .build();
    }

    @Override
    @Transactional
    public Co2SuccessDto co2UpdateReserve(Long idx, Co2RequestDto co2RequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Co2 targetCo2 = co2Repository.findByIdAndFishbowl(idx, fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CO2_RESERVE));

        targetCo2.setCo2ReserveState(co2RequestDto.getCo2ReserveState());
        targetCo2.setCo2StartTime(co2RequestDto.getCo2StartTime());
        targetCo2.setCo2EndTime(co2RequestDto.getCo2EndTime());

        String onKey =
                redisService.makeKey(
                        users,
                        targetCo2.getFishbowl(),
                        targetCo2.getId(), "co2", "on");
        String offKey =
                redisService.makeKey(
                        users,
                        targetCo2.getFishbowl(),
                        targetCo2.getId(), "co2", "off");
        int getStartExpirationTime =
                redisService.getExpirationTime(targetCo2.getCo2StartTime(), LocalTime.now());
        int getEndExpirationTime =
                redisService.getExpirationTime(targetCo2.getCo2EndTime(), LocalTime.now());
        String pattern = users.getUserId() + "/*/" + "co2" + "/" + idx + "/*";

        switch (redisService.checkUpdateStateAndIsExistsKeys(
                targetCo2.isCo2ReserveState(),
                redisService.isExists(redisTemplateForFishbowlSettings, onKey)
                        && redisService.isExists(redisTemplateForFishbowlSettings, offKey))) {
            case UPDATE_STATE_TRUE_IS_EXIST_TRUE -> {
                redisService.updateDataForReserve(redisTemplateForFishbowlSettings, onKey, getStartExpirationTime);
                redisService.updateDataForReserve(redisTemplateForFishbowlSettings, offKey, getEndExpirationTime);
            }
            case UPDATE_STATE_TRUE_IS_EXIST_FALSE -> {
                redisService.saveData(redisTemplateForFishbowlSettings,
                        onKey, null,
                        getStartExpirationTime, TimeUnit.SECONDS);
                redisService.saveData(redisTemplateForFishbowlSettings,
                        offKey, null,
                        getStartExpirationTime, TimeUnit.SECONDS);
            }
            case UPDATE_STATE_FALSE_IS_EXIST_TRUE -> redisService.deleteReserveUsePatternInRedis(redisTemplateForFishbowlSettings, pattern);
            default -> log.info("변경된 값과 현재 값 모두 설정이 false입니다.");
        }

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
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        co2Repository.deleteByIdAndFishbowl(idx, fishbowl);

        String pattern = users.getUserId() + "/*/" + "co2" + "/" + idx + "/*";

        redisService.deleteReserveUsePatternInRedis(redisTemplateForFishbowlSettings, pattern);

        return DeleteCo2SuccessDto.builder()
                .success(true)
                .build();
    }
}
