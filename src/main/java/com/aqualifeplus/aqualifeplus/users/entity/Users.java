package com.aqualifeplus.aqualifeplus.users.entity;

import com.aqualifeplus.aqualifeplus.users.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.common.enum_type.LoginPlatform;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String email;

    @Setter
    private String password;

    private String nickname;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private LoginPlatform accountType;

    private LocalDateTime accessDate;

    private LocalDateTime subscriptionDate;

    private LocalDateTime changeDate;

    public UsersResponseDto toUsersResponseDto() {
        return UsersResponseDto.builder()
                .nickname(this.nickname)
                .phoneNumber(this.phoneNumber)
                .accessDate(this.accessDate)
                .subscriptionDate(this.subscriptionDate)
                .changeDate(this.changeDate)
                .build();
    }

    public void setUpdateData(UsersResponseDto usersResponseDto) {
        this.nickname = usersResponseDto.getNickname();
        this.phoneNumber = usersResponseDto.getPhoneNumber();
        this.subscriptionDate = LocalDateTime.now();
    }
}
