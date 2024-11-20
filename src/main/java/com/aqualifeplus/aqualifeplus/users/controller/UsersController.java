package com.aqualifeplus.aqualifeplus.users.controller;

import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupCheckDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UsersController {
    private final UsersService usersService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UsersRequestDto usersRequestDto) {
        return ResponseEntity.ok(usersService.signUp(usersRequestDto));
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody SignupCheckDto signupCheckDto) {
        return ResponseEntity.ok(usersService.checkEmail(signupCheckDto.getEmail()));
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
