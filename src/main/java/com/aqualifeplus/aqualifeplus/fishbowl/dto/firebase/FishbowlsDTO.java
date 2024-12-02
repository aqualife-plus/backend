package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class FishbowlsDTO {
    private FishbowlDTO fishbowl; // Map with fishbowl UUID as keys

    public static FishbowlsDTO makeFrame(long userId) {
        NowDTO nowDTO = NowDTO.startNowData();
        FilterDTO filterDTO = FilterDTO.startFilterData();
        Co2DTO co2DTO = Co2DTO.startCo2Data();
        LightDTO lightDTO  = LightDTO.startLightData();
        PhDTO phDTO  = PhDTO.startPhData();
        TempDTO tempDTO  = TempDTO.startTempData();

        FishbowlDTO fishbowlDTO = FishbowlDTO.builder()
                .name("이름을 정해주세요!")
                .now(nowDTO)
                .filter(filterDTO)
                .co2(List.of(co2DTO))
                .light(List.of(lightDTO))
                .ph(phDTO)
                .temp(tempDTO)
                .build();

        return FishbowlsDTO.builder()
                .fishbowl(fishbowlDTO)
                .build();
    }

    public static Map<String, Object> convertDTOToMap(FishbowlsDTO fishbowlDTO) {
        return new ObjectMapper().convertValue(fishbowlDTO, Map.class);
    }
}

