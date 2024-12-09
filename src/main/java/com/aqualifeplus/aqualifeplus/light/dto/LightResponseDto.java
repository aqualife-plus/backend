package com.aqualifeplus.aqualifeplus.light.dto;

import com.aqualifeplus.aqualifeplus.light.entity.Light;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LightResponseDto {
    private Long id;
    private boolean lightReserveState;
    private LocalTime lightStartTime; // Format hh:mm
    private LocalTime lightEndTime;   // Format hh:mm

    public static LightResponseDto toResponseDto(Light light) {
        return LightResponseDto.builder()
                .id(light.getId())
                .lightReserveState(light.isLightReserveState())
                .lightStartTime(light.getLightStartTime())
                .lightEndTime(light.getLightEndTime())
                .build();
    }
}
