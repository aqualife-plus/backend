package com.aqualifeplus.aqualifeplus.temp.dto;

import com.aqualifeplus.aqualifeplus.common.deserializer.CustomTempDeserializer;
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
public class UpdateTempLimitRequestDto {
    @NotNull(message = "온도 유지값이 필요합니다.")
    @JsonDeserialize(using = CustomTempDeserializer.class)
    private Double tempStay;
}
