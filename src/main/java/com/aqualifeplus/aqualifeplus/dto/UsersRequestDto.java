package com.aqualifeplus.aqualifeplus.dto;

import com.aqualifeplus.aqualifeplus.entity.Users;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
public class UsersRequestDto {
    private Long userId;

    @Email(message = "Please enter it in email format")
    @NotEmpty(message = "Please enter email")
    private String email;

    @NotEmpty(message = "Please enter password")
    private String password;

    @NotEmpty(message = "Please enter nickname")
    private String nickname;

    @NotEmpty(message = "Please enter phoneNumber")
    @Size(min = 11, max = 11, message = "Please enter 11 digits")
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
