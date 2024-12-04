package com.aqualifeplus.aqualifeplus.auth.service;

import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, String> redisTemplate;

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

            redisTemplate.opsForValue().set(
                    "refreshToken:" + email,
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
                redisTemplate.opsForValue().get("refreshToken:" + email);
        if (storedRefreshToken != null && storedRefreshToken.equals(authData)) {
            return "Bearer " + jwtService.makeAccessToken(email);
        } else {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
