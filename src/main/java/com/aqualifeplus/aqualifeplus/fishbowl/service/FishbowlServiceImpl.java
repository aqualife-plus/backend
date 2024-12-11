package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.co2.repository.Co2Repository;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.redis.FishbowlSettingRedis;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.ConnectDto;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.firebase.entity.FishbowlData;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseHttpRepository;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FishbowlRepository;
import com.aqualifeplus.aqualifeplus.light.repository.LightRepository;
import com.aqualifeplus.aqualifeplus.users.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FishbowlServiceImpl implements FishbowlService {
    private static final int ADAY = 60 * 60 * 24;

    private final JwtService jwtService;
    private final FirebaseConfig firebaseConfig;
    private final FishbowlSettingRedis fishbowlSettingRedis;
    private final RedisTemplate<String, String> redisTemplateForTokens;

    private final UsersRepository usersRepository;
    private final FishbowlRepository fishbowlRepository;
    private final Co2Repository co2Repository;
    private final LightRepository lightRepository;
    private final FirebaseHttpRepository firebaseHttpRepository;

    @Override
    @Transactional
    public ConnectDto connect() {
        //변수생성
        String accessToken = firebaseConfig.getAccessToken();
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        String createFishbowlId = createUUID();
        List<String> deleteFishbowlList = new ArrayList<>();

        //firebase에서 userId에 모든 어항 가져오기
        Map<String, Map<String, Object>> firebaseData = firebaseHttpRepository.getFirebaseData(
                String.valueOf(users.getUserId()), accessToken,
                new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {
                });

        if (firebaseData != null) {
            //여기서 이름이 설정안된 친구만 찾고 firebase에서 삭제 + 해당 fishbowlId를 list에 저장
            for (Map.Entry<String, Map<String, Object>> entry : firebaseData.entrySet()) {
                Map<String, Object> fishbowlData = entry.getValue();

                if (fishbowlData.containsKey("name") &&
                        fishbowlData.get("name").equals("이름을 정해주세요!")) {
                    deleteFishbowlList.add(entry.getKey());
                    firebaseHttpRepository.deleteFirebaseData(
                            users.getUserId() + "/" + entry.getKey(), accessToken);
                }
            }
        }

        //저장된 list값으로 삭제
        fishbowlRepository.deleteByFishbowlIdIn(deleteFishbowlList);

        Fishbowl fishbowl = Fishbowl.builder()
                .fishbowlId(createFishbowlId)
                .users(users)
                .build();

        //firebase에 기본적인 틀 생성
        firebaseHttpRepository.createFirebaseData(
                FishbowlData.makeFrame(), users.getUserId() + "/" + createFishbowlId, accessToken);
        fishbowlRepository.save(fishbowl);

        //그리고 이름 생성을 위한 fishbowlid를 redis에 저장
        redisTemplateForTokens.opsForValue().set(
                "fishbowl : fishbowl id : " + users.getUserId(),
                createFishbowlId,
                ADAY,
                TimeUnit.MILLISECONDS);
        //그다음에 return
        return ConnectDto.builder()
                .success(true)
                .fishbowlId(createFishbowlId)
                .build();
    }

    @Override
    public SuccessDto createFishbowlName(String name) {
        String accessToken = firebaseConfig.getAccessToken();
        long userId = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)).getUserId();
        String fishbowlId = redisTemplateForTokens.opsForValue().get("fishbowl : fishbowl id : " + userId);

        //null 처리 필요
        if (fishbowlId == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID);
        }

        Map<String, String> maps = new HashMap<>();
        maps.put("name", name);
        String url = userId + "/" + fishbowlId;
        firebaseHttpRepository.updateFirebaseData(maps, url, accessToken);

        return SuccessDto.builder()
                .success(true)
                .build();
    }

    @Override
    public SuccessDto updateFishbowlName(String name) {
        String accessToken = firebaseConfig.getAccessToken();
        long userId = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)).getUserId();
        String fishbowlToken = jwtService.getFishbowlToken();

        if (fishbowlToken == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID);
        }

        Map<String, String> maps = new HashMap<>();
        maps.put("name", name);
        String url = userId + "/" + fishbowlToken;
        firebaseHttpRepository.updateFirebaseData(maps, url, accessToken);

        return SuccessDto.builder()
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public SuccessDto deleteFishbowl() {
        String accessToken = firebaseConfig.getAccessToken();
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID));

        //firebase에서 어항삭제
        firebaseHttpRepository.deleteFirebaseData(
                users.getUserId() + "/" + fishbowl.getFishbowlId(), accessToken);
        //rdbms에서 관련 값 다 삭제
        co2Repository.deleteAllByFishbowl(fishbowl);
        lightRepository.deleteAllByFishbowl(fishbowl);
        //filter repo 삭제
        fishbowlRepository.deleteByFishbowlId(fishbowl.getFishbowlId());

        //redis에서 관련 값 다 삭제
        fishbowlSettingRedis.deleteCo2LightReserveInRedis(
                users.getUserId() + "/"+ fishbowl.getFishbowlId() +"/*/*/*");

        return SuccessDto.builder()
                .success(true)
                .build();
    }

    private String createUUID() {
        return UUID.randomUUID().toString();
    }
}
