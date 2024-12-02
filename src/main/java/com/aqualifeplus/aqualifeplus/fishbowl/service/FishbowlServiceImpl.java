package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.FishbowlsDTO;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.ConnectDto;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import com.aqualifeplus.aqualifeplus.websocket.FirebaseSaveService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FishbowlServiceImpl implements FishbowlService{
    private final JwtService jwtService;
    private final FirebaseSaveService firebaseSaveService;
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public ConnectDto connect() {
        long userId = usersRepository.findByEmail(jwtService.getEmail()).get().getUserId();
        //기존에 비어있는 게 있는 지 확인하기
        //uuid생성
        String fishbowlId = createUUID();
        //firebase에 기본적인 틀 생성
        firebaseSaveService.createFishbowl(
                userId, fishbowlId, FishbowlsDTO.makeFrame(userId));
        //그다음에 return
        return ConnectDto.builder()
                .fishbowlId(fishbowlId)
                .build();
    }

    @Override
    public boolean nameSet(String name) {
        long userId = usersRepository.findByEmail(jwtService.getEmail()).get().getUserId();
        String fishbowlId = redisTemplate.opsForValue().get("fishbowl id : " + userId);
        
        //null 처리 필요
        if (fishbowlId == null) {

        }

        firebaseSaveService.updateName(userId, fishbowlId, name);

        return true;
    }

    private String createUUID() {
        return UUID.randomUUID().toString();
    }
}
