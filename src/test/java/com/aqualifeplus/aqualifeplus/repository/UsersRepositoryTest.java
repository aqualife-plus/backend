package com.aqualifeplus.aqualifeplus.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UsersRepositoryTest {
    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveAndFind() {
        // given
        UsersRequestDto usersRequestDto = UsersRequestDto.builder()
                .email("1@1.com")
                .password("testPW")
                .nickname("testNick")
                .phoneNumber(null)
                .build();

        Users user = usersRequestDto.toUserForSignUp(new BCryptPasswordEncoder());

        // when
        Users savedUser = usersRepository.save(user);
        Users foundUser =
                usersRepository
                        .findById(savedUser.getUserId())
                        .orElse(null);

        // then
        assertNotNull(foundUser);
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }
}