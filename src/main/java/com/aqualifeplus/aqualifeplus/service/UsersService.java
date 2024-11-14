package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.dto.SignUpDto;
import com.aqualifeplus.aqualifeplus.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;

public interface UsersService {
    SignUpDto signUp(UsersRequestDto usersRequestDto);
    boolean checkEmail(String email);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    String refreshAccessToken();
    UsersResponseDto getMyInfo();
    boolean updateMyInfo(UsersResponseDto usersResponseDto);
    boolean changePassword(PasswordChangeDto passwordUpdateRequestDto);
    boolean deleteUser();
    boolean logout();
}
