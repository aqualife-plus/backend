package com.aqualifeplus.aqualifeplus.light.dto;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public class LightRequestDto {
    private boolean lightReserveState;
    private LocalTime lightStartTime; // Format hh:mm
    private LocalTime lightEndTime;   // Format hh:mm
}
