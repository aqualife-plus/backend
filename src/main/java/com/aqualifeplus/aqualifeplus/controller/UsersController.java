package com.aqualifeplus.aqualifeplus.controller;

import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String login(@RequestBody UsersRequestDto requestDto) {
        return usersService.login(requestDto);
    }

    @PostMapping("/logout")
    public String logout(@RequestParam String email) {
        usersService.logout(email);
        return "Logged out successfully";
    }

    @GetMapping("/test")
    public String test(@RequestHeader("Authorization") String token) {
        // 토큰에서 "Bearer " 부분 제거
        String accessToken = token.substring(7);
        System.out.println("get : " + accessToken);
        String email = usersService.getEmails(accessToken);
        return "Email: " + email;
    }
}
