package com.aqualifeplus.aqualifeplus.controller;

import com.aqualifeplus.aqualifeplus.dto.LoginDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<?> signUp(@Valid @RequestBody UsersRequestDto usersRequestDto) {
        usersService.signUp(usersRequestDto);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(usersService.login(loginDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken() {
        return ResponseEntity.ok(
                usersService.refreshAccessToken());
    }

    @GetMapping("/my-info")
    public ResponseEntity<?> myInfo() {
        return ResponseEntity.ok(usersService.getMyInfo());
    }

    @PostMapping("/my-info")
    public ResponseEntity<?> myInfo(@Valid @RequestBody UsersResponseDto usersResponseDto) {
        return ResponseEntity.ok(usersService.updateMyInfo(usersResponseDto));
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<?> withdrawal() {
        usersService.deleteUser();

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/logout")
    public String logout() {
        usersService.logout();
        return "Logged out successfully";
    }
}
