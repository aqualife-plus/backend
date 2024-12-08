package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ph {
    private double warningMaxPh;
    private double warningMinPh;

    public static Ph startPhData() {
        return Ph.builder()
                .warningMaxPh(10.0)
                .warningMinPh(0.0)
                .build();
    }
}
