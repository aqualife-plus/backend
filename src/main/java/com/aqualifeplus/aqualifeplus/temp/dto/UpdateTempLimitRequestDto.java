package com.aqualifeplus.aqualifeplus.temp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTempLimitRequestDto {
    private double tempStay;
}
