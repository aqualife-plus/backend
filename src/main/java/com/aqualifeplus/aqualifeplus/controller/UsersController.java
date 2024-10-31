package com.aqualifeplus.aqualifeplus.controller;

import com.aqualifeplus.aqualifeplus.dto.UsersDto;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UsersController {
    private final UsersService usersService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UsersDto usersDto) {
        usersService.signUp(usersDto);
        return ResponseEntity.ok(usersService.size());
    }
}
