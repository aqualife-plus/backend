package com.aqualifeplus.aqualifeplus.controller;

import com.aqualifeplus.aqualifeplus.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(String email) {
        return ResponseEntity.ok(usersService.checkEmail(email));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(usersService.login(loginRequestDto));
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

    @PutMapping("/my-info")
    public ResponseEntity<?> myInfo(@Valid @RequestBody UsersResponseDto usersResponseDto) {
        return ResponseEntity.ok(usersService.updateMyInfo(usersResponseDto));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeDto passwordUpdateRequestDto) {
        return ResponseEntity.ok(usersService.changePassword(passwordUpdateRequestDto));
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<?> withdrawal() {
        return ResponseEntity.ok(usersService.deleteUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(usersService.logout());
    }
}
