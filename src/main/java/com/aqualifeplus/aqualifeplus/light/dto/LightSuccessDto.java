package com.aqualifeplus.aqualifeplus.light.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LightSuccessDto {
    private boolean success;
    private LightResponseDto lightResponseDto;
}
