package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.LoginDto;
import com.aqualifeplus.aqualifeplus.dto.TokenDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;

public interface UsersService {
    void signUp(UsersRequestDto usersRequestDto);
    TokenDto login(LoginDto loginDto);
    String refreshAccessToken();
    UsersResponseDto getMyInfo();
    void logout();
}
