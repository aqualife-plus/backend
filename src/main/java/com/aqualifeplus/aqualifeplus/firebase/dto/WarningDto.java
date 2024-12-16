package com.aqualifeplus.aqualifeplus.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarningDto {
    private String deviceToken;
    private double tempState;
    private double phState;
    private double tempStay;
    private double warningMaxPh;
    private double warningMinPh;
}
