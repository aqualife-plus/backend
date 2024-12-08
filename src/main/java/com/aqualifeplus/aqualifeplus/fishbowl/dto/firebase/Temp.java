package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Temp {
    private double tempStay;

    public static Temp startTempData() {
        return Temp.builder()
                .tempStay(0.0)
                .build();
    }
}
