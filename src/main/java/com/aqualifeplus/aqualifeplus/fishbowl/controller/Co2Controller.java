package com.aqualifeplus.aqualifeplus.fishbowl.controller;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Co2;
import com.aqualifeplus.aqualifeplus.fishbowl.service.Co2Service;
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
@RequestMapping("/co2")
@RestController
@RequiredArgsConstructor
public class Co2Controller {
    private final Co2Service co2Service;

    @GetMapping("/reserve-list")
    public ResponseEntity<?> co2ReserveList() {
        return ResponseEntity.ok(co2Service.co2ReserveList());
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> co2CreateReserve(@RequestBody Co2 co2) {
        return ResponseEntity.ok(co2Service.co2CreateReserve(co2));
    }

    @GetMapping("/reserve/{idx}")
    public ResponseEntity<?> co2Reserve(@PathVariable String idx) {
        return ResponseEntity.ok(co2Service.co2Reserve(idx));
    }

    @PutMapping("/reserve/{idx}")
    public ResponseEntity<?> co2UpdateReserve(@PathVariable String idx, @RequestBody Co2 co2) {
        return ResponseEntity.ok(co2Service.co2UpdateReserve(idx, co2));
    }

    @DeleteMapping("/reserve/{idx}")
    public ResponseEntity<?> co2DeleteReserve(@PathVariable String idx) {
        return ResponseEntity.ok(co2Service.co2DeleteReserve(idx));
    }
}
