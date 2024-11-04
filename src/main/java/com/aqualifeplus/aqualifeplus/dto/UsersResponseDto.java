package com.aqualifeplus.aqualifeplus.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class UsersResponseDto {
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
}
