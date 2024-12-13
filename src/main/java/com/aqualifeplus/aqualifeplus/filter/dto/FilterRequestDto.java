package com.aqualifeplus.aqualifeplus.filter.dto;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequestDto {
    private String filterDay;
    private int filterRange;
    private LocalTime filterTime;
}
