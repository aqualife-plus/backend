package com.aqualifeplus.aqualifeplus.users.service;

import com.aqualifeplus.aqualifeplus.users.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupCheckDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupResponseDto;
import com.aqualifeplus.aqualifeplus.users.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersResponseDto;

public interface UsersService {
    SignupResponseDto signUp(UsersRequestDto usersRequestDto);
    SuccessDto checkEmail(SignupCheckDto signupCheckDto);
    UsersResponseDto getMyInfo();
    SuccessDto updateMyInfo(UsersResponseDto usersResponseDto);
    SuccessDto changePassword(PasswordChangeDto passwordUpdateRequestDto);
    SuccessDto deleteUser();
    SuccessDto logout();
    Long getId(String email);
}
