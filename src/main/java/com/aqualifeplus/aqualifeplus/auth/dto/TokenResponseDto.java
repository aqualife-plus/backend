package com.aqualifeplus.aqualifeplus.auth.dto;

import lombok.Getter;

@Getter
public class TokenResponseDto {
    private final String accessToken;
    private final String refreshToken;

    public TokenResponseDto(String accessToken, String refreshToken) {
        this.accessToken = "Bearer " + accessToken;
        this.refreshToken = "Bearer " + refreshToken;
    }
}
