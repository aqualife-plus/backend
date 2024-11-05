package com.aqualifeplus.aqualifeplus.dto;

import com.aqualifeplus.aqualifeplus.entity.Users;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
public class UsersRequestDto {
    private Long userId;

    private String email;

    private String password;

    private String nickname;

    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accessDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscriptionDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeDate;

    public Users toUserForSignUp(BCryptPasswordEncoder passwordEncoder) {
        return Users.builder()
                .email(this.email)
                .password(passwordEncoder.encode(this.getPassword()))
                .nickname(this.nickname)
                .phoneNumber(this.phoneNumber)
                .accessDate(LocalDateTime.now())
                .subscriptionDate(LocalDateTime.now())
                .changeDate(LocalDateTime.now())
                .build();
    }

    public Users toUser() {
        return Users.builder()
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .phoneNumber(this.phoneNumber)
                .accessDate(this.accessDate)
                .subscriptionDate(this.subscriptionDate)
                .changeDate(this.changeDate)
                .build();
    }
}
