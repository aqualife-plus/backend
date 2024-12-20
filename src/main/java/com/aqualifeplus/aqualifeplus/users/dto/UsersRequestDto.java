package com.aqualifeplus.aqualifeplus.users.dto;

import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.common.enum_type.LoginPlatform;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersRequestDto {
    @NotBlank(message = "이메일을 입력해야 합니다.")
    @Email(message = "이메일 형식으로 입력해야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임을 입력해야 합니다.")
    private String nickname;

    @Pattern(regexp = "^$|^[0-9]{11}$", message = "11자리의 숫자가 필요합니다.")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private LoginPlatform accountType;

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
                .accountType(this.accountType == null ? LoginPlatform.AQUA_LIFE : this.accountType)
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
