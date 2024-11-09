package com.aqualifeplus.aqualifeplus.dto;

import com.aqualifeplus.aqualifeplus.enum_type.LoginPlatform;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsersResponseDto {
    @NotEmpty(message = "Please enter nickname")
    private String nickname;

    @Size(min = 11, max = 11, message = "Please enter exactly 11 digits")
    @Pattern(regexp = "^$|^[0-9]{11}$", message = "Please enter 11 digits or leave it blank")
    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accessDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscriptionDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeDate;
}
