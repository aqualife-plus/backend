package com.aqualifeplus.aqualifeplus.ph.dto;

import com.aqualifeplus.aqualifeplus.common.deserializer.CustomPhDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.AssertTrue;
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

    @AssertTrue(message = "최저치값(warningMinPh)은 최대치값(warningMaxPh)보다 작아야 합니다.")
    public boolean isValidPhRange() {
        if (warningMaxPh == null || warningMinPh == null) {
            return true; // null 상태는 @NotNull로 처리
        }
        return warningMinPh <= warningMaxPh;
    }
}
