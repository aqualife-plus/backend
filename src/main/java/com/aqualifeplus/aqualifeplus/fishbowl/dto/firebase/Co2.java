package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Co2 {
    private boolean co2ReserveState;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime co2StartTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime co2EndTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createDate;

    public static Co2 startCo2Data() {
        return Co2.builder()
                .co2ReserveState(false)
                .co2StartTime(LocalTime.of(0, 0))
                .co2EndTime(LocalTime.of(0, 0))
                .createDate(LocalDateTime.now())
                .build();
    }
}
