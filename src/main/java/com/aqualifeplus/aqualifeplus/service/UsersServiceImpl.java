package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import com.aqualifeplus.aqualifeplus.repository.UserRepository;
import com.aqualifeplus.aqualifeplus.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void signUp(UsersRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        Users user = Users.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .accessDate(LocalDateTime.now())
                .subscriptionDate(LocalDateTime.now())
                .changeDate(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }

    public String login(UsersRequestDto usersRequestDto) {
        String email = usersRequestDto.getEmail();

        Optional<Users> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(usersRequestDto.getPassword(), user.get().getPassword())) {
            String token = jwtUtil.generateToken(email);
            redisTemplate.opsForValue().set(email, token, jwtUtil.getExpirationMs(), TimeUnit.MILLISECONDS);
            return "Bearer " + token;
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public void logout(String username) {
        redisTemplate.delete(username);
    }

    @Override
    public String getEmails(String accessToken) {
        System.out.println("getEmails" + accessToken);
        return jwtUtil.extractEmail(accessToken);
    }

    private Users toUsers(UsersRequestDto requestDto) {
        return Users.builder()
                .userId(requestDto.getUserId())
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .accessDate(requestDto.getAccessDate())
                .subscriptionDate(requestDto.getSubscriptionDate())
                .changeDate(requestDto.getChangeDate())
                .build();
    }
}
