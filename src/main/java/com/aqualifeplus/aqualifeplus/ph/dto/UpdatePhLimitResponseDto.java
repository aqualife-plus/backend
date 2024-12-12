package com.aqualifeplus.aqualifeplus.ph.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePhLimitResponseDto {
    private boolean success;
    private double warningMaxPh;
    private double warningMinPh;
}
