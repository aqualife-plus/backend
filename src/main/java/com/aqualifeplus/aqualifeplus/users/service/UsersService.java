package com.aqualifeplus.aqualifeplus.users.service;

import com.aqualifeplus.aqualifeplus.users.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupResponseDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersResponseDto;

public interface UsersService {
    SignupResponseDto signUp(UsersRequestDto usersRequestDto);
    boolean checkEmail(String email);
    UsersResponseDto getMyInfo();
    boolean updateMyInfo(UsersResponseDto usersResponseDto);
    boolean changePassword(PasswordChangeDto passwordUpdateRequestDto);
    boolean deleteUser();
    boolean logout();
}
