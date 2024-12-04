package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NowDTO {
    private boolean co2State;
    private boolean lightState;
    private double phState;
    private double tempState;
    private int filterState;

    public static NowDTO startNowData() {
        return NowDTO.builder()
                .co2State(false)
                .lightState(false)
                .phState(0.0)
                .tempState(0.0)
                .filterState(0)
                .build();
    }
}
