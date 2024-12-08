package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fishbowl {
    private String name;
    private Now now;
    private Map<String, Co2> co2; // map of Co2DTO
    private Map<String, Light> light; // map of LightDTO
    private Ph ph;
    private Temp temp;
    private Filter filter;

    public static Fishbowl makeFrame() {
        Now now = Now.startNowData();
        Filter filter = Filter.startFilterData();
        Co2 co2 = com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Co2.startCo2Data();
        Light light  = Light.startLightData();
        Ph ph  = Ph.startPhData();
        Temp temp  = Temp.startTempData();

        Map<String, Co2> co2Map = new HashMap<>();
        co2Map.put(UUID.randomUUID().toString(), co2);
        Map<String, Light> lightMap = new HashMap<>();
        lightMap.put(UUID.randomUUID().toString(), light);

        return Fishbowl.builder()
                .name("이름을 정해주세요!")
                .now(now)
                .filter(filter)
                .co2(co2Map)
                .light(lightMap)
                .ph(ph)
                .temp(temp)
                .build();
    }


    public static Map<String, Object> convertDTOToMap(Fishbowl fishbowl) {
        return new ObjectMapper().registerModule(new JavaTimeModule()).convertValue(fishbowl, Map.class);
    }
}
