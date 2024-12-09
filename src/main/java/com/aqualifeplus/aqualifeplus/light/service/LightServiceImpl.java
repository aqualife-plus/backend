package com.aqualifeplus.aqualifeplus.light.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.light.dto.LightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.dto.DeleteLightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightRequestDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightResponseDto;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.light.entity.Light;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FishbowlRepository;
import com.aqualifeplus.aqualifeplus.light.repository.LightRepository;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LightServiceImpl implements LightService {
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final LightRepository lightRepository;
    private final FishbowlRepository fishbowlRepository;

    @Override
    public List<LightResponseDto> lightReserveList() {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID));

        List<Light> lightList = lightRepository
                .findAllByFishbowlAndUsers(fishbowl, users);

        return lightList.stream()
                .map(LightResponseDto::toResponseDto)
                .toList();
    }

    @Override
    public LightResponseDto lightReserve(Long idx) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        Light light = lightRepository.findByIdAndUsers(idx, users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIGHT_RESERVE));

        return LightResponseDto.toResponseDto(light);
    }

    @Override
    @Transactional
    public LightSuccessDto lightCreateReserve(LightRequestDto lightRequestDto) {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID));

        System.out.println(fishbowl);

        Light saveLight = lightRepository.save(Light.builder()
                .lightReserveState(lightRequestDto.isLightReserveState())
                .lightStartTime(lightRequestDto.getLightStartTime())
                .lightEndTime(lightRequestDto.getLightEndTime())
                .users(users)
                .fishbowl(fishbowl)
                .build());

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

        Light targetLight = lightRepository.findByIdAndUsers(idx, users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIGHT_RESERVE));

        targetLight.setLightReserveState(lightRequestDto.isLightReserveState());
        targetLight.setLightStartTime(lightRequestDto.getLightStartTime());
        targetLight.setLightEndTime(lightRequestDto.getLightEndTime());

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

        lightRepository.deleteByIdAndUsers(idx, users);

        return DeleteLightSuccessDto.builder()
                .success(true)
                .build();
    }
}
