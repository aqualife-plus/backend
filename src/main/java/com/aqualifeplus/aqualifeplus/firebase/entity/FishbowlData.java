package com.aqualifeplus.aqualifeplus.firebase.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishbowlData {
    private String name;
    private String deviceToken;
    private Now now;
    private Ph ph;
    private Temp temp;
    private FilterData filterData;
    private Device device;

    public static FishbowlData makeFrame() {
        return FishbowlData.builder()
                .name("이름을 정해주세요!")
                .now(Now.startNowData())
                .filterData(FilterData.startFilterData())
                .ph(Ph.startPhData())
                .temp(Temp.startTempData())
                .device(Device.startDeviceData())
                .build();
    }


    public static Map<String, Object> convertDTOToMap(FishbowlData fishbowlData) {
        return new ObjectMapper().registerModule(new JavaTimeModule()).convertValue(fishbowlData, Map.class);
    }
}
