package com.aqualifeplus.aqualifeplus.temp.controller;

import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitRequestDto;
import com.aqualifeplus.aqualifeplus.temp.service.TempService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/temp")
@RequiredArgsConstructor
public class TempController {
    private final TempService tempService;

    @PutMapping
    public ResponseEntity<?> updateTempLimit(
            @Valid @RequestBody UpdateTempLimitRequestDto updateTempLimitRequestDto) {
        return ResponseEntity.ok(tempService.updateTempLimit(updateTempLimitRequestDto));
    }
}
