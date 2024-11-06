package com.aqualifeplus.aqualifeplus.dto;

import lombok.Getter;

@Getter
public class TokenDto {
    private final String accessToken;
    private final String userToken;
    private final String refreshToken;

    public TokenDto(String accessToken, String userToken, String refreshToken) {
        this.accessToken = "Bearer " + accessToken;
        this.userToken = userToken;
        this.refreshToken = "Bearer " + refreshToken;
    }
}
