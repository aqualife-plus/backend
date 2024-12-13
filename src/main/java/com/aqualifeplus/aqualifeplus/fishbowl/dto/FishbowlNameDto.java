package com.aqualifeplus.aqualifeplus.fishbowl.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishbowlNameDto {
    @NotEmpty(message = "이름이 필요합니다.")
    String name;
}
