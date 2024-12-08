package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Light {
    private boolean lightReserveState;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime lightStartTime; // Format hh:mm
    @JsonFormat(pattern = "HH:mm")
    private LocalTime lightEndTime;   // Format hh:mm
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createDate;

    public static Light startLightData() {
        return Light.builder()
                .lightReserveState(false)
                .lightStartTime(LocalTime.of(0, 0))
                .lightEndTime(LocalTime.of(0, 0))
                .createDate(LocalDateTime.now())
                .build();
    }
}
