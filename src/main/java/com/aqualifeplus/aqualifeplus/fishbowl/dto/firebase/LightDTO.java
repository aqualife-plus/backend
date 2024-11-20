package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LightDTO {
    private boolean lightReserveState;
    private String lightStartTime; // Format hh:mm
    private String lightEndTime;   // Format hh:mm

    public static LightDTO startLightData() {
        return LightDTO.builder()
                .lightReserveState(false)
                .lightStartTime("00:00")
                .lightEndTime("00:00")
                .build();
    }
}
