package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Co2DTO {
    private boolean co2ReserveState;
    private String co2StartTime; // Format hh:mm
    private String co2EndTime;   // Format hh:mm

    public static Co2DTO startCo2Data() {
        return Co2DTO.builder()
                .co2ReserveState(false)
                .co2StartTime("00:00")
                .co2EndTime("00:00")
                .build();
    }
}
