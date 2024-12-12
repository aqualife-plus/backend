package com.aqualifeplus.aqualifeplus.temp.service;

import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitRequestDto;
import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitResponseDto;
import jakarta.validation.Valid;

public interface TempService {
    UpdateTempLimitResponseDto updateTempLimit(
            @Valid UpdateTempLimitRequestDto updateTempLimitRequestDto);
}
