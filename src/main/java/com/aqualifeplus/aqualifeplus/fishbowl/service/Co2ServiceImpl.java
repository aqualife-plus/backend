package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Co2;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.CreateSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.DeleteSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.UpdateSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FirebaseHttpRepository;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Co2ServiceImpl implements Co2Service {
    private final FirebaseConfig firebaseConfig;
    private final JwtService jwtService;
    private final UsersService usersService;
    private final FirebaseHttpRepository firebaseHttpRepository;

    @Override
    public List<Map<String, Co2>> co2ReserveList() {
        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
        Long userId = usersService.getId(jwtService.getEmail());
        String fishbowlId = jwtService.getFishbowlToken();

        String url = userId + "/" + fishbowlId + "/" + "co2";

        Map<String, Co2> co2MapData = firebaseHttpRepository
                .getFirebaseData(url, accessToken, new ParameterizedTypeReference<Map<String, Co2>>() {
                });

        return co2MapData.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(Co2::getCreateDate)))
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Co2 co2Reserve(String idx) {
        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
        Long userId = usersService.getId(jwtService.getEmail());
        String fishbowlId = jwtService.getFishbowlToken();

        String url = userId + "/" + fishbowlId + "/" + "co2" + "/" + idx;

        return firebaseHttpRepository.getFirebaseData(url, accessToken, new ParameterizedTypeReference<Co2>() {
        });
    }

    @Override
    public CreateSuccessDto co2CreateReserve(Co2 co2) {
        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
        Long userId = usersService.getId(jwtService.getEmail());
        String fishbowlId = jwtService.getFishbowlToken();
        String reserveId = UUID.randomUUID().toString();

        String url = userId + "/" + fishbowlId + "/" + "co2" + "/" + reserveId;
        co2.setCreateDate(LocalDateTime.now());

        firebaseHttpRepository.createFirebaseData(co2, url, accessToken);

        return CreateSuccessDto.builder()
                .success(true)
                .reserveId(reserveId)
                .build();
    }

    @Override
    public UpdateSuccessDto co2UpdateReserve(String idx, Co2 co2) {
        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
        Long userId = usersService.getId(jwtService.getEmail());
        String fishbowlId = jwtService.getFishbowlToken();

        String url = userId + "/" + fishbowlId + "/" + "co2" + "/" + idx;
        co2.setCreateDate(co2Reserve(idx).getCreateDate());

        firebaseHttpRepository.updateFirebaseData(co2, url, accessToken);

        return UpdateSuccessDto.builder()
                .success(true)
                .reserveId(idx)
                .build();
    }

    @Override
    public DeleteSuccessDto co2DeleteReserve(String idx) {
        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
        Long userId = usersService.getId(jwtService.getEmail());
        String fishbowlId = jwtService.getFishbowlToken();

        String url = userId + "/" + fishbowlId + "/" + "co2" + "/" + idx;

        firebaseHttpRepository.deleteFirebaseData(url, accessToken);

        return DeleteSuccessDto.builder()
                .success(true)
                .reserveId(idx)
                .build();
    }
}
