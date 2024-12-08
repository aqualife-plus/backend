package com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Filter {
    private String filterDay; // "7x4" or "0/1"
    private int filterRange;  // Range 1-4
    @JsonFormat(pattern = "HH:mm")
    private LocalTime filterTime; // Format hh:mm

    public static Filter startFilterData() {
        return Filter.builder()
                .filterDay("0000000")
                .filterRange(0)
                .filterTime(LocalTime.of(0, 0))
                .build();
    }
}
