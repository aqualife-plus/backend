package com.aqualifeplus.aqualifeplus.firebase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimpleServiceImpl{
//    private final FirebaseConfig firebaseConfig;
//    private final JwtService jwtService;
//    private final UsersService usersService;
//    private final FirebaseHttpRepository firebaseHttpRepository;
//
//    @Override
//    public Object lightReserveList() {
//        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
//        Long userId = usersService.getId(jwtService.getEmail());
//        String fishbowlId = jwtService.getFishbowlToken();
//
//        String url = userId + "/" + fishbowlId + "/" + "light";
//
//        Map<String, Light> lightMapData = firebaseHttpRepository
//                .getFirebaseData(url, accessToken, new ParameterizedTypeReference<Map<String, Light>>() {
//                });
//
//        return lightMapData.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.comparing(Light::getCreateDate)))
//                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Light lightReserve(String idx) {
//        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
//        Long userId = usersService.getId(jwtService.getEmail());
//        String fishbowlId = jwtService.getFishbowlToken();
//
//        String url = userId + "/" + fishbowlId + "/" + "light" + "/" + idx;
//
//        return firebaseHttpRepository.getFirebaseData(url, accessToken, new ParameterizedTypeReference<Light>() {
//        });
//    }
//
//    @Override
//    public CreateSuccessDto lightCreateReserve(Light light) {
//        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
//        Long userId = usersService.getId(jwtService.getEmail());
//        String fishbowlId = jwtService.getFishbowlToken();
//        String reserveId = UUID.randomUUID().toString();
//
//        String url = userId + "/" + fishbowlId + "/" + "light" + "/" + reserveId;
//        light.setCreateDate(LocalDateTime.now());
//
//        firebaseHttpRepository.createFirebaseData(light, url, accessToken);
//
//        return CreateSuccessDto.builder()
//                .success(true)
//                .reserveId(reserveId)
//                .build();
//    }
//
//    @Override
//    public UpdateSuccessDto lightUpdateReserve(String idx, Light light) {
//        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
//        Long userId = usersService.getId(jwtService.getEmail());
//        String fishbowlId = jwtService.getFishbowlToken();
//
//        String url = userId + "/" + fishbowlId + "/" + "light" + "/" + idx;
//        light.setCreateDate(lightReserve(idx).getCreateDate());
//
//        firebaseHttpRepository.updateFirebaseData(light, url, accessToken);
//
//        return UpdateSuccessDto.builder()
//                .success(true)
//                .reserveId(idx)
//                .build();
//    }
//
//    @Override
//    public DeleteSuccessDto lightDeleteReserve(String idx) {
//        String accessToken = firebaseConfig.getAccessToken(); // Access Token 가져오기
//        Long userId = usersService.getId(jwtService.getEmail());
//        String fishbowlId = jwtService.getFishbowlToken();
//
//        String url = userId + "/" + fishbowlId + "/" + "light" + "/" + idx;
//
//        firebaseHttpRepository.deleteFirebaseData(url, accessToken);
//
//        return DeleteSuccessDto.builder()
//                .success(true)
//                .reserveId(idx)
//                .build();
//    }
}
