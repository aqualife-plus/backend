package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FishbowlDTO {
    private NowDTO now;
    private List<Co2DTO> co2; // Array of Co2DTO
    private List<LightDTO> light; // Array of LightDTO
    private PhDTO ph;
    private TempDTO temp;
    private FilterDTO filter;
}
