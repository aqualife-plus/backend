package com.aqualifeplus.aqualifeplus.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @Email(message = "Please enter it in email format")
    @NotEmpty(message = "Please enter email")
    private String email;
    @NotEmpty(message = "Please enter password")
    private String password;
}
