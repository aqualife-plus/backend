package com.aqualifeplus.aqualifeplus.fishbowl.controller;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Light;
import com.aqualifeplus.aqualifeplus.fishbowl.service.LightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/light")
@RestController
@RequiredArgsConstructor
public class LightController {
    private final LightService lightService;

    @GetMapping("/reserve-list")
    public ResponseEntity<?> lightReserveList() {
        return ResponseEntity.ok(lightService.lightReserveList());
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> lightCreateReserve(@RequestBody Light light) {
        return ResponseEntity.ok(lightService.lightCreateReserve(light));
    }

    @GetMapping("/reserve/{idx}")
    public ResponseEntity<?> lightReserve(@PathVariable String idx) {
        return ResponseEntity.ok(lightService.lightReserve(idx));
    }

    @PutMapping("/reserve/{idx}")
    public ResponseEntity<?> lightUpdateReserve(@PathVariable String idx, @RequestBody Light light) {
        return ResponseEntity.ok(lightService.lightUpdateReserve(idx, light));
    }

    @DeleteMapping("/reserve/{idx}")
    public ResponseEntity<?> lightDeleteReserve(@PathVariable String idx) {
        return ResponseEntity.ok(lightService.lightDeleteReserve(idx));
    }
}