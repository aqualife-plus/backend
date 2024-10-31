package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.UsersDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import com.aqualifeplus.aqualifeplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService{
    private final UserRepository userRepository;

    @Override
    public void signUp(UsersDto usersDto) {
        System.out.println(toUsers(usersDto).toString());
        userRepository.save(toUsers(usersDto));
    }

    @Override
    public Long size() {
        return userRepository.count();
    }

    private Users toUsers(UsersDto usersDto) {
        return Users.builder()
                .userId(usersDto.getUserId())
                .email(usersDto.getEmail())
                .password(usersDto.getPassword())
                .nickname(usersDto.getNickname())
                .phoneNumber(usersDto.getPhoneNumber())
                .accessDate(usersDto.getAccessDate())
                .subscriptionDate(usersDto.getSubscriptionDate())
                .changeDate(usersDto.getChangeDate())
                .build();
    }
}
