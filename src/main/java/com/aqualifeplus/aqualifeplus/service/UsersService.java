package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.ReturnToken;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UsersService {
    void signUp(UsersRequestDto usersRequestDto);
    ReturnToken login(UsersRequestDto usersRequestDto);
    String refreshAccessToken();
    void logout();
}
