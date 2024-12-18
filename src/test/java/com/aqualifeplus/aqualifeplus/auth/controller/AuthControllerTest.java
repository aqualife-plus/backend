package com.aqualifeplus.aqualifeplus.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.auth.service.AuthService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.users.controller.UsersController;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;
    @MockBean
    private UsersService usersService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("로그인 성공")
    void successLogin() throws Exception {
        //given
        LoginRequestDto loginRequestDto =
                new LoginRequestDto("1@1.com", "testPassword");
        TokenResponseDto tokenDto =
                new TokenResponseDto(
                        "accessTokenValue",
                        "refreshTokenValue");
        //when
        when(authService.login(any(LoginRequestDto.class))).thenReturn(tokenDto);
        //then
        String responseValue = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk()) // 예외 시 예상되는 상태 코드 설정
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        Map<String, String> responseMap =
                objectMapper.readValue(responseValue, new TypeReference<>() {
                });

        assertEquals("Bearer accessTokenValue", responseMap.get("accessToken"));
        assertEquals("Bearer refreshTokenValue", responseMap.get("refreshToken"));
    }

    @Test
    @DisplayName("로그인 실패 -> 이메일과 비밀번호가 매칭 X")
    void failLogin_notMatchEmailAndPassword() throws Exception {
        //given
        LoginRequestDto loginDto =
                new LoginRequestDto("1@1.com", "testPassword");
        //when
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_MATCH_PASSWORD_OR_EMAIL));
        //then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "이메일 or 비밀번호가 맞지 않습니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 -> 입력 이메일과 매칭되는 계정이 없음")
    void failLogin_invalidUserMatchEmail() throws Exception {
        //given
        LoginRequestDto loginRequestDto =
                new LoginRequestDto("1@1.com", "testPassword");
        //when
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        //then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "존재하지 않는 회원입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("refresh 토큰으로 accessToken 생성 성공")
    @WithMockUser
    void successCreateNewAccessToken_useRefreshToken() throws Exception {
        //given
        String refreshTokenExample = "Bearer refreshTokenExample";
        String newAccessTokenExample = "Bearer newAccessTokenExample";
        //when
        when(authService.refreshAccessToken()).thenReturn(newAccessTokenExample);
        //then
        String responseValue = mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", refreshTokenExample) // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals(newAccessTokenExample, responseValue);
    }

    @Test
    @DisplayName("refresh 토큰으로 accessToken 생성 실패 -> token이 없음")
    @WithMockUser
    void failCreateNewAccessToken_invalidRefreshToken() throws Exception {
        //given
        String refreshTokenExample = "Bearer refreshTokenExample";
        //when
        when(authService.refreshAccessToken())
                .thenThrow(new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
        //then
        mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", refreshTokenExample)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "존재하지 않는 토큰입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    void login() {
    }

    @Test
    void androidToken() {
    }

    @Test
    void refreshAccessToken() {
    }
}