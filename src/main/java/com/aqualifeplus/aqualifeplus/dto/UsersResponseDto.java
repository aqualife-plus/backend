package com.aqualifeplus.aqualifeplus.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UsersResponseDto {
    @Email(message = "Please enter it in email format")
    @NotEmpty(message = "Please enter email")
    private String email;

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
}
