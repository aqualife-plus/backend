package com.aqualifeplus.aqualifeplus.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDto {
    private final boolean success;
    private final String email;
}
