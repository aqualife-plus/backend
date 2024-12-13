package com.aqualifeplus.aqualifeplus.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @Email(message = "Please enter it in email format")
    @NotNull(message = "Please enter email")
    private String email;
    @NotNull(message = "Please enter password")
    private String password;
}
