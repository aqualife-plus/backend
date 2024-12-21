package com.aqualifeplus.aqualifeplus.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeDto {
    @NotBlank(message = "지금 비밀번호를 입력해주세요.")
    private String oldPassword;
    @NotBlank(message = "바꿀 비밀번호를 입력해주세요.")
    private String changePassword;
}
