package com.aqualifeplus.aqualifeplus.controller;

import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.exception.CustomException;
import com.aqualifeplus.aqualifeplus.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(UsersController.class)
class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsersService usersService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    private final UsersRequestDto usersRequestDto =
            UsersRequestDto.builder()
                    .email("1@1.com")
                    .password("test password")
                    .nickname("test nickname")
                    .phoneNumber(null)
                    .build();

    @Test
    @DisplayName("회원가입 성공")
    void successSignup() throws Exception {
        // given
        // when
        when(usersService.signUp(any(UsersRequestDto.class))).thenReturn(true);
        // then
        String responseValue = mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals("true", responseValue);
    }

    @Test
    @DisplayName("회원가입 실패 -> 이미 가입된 email or id로 가입 시도")
    void failSignup_tryAlreadySignedEmail() throws Exception {
        // given
        // when
        when(usersService.signUp(any(UsersRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.USER_ALREADY_EXISTS));
        // then
        String responseValue = mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isConflict()) // 예외 시 예상되는 상태 코드 설정
                .andExpect(result -> assertInstanceOf(CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("이미 가입한 회원입니다.", result.getResolvedException().getMessage()))
                .andDo(print()).toString();

        System.out.println("responseValue = " + responseValue);
    }

    @Test
    @DisplayName("로그인 성공")
    void test() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("로그인 실패 -> 이메일과 비밀번호가 매칭 X")
    void test1() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("로그인 실패 -> 입력 이메일과 매칭되는 계정이 없음")
    void test2() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("카카오 로그인 성공")
    void test3() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("카카오 로그인 실패 -> ?")
    void test4() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("구글 로그인 성공")
    void test5() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("구글 로그인 실패 -> ?")
    void test6() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("refresh 토큰으로 accessToken 생성 성공")
    void test7() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("refresh 토큰으로 accessToken 생성 실패 -> 해당 user없음")
    void test8() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("refresh 토큰으로 accessToken 생성 실패 -> token만료시간이 넘음")
    void test9() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원정보 get 성공")
    void test10() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원정보 get 실패 -> token만료")
    void test11() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원정보 get 실패 -> 해당 회원 정보 X")
    void test12() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원정보 post 성공")
    void test13() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원정보 post 실패 -> token만료")
    void test14() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원정보 post 실패 -> 해당 회원 정보 X")
    void test15() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원 delete 성공")
    void test16() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원 delete 실패 -> token만료")
    void test17() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원 delete 실패 -> 해당 회원 정보 X")
    void test18() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원 logout 성공")
    void test19() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원 logout 실패 -> token만료")
    void test20() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("회원 logout 실패 -> 해당 회원 정보 X")
    void test21() throws Exception {
        //given
        //when
        //then
    }
}