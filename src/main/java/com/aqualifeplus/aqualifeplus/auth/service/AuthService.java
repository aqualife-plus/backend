package com.aqualifeplus.aqualifeplus.auth.service;

import com.aqualifeplus.aqualifeplus.auth.dto.AndroidRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import jakarta.validation.Valid;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    String refreshAccessToken();

    SuccessDto setAndroidToken(@Valid AndroidRequestDto androidRequestDto);
}
