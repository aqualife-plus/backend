package com.aqualifeplus.aqualifeplus.fishbowl.controller;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.FishbowlNameDto;
import com.aqualifeplus.aqualifeplus.fishbowl.service.FishbowlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fishbowl")
@RequiredArgsConstructor
public class FishbowlController {
    private final FishbowlService fishbowlService;

    @GetMapping("/connect")
    public ResponseEntity<?> connect() {
        return ResponseEntity.ok(fishbowlService.connect());
    }

    @PostMapping("/name")
    public ResponseEntity<?> createFishbowlName(@Valid @RequestBody FishbowlNameDto fishbowlNameDto) {
        return ResponseEntity.ok(fishbowlService.createFishbowlName(fishbowlNameDto));
    }

    @PatchMapping("/name")
    public ResponseEntity<?> updateFishbowlName(@Valid @RequestBody FishbowlNameDto fishbowlNameDto) {
        return ResponseEntity.ok(fishbowlService.updateFishbowlName(fishbowlNameDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFishbowl() {
        return ResponseEntity.ok(fishbowlService.deleteFishbowl());
    }
}
