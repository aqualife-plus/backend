package com.aqualifeplus.aqualifeplus.auth.dto;

import lombok.Getter;

@Getter
public class TokenResponseDto {
    private final String accessToken;
    private final String userToken;
    private final String refreshToken;

    public TokenResponseDto(String accessToken, String userToken, String refreshToken) {
        this.accessToken = "Bearer " + accessToken;
        this.userToken = userToken;
        this.refreshToken = "Bearer " + refreshToken;
    }
}
