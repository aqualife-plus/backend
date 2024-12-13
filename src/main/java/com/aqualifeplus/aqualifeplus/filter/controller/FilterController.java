package com.aqualifeplus.aqualifeplus.filter.controller;

import com.aqualifeplus.aqualifeplus.filter.dto.FilterRequestDto;
import com.aqualifeplus.aqualifeplus.filter.service.FilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {
    private final FilterService filterService;

    @GetMapping
    public ResponseEntity<?> getFilter() {
        return ResponseEntity.ok(filterService.getFilter());
    }

    @PutMapping
    public ResponseEntity<?> updateFilter(@Valid @RequestBody FilterRequestDto filterRequestDto) {
        return ResponseEntity.ok(filterService.updateFilter(filterRequestDto));
    }
}
