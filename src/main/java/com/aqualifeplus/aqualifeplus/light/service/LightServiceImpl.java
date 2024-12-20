package com.aqualifeplus.aqualifeplus.light.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.redis.RedisService;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FishbowlRepository;
import com.aqualifeplus.aqualifeplus.light.dto.DeleteLightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightRequestDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightResponseDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.entity.Light;
import com.aqualifeplus.aqualifeplus.light.repository.LightRepository;
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
public class LightServiceImpl implements LightService {
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final LightRepository lightRepository;
    private final FishbowlRepository fishbowlRepository;

    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;

    @Override
    public List<LightResponseDto> lightReserveList() {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        List<Light> lightList = lightRepository
                .findAllByFishbowl(fishbowl);

        return lightList.stream()
                .map(LightResponseDto::toResponseDto)
                .toList();
    }

    @Override
    public LightResponseDto lightReserve(Long idx) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Light light = lightRepository.findByIdAndFishbowl(idx, fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIGHT_RESERVE));

        return LightResponseDto.toResponseDto(light);
    }

    @Override
    @Transactional
    public LightSuccessDto lightCreateReserve(LightRequestDto lightRequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Light saveLight = lightRepository.save(
                Light.builder()
                        .lightReserveState(lightRequestDto.isLightReserveState())
                        .lightStartTime(lightRequestDto.getLightStartTime())
                        .lightEndTime(lightRequestDto.getLightEndTime())
                        .fishbowl(fishbowl)
                        .build()
        );

        if (saveLight.isLightReserveState()) {
            redisService.saveData(redisTemplateForFishbowlSettings,
                    redisService.makeKey(users, fishbowl, saveLight.getId(), "light", "on"),
                    "",
                    redisService.getExpirationTime(saveLight.getLightStartTime(), LocalTime.now()),
                    TimeUnit.SECONDS);
            redisService.saveData(redisTemplateForFishbowlSettings,
                    redisService.makeKey(users, fishbowl, saveLight.getId(), "light", "off"),
                    "",
                    redisService.getExpirationTime(saveLight.getLightEndTime(), LocalTime.now()),
                    TimeUnit.SECONDS);
        }

        return LightSuccessDto.builder()
                .success(true)
                .lightResponseDto(
                        LightResponseDto.toResponseDto(saveLight))
                .build();
    }

    @Override
    @Transactional
    public LightSuccessDto lightUpdateReserve(Long idx, LightRequestDto lightRequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Light targetLight = lightRepository.findByIdAndFishbowl(idx, fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIGHT_RESERVE));

        targetLight.setLightReserveState(lightRequestDto.isLightReserveState());
        targetLight.setLightStartTime(lightRequestDto.getLightStartTime());
        targetLight.setLightEndTime(lightRequestDto.getLightEndTime());

        String onKey =
                redisService.makeKey(
                        users,
                        targetLight.getFishbowl(),
                        targetLight.getId(), "light", "on");
        String offKey =
                redisService.makeKey(
                        users,
                        targetLight.getFishbowl(),
                        targetLight.getId(), "light", "off");
        int getStartExpirationTime =
                redisService.getExpirationTime(targetLight.getLightStartTime(), LocalTime.now());
        int getEndExpirationTime =
                redisService.getExpirationTime(targetLight.getLightEndTime(), LocalTime.now());
        String pattern = users.getUserId() + "/*/" + "light" + "/" + idx + "/*";

        switch (redisService.checkUpdateStateAndIsExistsKeys(
                targetLight.isLightReserveState(),
                redisService.isExists(redisTemplateForFishbowlSettings, onKey)
                        && redisService.isExists(redisTemplateForFishbowlSettings, offKey))
        ) {
            case UPDATE_STATE_TRUE_IS_EXIST_TRUE -> {
                redisService.updateDataForReserve(redisTemplateForFishbowlSettings,
                        onKey, getStartExpirationTime);
                redisService.updateDataForReserve(redisTemplateForFishbowlSettings,
                        offKey, getEndExpirationTime);
            }
            case UPDATE_STATE_TRUE_IS_EXIST_FALSE -> {
                redisService.saveData(redisTemplateForFishbowlSettings,
                        onKey, "",
                        getStartExpirationTime, TimeUnit.SECONDS);
                redisService.saveData(redisTemplateForFishbowlSettings,
                        offKey, "",
                        getEndExpirationTime, TimeUnit.SECONDS);
            }
            case UPDATE_STATE_FALSE_IS_EXIST_TRUE ->
                    redisService.deleteReserveUsePatternInRedis(redisTemplateForFishbowlSettings, pattern);
            default -> log.info("변경된 값과 현재 값 모두 설정이 false입니다.");
        }

        return LightSuccessDto.builder()
                .success(true)
                .lightResponseDto(
                        LightResponseDto.toResponseDto(targetLight))
                .build();
    }

    @Override
    @Transactional
    public DeleteLightSuccessDto lightDeleteReserve(Long idx) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        lightRepository.deleteByIdAndFishbowl(idx, fishbowl);

        String pattern = users.getUserId() + "/*/" + "light" + "/" + idx + "/*";

        redisService.deleteReserveUsePatternInRedis(
                redisTemplateForFishbowlSettings, pattern);

        return DeleteLightSuccessDto.builder()
                .success(true)
                .build();
    }
}
