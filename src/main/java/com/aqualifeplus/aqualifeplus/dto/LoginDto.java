package com.aqualifeplus.aqualifeplus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDto {
    @Email(message = "Please enter it in email format")
    @NotEmpty(message = "Please enter email")
    private String email;
    @NotEmpty(message = "Please enter password")
    private String password;
}
