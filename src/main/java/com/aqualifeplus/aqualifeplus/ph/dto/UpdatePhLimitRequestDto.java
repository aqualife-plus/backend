package com.aqualifeplus.aqualifeplus.ph.dto;

import com.aqualifeplus.aqualifeplus.common.deserializer.CustomPhDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePhLimitRequestDto {
    @NotNull(message = "ph 최대치값이 필요합니다.")
    @JsonDeserialize(using = CustomPhDeserializer.class)
    private Double warningMaxPh;
    @NotNull(message = "ph 최저치값이 필요합니다.")
    @JsonDeserialize(using = CustomPhDeserializer.class)
    private Double warningMinPh;
}
