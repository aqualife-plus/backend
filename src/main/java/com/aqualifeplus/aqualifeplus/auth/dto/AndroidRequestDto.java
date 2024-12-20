package com.aqualifeplus.aqualifeplus.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AndroidRequestDto {
    @NotBlank(message = "토큰을 넣어주세요.")
    private String token;
}
