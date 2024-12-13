package com.aqualifeplus.aqualifeplus.filter.dto;

import com.aqualifeplus.aqualifeplus.common.deserializer.FilterDayDeserializer;
import com.aqualifeplus.aqualifeplus.common.deserializer.FilterRangeDeserializer;
import com.aqualifeplus.aqualifeplus.common.deserializer.LocalTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequestDto {
    @NotNull(message = "설정할 요일데이터가 필요합니다.")
    @JsonDeserialize(using = FilterDayDeserializer.class)
    private String filterDay;
    @NotNull(message = "설정할 환수량이 필요합니다.")
    @JsonDeserialize(using = FilterRangeDeserializer.class)
    private Integer filterRange;
    @NotNull(message = "설정할 시간이 필요합니다.")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime filterTime;
}
