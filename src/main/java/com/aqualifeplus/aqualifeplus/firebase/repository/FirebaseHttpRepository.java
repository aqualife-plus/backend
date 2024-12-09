package com.aqualifeplus.aqualifeplus.firebase.repository;

import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FirebaseHttpRepository {
    private final FirebaseConfig firebaseConfig;

    public void createFirebaseData(Object value, String url, String accessToken) {
        firebaseConfig.webClientCreate()
                .put() // POST로 요청
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .bodyValue(value) // Body에 저장할 데이터 설정
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void updateFirebaseData(Object value, String url, String accessToken) {
        firebaseConfig.webClientCreate()
                .patch() // POST로 요청
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .bodyValue(value) // Body에 저장할 데이터 설정
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void deleteFirebaseData(String url, String accessToken) {
        firebaseConfig.webClientCreate()
                .delete() // DELETE로 요청
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public <T> T getFirebaseData(String url, String accessToken, ParameterizedTypeReference<T> responseType) {
        return firebaseConfig.webClientCreate()
                .get()
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
