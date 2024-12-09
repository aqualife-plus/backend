package com.aqualifeplus.aqualifeplus.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.signature.qual.BinaryName;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupCheckDto {
    private String email;
}
