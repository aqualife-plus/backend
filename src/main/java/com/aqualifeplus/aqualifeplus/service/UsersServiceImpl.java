package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.dto.SignUpDto;
import com.aqualifeplus.aqualifeplus.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import com.aqualifeplus.aqualifeplus.exception.CustomException;
import com.aqualifeplus.aqualifeplus.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService{
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final HttpServletRequest request;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public SignUpDto signUp(UsersRequestDto requestDto) {
        if (usersRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        usersRepository.save(requestDto.toUserForSignUp(passwordEncoder));
        return new SignUpDto(
                true,
                requestDto.getEmail());
    }

    @Override
    public boolean checkEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (email == null || !Pattern.matches(emailRegex, email)) {
            throw new CustomException(ErrorCode.NULL_AND_NOT_FORMAT_EMAIL);
        }

        return !usersRepository.existsByEmail(email);
    }

    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (passwordEncoder.matches(loginRequestDto.getPassword(), users.getPassword())) {
            TokenResponseDto rt = new TokenResponseDto(
                    jwtService.makeAccessToken(email),
                    jwtService.makeUserToken(email),
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
        String email = jwtService.extractEmail(getAuthorization());
        String storedRefreshToken
                = redisTemplate.opsForValue().get("refreshToken:" + email);
        if (storedRefreshToken != null && storedRefreshToken.equals(getAuthorization())) {
            return jwtService.makeAccessToken(email);
        } else {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    @Override
    public UsersResponseDto getMyInfo() {
        return usersRepository.findByEmail(getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER))
                .toUsersResponseDto();
    }

    @Override
    @Transactional
    public boolean updateMyInfo(UsersResponseDto usersResponseDto) {
        Users users =  usersRepository.findByEmail(getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        users.setUpdateData(usersResponseDto);

        return true;
    }

    @Override
    @Transactional
    public boolean changePassword(PasswordChangeDto passwordUpdateRequestDto) {
        Users users =  usersRepository.findByEmail(getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (passwordEncoder.matches(
                passwordUpdateRequestDto.getOldPassword(), users.getPassword())) {
            users.setPassword(
                    passwordEncoder.encode(passwordUpdateRequestDto.getChangePassword()));
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteUser() {
        Users users =  usersRepository.findByEmail(getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        usersRepository.delete(users);

        return true;
    }

    @Override
    public boolean logout() {
        return Boolean.TRUE.equals(
                redisTemplate.delete("refreshToken:" + getEmail()));
    }

    private String getEmail() {
        return jwtService.extractEmail(getAuthorization());
    }

    private String getAuthorization() {
        return request.getHeader("Authorization").substring(7);
    }
}
