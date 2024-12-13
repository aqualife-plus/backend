package com.aqualifeplus.aqualifeplus.filter.service;

import com.aqualifeplus.aqualifeplus.filter.dto.FilterRequestDto;
import com.aqualifeplus.aqualifeplus.filter.dto.FilterResponseDto;
import com.aqualifeplus.aqualifeplus.filter.dto.UpdateFilterResponseDto;
import jakarta.validation.Valid;

public interface FilterService {
    FilterResponseDto getFilter();

    UpdateFilterResponseDto updateFilter(@Valid FilterRequestDto filterRequestDto);
}
