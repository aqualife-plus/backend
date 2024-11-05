package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.ReturnToken;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import com.aqualifeplus.aqualifeplus.repository.UserRepository;
import com.aqualifeplus.aqualifeplus.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final HttpServletRequest request;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void signUp(UsersRequestDto requestDto) {
        userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User already exists"));

        userRepository.save(
                requestDto.toUserForSignUp(passwordEncoder));
    }

    @Override
    public ReturnToken login(UsersRequestDto usersRequestDto) {
        String email = usersRequestDto.getEmail();
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (passwordEncoder.matches(usersRequestDto.getPassword(), user.getPassword())) {
            ReturnToken rt = new ReturnToken(
                    jwtUtil.makeAccessToken(email),
                    jwtUtil.makeUserToken(email),
                    jwtUtil.makeRefreshToken(email));

            redisTemplate.opsForValue().set(
                    "refreshToken:" + email,
                    rt.getRefreshToken().substring(7),
                    jwtUtil.getRefreshTokenExpirationMs(),
                    TimeUnit.MILLISECONDS);
            return rt;
        }

        return null;
    }

    @Override
    public String refreshAccessToken() {
        String email = jwtUtil.extractEmail(getAuthorization());
        String storedRefreshToken
                = redisTemplate.opsForValue().get("refreshToken:" + email);
        if (storedRefreshToken != null && storedRefreshToken.equals(getAuthorization())) {
            return jwtUtil.makeAccessToken(email);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    @Override
    public void logout() {
        redisTemplate.delete("refreshToken:" + getEmail());
    }

    private String getEmail() {
        return jwtUtil.extractEmail(getAuthorization());
    }

    private String getAuthorization() {
        return request.getHeader("Authorization").substring(7);
    }
}
