package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import javax.naming.Name;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FishbowlDTO {
    private String name;
    private NowDTO now;
    private List<Co2DTO> co2; // Array of Co2DTO
    private List<LightDTO> light; // Array of LightDTO
    private PhDTO ph;
    private TempDTO temp;
    private FilterDTO filter;

    public static FishbowlDTO makeFrame() {
        NowDTO nowDTO = NowDTO.startNowData();
        FilterDTO filterDTO = FilterDTO.startFilterData();
        Co2DTO co2DTO = Co2DTO.startCo2Data();
        LightDTO lightDTO  = LightDTO.startLightData();
        PhDTO phDTO  = PhDTO.startPhData();
        TempDTO tempDTO  = TempDTO.startTempData();

        return FishbowlDTO.builder()
                .name("이름을 정해주세요!")
                .now(nowDTO)
                .filter(filterDTO)
                .co2(List.of(co2DTO))
                .light(List.of(lightDTO))
                .ph(phDTO)
                .temp(tempDTO)
                .build();
    }


    public static Map<String, Object> convertDTOToMap(FishbowlDTO fishbowlDTO) {
        return new ObjectMapper().convertValue(fishbowlDTO, Map.class);
    }
}
