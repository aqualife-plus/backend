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
        //TODO : error 처리

        firebaseConfig.webClientCreate()
                .put() // POST로 요청
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .bodyValue(value) // Body에 저장할 데이터 설정
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(), // 상태 코드 체크
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Error response: " + body))
                )
                .toBodilessEntity()
                .block();
    }

    public void updateFirebaseData(Object value, String url, String accessToken) {
        //TODO : error 처리

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
        //TODO : error 처리

        firebaseConfig.webClientCreate()
                .delete() // DELETE로 요청
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public <T> T getFirebaseData(String url, String accessToken, ParameterizedTypeReference<T> responseType) {
        //TODO : error 처리

        return firebaseConfig.webClientCreate()
                .get()
                .uri(url + ".json")
                .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
