package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;

public interface UsersService {
    public void signUp(UsersRequestDto usersRequestDto);
    public String login(UsersRequestDto usersRequestDto);
    public void logout(String username);
    public String getEmails(String accessToken);
}
