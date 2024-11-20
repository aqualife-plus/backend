package com.aqualifeplus.aqualifeplus.auth.service;

import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    String refreshAccessToken();
}
