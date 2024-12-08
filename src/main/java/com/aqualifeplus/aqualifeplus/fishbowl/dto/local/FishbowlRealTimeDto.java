package com.aqualifeplus.aqualifeplus.fishbowl.dto.local;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishbowlRealTimeDto {
    private boolean co2State;
    private boolean lightState;
    private long filterState;
    private double tempState;
    private double phState;
    private double tempStay;
    private double warningMaxPh;
    private double warningMinPh;
}