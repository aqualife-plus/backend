package com.aqualifeplus.aqualifeplus.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @Email(message = "email 형태여야 합니다.")
    @NotBlank(message = "값이 필요합니다.")
    private String email;
}
