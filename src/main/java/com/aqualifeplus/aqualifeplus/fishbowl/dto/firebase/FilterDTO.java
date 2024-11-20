package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterDTO {
    private String filterDay; // "7x4" or "0/1"
    private int filterRange;  // Range 1-4
    private String filterTime; // Format hh:mm

    public static FilterDTO startFilterData() {
        return FilterDTO.builder()
                .filterDay("0000000")
                .filterRange(0)
                .filterTime("00:00")
                .build();
    }
}
