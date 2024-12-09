package com.aqualifeplus.aqualifeplus.users.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class PasswordChangeDto {
    @NotEmpty(message = "Please enter your old password")
    private String oldPassword;
    @NotEmpty(message = "Please enter the password you want to change")
    private String changePassword;
}
