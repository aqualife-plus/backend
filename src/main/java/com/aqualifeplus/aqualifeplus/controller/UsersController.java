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
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UsersController {
    private final UsersService usersService;

    @GetMapping("/google/login")
    public RedirectView googleLogin() {
        return new RedirectView("/oauth2/authorization/google");
    }

    @GetMapping("/naver/login")
    public RedirectView naverLogin() {
        return new RedirectView("/oauth2/authorization/naver");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UsersRequestDto usersRequestDto) {
        return ResponseEntity.ok(usersService.signUp(usersRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(usersService.login(loginDto));
    }

    @PostMapping("/refresh-token")
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
