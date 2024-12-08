package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Fishbowl;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.ConnectDto;
import com.aqualifeplus.aqualifeplus.users.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FirebaseRealTimeRepository;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FishbowlServiceImpl implements FishbowlService{
    private static final int ADAY = 60 * 60 * 24;

    private final JwtService jwtService;
    private final FirebaseRealTimeRepository firebaseRealTimeRepository;
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public ConnectDto connect() {
        long userId = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)).getUserId();
        //이름이 비어있는 어항은 삭제
        firebaseRealTimeRepository.deleteFishbowlWithName(userId, "이름을 정해주세요!");
        //uuid생성
        String fishbowlId = createUUID();
        //firebase에 기본적인 틀 생성
        firebaseRealTimeRepository.createFishbowl(
                userId, fishbowlId, Fishbowl.makeFrame());
        //그리고 redis에 저장
        redisTemplate.opsForValue().set(
                "fishbowl id : " + userId,
                fishbowlId,
                ADAY,
                TimeUnit.MILLISECONDS);
        //그다음에 return
        return ConnectDto.builder()
                .success(true)
                .fishbowlId(fishbowlId)
                .build();
    }

    @Override
    public SuccessDto createFishbowlName(String name) {
        long userId = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)).getUserId();
        String fishbowlId = redisTemplate.opsForValue().get("fishbowl id : " + userId);
        
        //null 처리 필요
        if (fishbowlId == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID);
        }

        firebaseRealTimeRepository.updateName(userId, fishbowlId, name);

        return SuccessDto.builder()
                .success(true)
                .build();
    }

    @Override
    public SuccessDto updateFishbowlName(String name) {
        long userId = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)).getUserId();
        String fishbowlToken = jwtService.getFishbowlToken();

        if (fishbowlToken == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID);
        }

        firebaseRealTimeRepository.updateName(userId, fishbowlToken, name);

        return SuccessDto.builder()
                .success(true)
                .build();
    }

    private String createUUID() {
        return UUID.randomUUID().toString();
    }
}
