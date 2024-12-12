package com.aqualifeplus.aqualifeplus.ph.service;

import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitRequestDto;
import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitResponseDto;

public interface PhService {
    UpdatePhLimitResponseDto updatePhLimit(UpdatePhLimitRequestDto updatePhLimitRequestDto);
}
