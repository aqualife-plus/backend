package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TempDTO {
    private double tempStay;

    public static TempDTO startTempData() {
        return TempDTO.builder()
                .tempStay(0.0)
                .build();
    }
}
