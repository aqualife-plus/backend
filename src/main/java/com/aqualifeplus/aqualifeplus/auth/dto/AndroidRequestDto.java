package com.aqualifeplus.aqualifeplus.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AndroidRequestDto {
    @NotNull(message = "Please token")
    @NotEmpty(message = "Please enter token")
    private String token;
}
