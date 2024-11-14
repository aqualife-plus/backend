package com.aqualifeplus.aqualifeplus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpDto {
    private final boolean success;
    private final String email;
}
