package com.aqualifeplus.aqualifeplus.entity;

import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String email;

    private String password;

    private String nickname;

    private String phoneNumber;

    private LocalDateTime accessDate;

    private LocalDateTime subscriptionDate;

    private LocalDateTime changeDate;

    public UsersResponseDto toUsersResponseDto() {
        return UsersResponseDto.builder()
                .userId(this.userId)
                .email(this.email)
                .nickname(this.nickname)
                .phoneNumber(this.phoneNumber)
                .accessDate(this.accessDate)
                .subscriptionDate(this.subscriptionDate)
                .changeDate(this.changeDate)
                .build();
    }
}
