package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.LoginDto;
import com.aqualifeplus.aqualifeplus.dto.TokenDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import com.aqualifeplus.aqualifeplus.repository.UserRepository;
import com.aqualifeplus.aqualifeplus.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final HttpServletRequest request;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void signUp(UsersRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        userRepository.save(requestDto.toUserForSignUp(passwordEncoder));
    }

    @Override
    public TokenDto login(LoginDto loginDto) {
        String email = loginDto.getEmail();
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            TokenDto rt = new TokenDto(
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

        throw new RuntimeException("not match password or email");
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
    public UsersResponseDto getMyInfo() {
        return userRepository.findByEmail(getEmail())
                .orElseThrow(() -> new RuntimeException("have not a member."))
                .toUsersResponseDto();
    }

    @Override
    @Transactional
    public UsersResponseDto updateMyInfo(UsersResponseDto usersResponseDto) {
        Users users =  userRepository.findByEmail(getEmail())
                .orElseThrow(() -> new RuntimeException("have not a member."));

        users.setUpdateData(usersResponseDto);

        return users.toUsersResponseDto();
    }

    @Override
    public void deleteUser() {
        Users users =  userRepository.findByEmail(getEmail())
                .orElseThrow(() -> new RuntimeException("have not a member."));

        userRepository.delete(users);
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
