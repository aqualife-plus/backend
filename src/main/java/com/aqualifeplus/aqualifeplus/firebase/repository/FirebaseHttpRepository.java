package com.aqualifeplus.aqualifeplus.firebase.repository;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FirebaseHttpRepository {
    private final FirebaseConfig firebaseConfig;

    public void createFirebaseData(Object value, String url, String accessToken) {
        try {
            firebaseConfig.webClientCreate()
                    .put() // POST로 요청
                    .uri(url + ".json")
                    .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                    .bodyValue(value) // Body에 저장할 데이터 설정
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(), // 상태 코드 체크
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Error Firebase Create Data: {}, Response: {}", clientResponse.statusCode(), body);
                                        return Mono.error(new CustomException(
                                                ErrorCode.ERROR_DO_CREATE_400_OR_500_IN_REALTIME_FIREBASE));
                                    })
                    )
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("예상치 못한 에러가 발생했습니다.", e);
            throw new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_REALTIME_FIREBASE);
        }
    }

    public void updateFirebaseData(Object value, String url, String accessToken) {
        try {
            firebaseConfig.webClientCreate()
                    .patch() // POST로 요청
                    .uri(url + ".json")
                    .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                    .bodyValue(value) // Body에 저장할 데이터 설정
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(), // 상태 코드 체크
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Error Firebase Update Data: {}, Response: {}", clientResponse.statusCode(), body);
                                        return Mono.error(new CustomException(
                                                ErrorCode.ERROR_DO_UPDATE_400_OR_500_IN_REALTIME_FIREBASE));
                                    })
                    )
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("예상치 못한 에러가 발생했습니다.", e);
            throw new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_REALTIME_FIREBASE);
        }
    }

    public void deleteFirebaseData(String url, String accessToken) {
        try {
            firebaseConfig.webClientCreate()
                    .delete() // DELETE로 요청
                    .uri(url + ".json")
                    .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(), // 상태 코드 체크
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Error Firebase Delete Data: {}, Response: {}", clientResponse.statusCode(), body);
                                        return Mono.error(new CustomException(
                                                ErrorCode.ERROR_DO_DELETE_400_OR_500_IN_REALTIME_FIREBASE));
                                    })
                    )
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("예상치 못한 에러가 발생했습니다.", e);
            throw new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_REALTIME_FIREBASE);
        }
    }

    public <T> T getFirebaseData(String url, String accessToken, ParameterizedTypeReference<T> responseType) {
        try {
            return firebaseConfig.webClientCreate()
                    .get()
                    .uri(url + ".json")
                    .headers(headers -> headers.setBearerAuth(accessToken)) // Authorization 헤더 추가
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(), // 상태 코드 체크
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Error Firebase Load Data: {}, Response: {}", clientResponse.statusCode(), body);
                                        return Mono.error(new CustomException(
                                                ErrorCode.ERROR_DO_LOAD_400_OR_500_IN_REALTIME_FIREBASE));
                                    })
                    )
                    .bodyToMono(responseType)
                    .block();
        } catch (Exception e) {
            log.error("예상치 못한 에러가 발생했습니다.", e);
            throw new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_REALTIME_FIREBASE);
        }
    }
}
