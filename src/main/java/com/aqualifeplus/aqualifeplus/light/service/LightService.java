package com.aqualifeplus.aqualifeplus.light.service;

import com.aqualifeplus.aqualifeplus.light.dto.DeleteLightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightRequestDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightResponseDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightSuccessDto;
import java.util.List;

public interface LightService {
    List<LightResponseDto> lightReserveList();

    LightResponseDto lightReserve(Long idx);

    LightSuccessDto lightCreateReserve(LightRequestDto lightRequestDto);

    LightSuccessDto lightUpdateReserve(Long idx, LightRequestDto lightRequestDto);

    DeleteLightSuccessDto lightDeleteReserve(Long idx);
}
