package com.aqualifeplus.aqualifeplus.users.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.users.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupResponseDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService{
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public SignupResponseDto signUp(UsersRequestDto requestDto) {
        if (usersRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        usersRepository.save(requestDto.toUserForSignUp(passwordEncoder));
        return new SignupResponseDto(
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
    public UsersResponseDto getMyInfo() {
        return usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER))
                .toUsersResponseDto();
    }

    @Override
    @Transactional
    public boolean updateMyInfo(UsersResponseDto usersResponseDto) {
        Users users =  usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        users.setUpdateData(usersResponseDto);

        return true;
    }

    @Override
    @Transactional
    public boolean changePassword(PasswordChangeDto passwordUpdateRequestDto) {
        Users users =  usersRepository.findByEmail(jwtService.getEmail())
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
        Users users =  usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        usersRepository.delete(users);

        return true;
    }

    @Override
    public boolean logout() {
        return Boolean.TRUE.equals(
                redisTemplate.delete("refreshToken:" + jwtService.getEmail()));
    }
}
