package com.aqualifeplus.aqualifeplus.auth.service;

import com.aqualifeplus.aqualifeplus.auth.dto.AndroidRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.redis.RedisService;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseHttpRepository;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplateForTokens;
    private final FirebaseHttpRepository firebaseHttpRepository;
    private final FirebaseConfig firebaseConfig;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public TokenResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (passwordEncoder.matches(loginRequestDto.getPassword(), users.getPassword())) {
            TokenResponseDto rt = new TokenResponseDto(
                    jwtService.makeAccessToken(email),
                    jwtService.makeRefreshToken(email));

            redisService.saveData(redisTemplateForTokens,
                    "users : refreshToken : " + email,
                    rt.getRefreshToken().substring(7),
                    jwtService.getRefreshTokenExpirationMs(),
                    TimeUnit.MILLISECONDS);
            return rt;
        }

        throw new CustomException(ErrorCode.NOT_MATCH_PASSWORD_OR_EMAIL);
    }

    @Override
    public String refreshAccessToken() {
        String authData = jwtService.getAuthorization();
        String email = jwtService.extractEmail(authData);
        String storedRefreshToken =
                redisService.getData(redisTemplateForTokens, "users : refreshToken : " + email);
        if (storedRefreshToken.equals(authData)) {
            return "Bearer " + jwtService.makeAccessToken(email);
        } else {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    @Override
    public SuccessDto setAndroidToken(AndroidRequestDto androidRequestDto) {
        String accessToken = firebaseConfig.getAccessToken();
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        redisService.saveDataNotTTL(redisTemplateForTokens,
                "users : androidToken : " + users.getEmail(),
                androidRequestDto.getToken());
        Map<String, Map<String, Object>> firebaseData = firebaseHttpRepository.getFirebaseData(
                String.valueOf(users.getUserId()), accessToken,
                new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {
                });

        if (firebaseData != null) {
            //여기서 이름이 설정안된 친구만 찾고 firebase에서 삭제 + 해당 fishbowlId를 list에 저장
            for (Map.Entry<String, Map<String, Object>> entry : firebaseData.entrySet()) {
                Map<String, Object> fishbowlData = (Map<String, Object>) entry.getValue().get("device");

                Map<String, String> maps = new HashMap<>();
                maps.put("deviceToken",
                        redisService.getData(
                                redisTemplateForTokens,
                                "users : androidToken : " + users.getEmail()));

                if (fishbowlData.containsKey("deviceToken")) {
                    firebaseHttpRepository.updateFirebaseData(maps,
                            users.getUserId() + "/" + entry.getKey() + "/" + "device",
                            accessToken);
                }
            }
        }

        return SuccessDto.builder()
                .success(true)
                .build();
    }
}
