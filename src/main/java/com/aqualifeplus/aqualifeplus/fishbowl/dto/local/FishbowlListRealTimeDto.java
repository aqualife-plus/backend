package com.aqualifeplus.aqualifeplus.fishbowl.dto.local;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FishbowlListRealTimeDto {
    private double tempState;
    private double phState;
    private double tempStay;
    private double warningMaxPh;
    private double warningMinPh;
}
