package com.aqualifeplus.aqualifeplus.light.dto;

import com.aqualifeplus.aqualifeplus.common.deserializer.CustomBooleanDeserializer;
import com.aqualifeplus.aqualifeplus.common.deserializer.LocalTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;

@Getter
public class LightRequestDto {
    @NotNull(message = "예약 설정값이 필요합니다.")
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private boolean lightReserveState;
    @NotNull(message = "예약 시작값이 필요합니다.")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime lightStartTime;
    @NotNull(message = "예약 끝값이 필요합니다.")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime lightEndTime;
}
