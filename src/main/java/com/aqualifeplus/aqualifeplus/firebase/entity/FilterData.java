package com.aqualifeplus.aqualifeplus.firebase.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterData {
    private boolean filterOnOff;
    private int filterRange;  // Range 1-4

    public static FilterData startFilterData() {
        return FilterData.builder()
                .filterOnOff(false)
                .filterRange(0)
                .build();
    }
}
