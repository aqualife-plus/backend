package com.aqualifeplus.aqualifeplus.fishbowl.controller;

import com.aqualifeplus.aqualifeplus.fishbowl.service.FishbowlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/name-set")
    public ResponseEntity<?> nameSet(@RequestParam("name") String name) {
        return ResponseEntity.ok(fishbowlService.nameSet(name));
    }
}
