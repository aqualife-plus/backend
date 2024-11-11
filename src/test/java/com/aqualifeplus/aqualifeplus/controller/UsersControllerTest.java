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
}