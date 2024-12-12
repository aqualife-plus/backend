package com.aqualifeplus.aqualifeplus.ph.controller;

import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitRequestDto;
import com.aqualifeplus.aqualifeplus.ph.service.PhService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ph")
@RequiredArgsConstructor
public class PhController {
    private final PhService phService;

    @PutMapping
    public ResponseEntity<?> updatePhLimit(
            @Valid @RequestBody UpdatePhLimitRequestDto updatePhLimitRequestDto) {
        return ResponseEntity.ok(phService.updatePhLimit(updatePhLimitRequestDto));
    }
}
