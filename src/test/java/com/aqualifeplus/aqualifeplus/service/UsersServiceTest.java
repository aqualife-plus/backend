package com.aqualifeplus.aqualifeplus.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import com.aqualifeplus.aqualifeplus.users.service.UsersServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class UsersServiceTest {
    @Mock
    private UsersRepository usersRepository;

    @Mock
    private JwtService jwtService; // JwtService도 Mock으로 설정

    @InjectMocks
    private UsersServiceImpl usersServiceImpl; // UsersService에 필요한 모의 객체 주입

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_success() {
        // given
        UsersRequestDto usersRequestDto = UsersRequestDto.builder()
                .email("test@test.com")
                .password("testPassword")
                .nickname("testNickname")
                .phoneNumber("1234567890")
                .build();

        Users mockUser = usersRequestDto
                .toUserForSignUp(new BCryptPasswordEncoder());

        when(usersRepository.save(any(Users.class))).thenReturn(mockUser); // Repository가 save 호출 시 mockUser 반환

        // when
//        boolean result = usersServiceImpl.signUp(usersRequestDto);

        // then
//        assertTrue(result); // 회원가입이 성공적이어야 한다고 가정하고 True 여부 확인
//        verify(usersRepository, times(1)).save(any(Users.class)); // save가 한 번 호출되었는지 확인
    }
}