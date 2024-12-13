package com.aqualifeplus.aqualifeplus.filter.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterResponseDto {
    private String filterDay;
    private int filterRange;
    private LocalTime filterTime;
}
