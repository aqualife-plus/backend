package com.aqualifeplus.aqualifeplus.co2.dto;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public class Co2RequestDto {
    private boolean co2ReserveState;
    private LocalTime co2StartTime; // Format hh:mm
    private LocalTime co2EndTime;   // Format hh:mm
}
