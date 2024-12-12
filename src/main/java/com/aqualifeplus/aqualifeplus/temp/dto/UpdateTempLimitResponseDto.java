package com.aqualifeplus.aqualifeplus.temp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateTempLimitResponseDto {
    private boolean success;
    private double tempStay;
}
