package com.aqualifeplus.aqualifeplus.co2.dto;

import com.aqualifeplus.aqualifeplus.co2.entity.Co2;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Co2ResponseDto {
    private Long id;
    private boolean co2ReserveState;
    private LocalTime co2StartTime; // Format hh:mm
    private LocalTime co2EndTime;   // Format hh:mm

    public static Co2ResponseDto toResponseDto(Co2 co2) {
        return Co2ResponseDto.builder()
                .id(co2.getId())
                .co2ReserveState(co2.isCo2ReserveState())
                .co2StartTime(co2.getCo2StartTime())
                .co2EndTime(co2.getCo2EndTime())
                .build();
    }
}
