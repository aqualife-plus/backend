package com.aqualifeplus.aqualifeplus.firebase.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Now {
    private boolean co2State;
    private boolean lightState;
    private double phState;
    private double tempState;
    private int filterState;

    public static Now startNowData() {
        return Now.builder()
                .co2State(false)
                .lightState(false)
                .phState(0.0)
                .tempState(0.0)
                .filterState(0)
                .build();
    }
}
