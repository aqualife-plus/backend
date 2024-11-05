package com.aqualifeplus.aqualifeplus.controller;

import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<?> signUp(@RequestBody UsersRequestDto usersRequestDto) {
        usersService.signUp(usersRequestDto);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsersRequestDto requestDto) {
        return ResponseEntity.ok(usersService.login(requestDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken() {
        return ResponseEntity.ok(
                usersService.refreshAccessToken());
    }

    @PostMapping("/logout")
    public String logout() {
        usersService.logout();
        return "Logged out successfully";
    }
}
