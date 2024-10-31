package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.dto.UsersDto;

public interface UsersService {
    public void signUp(UsersDto usersDto);
    public Long size();
}
