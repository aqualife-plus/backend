package com.aqualifeplus.aqualifeplus.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @Email(message = "이메일 형식을 입력해야 합니다.")
    @NotBlank(message = "이메일을 입력해야합니다.")
    private String email;
    @NotBlank(message = "비밀번호를 입력해야 합니다.")
    private String password;
}
