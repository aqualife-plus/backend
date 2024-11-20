package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhDTO {
    private double warningMaxPh;
    private double warningMinPh;

    public static PhDTO startPhData() {
        return PhDTO.builder()
                .warningMaxPh(10.0)
                .warningMinPh(0.0)
                .build();
    }
}
