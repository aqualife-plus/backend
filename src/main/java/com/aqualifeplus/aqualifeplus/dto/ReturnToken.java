package com.aqualifeplus.aqualifeplus.dto;

import lombok.Getter;

@Getter
public class ReturnToken {
    private final String accessToken;
    private final String userToken;
    private final String refreshToken;

    public ReturnToken(String accessToken, String userToken, String refreshToken) {
        this.accessToken = "Bearer " + accessToken;
        this.userToken = userToken;
        this.refreshToken = "Bearer " + refreshToken;
    }
}
