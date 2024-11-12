package com.aqualifeplus.aqualifeplus.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.dto.LoginDto;
import com.aqualifeplus.aqualifeplus.dto.TokenDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.exception.CustomException;
import com.aqualifeplus.aqualifeplus.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
@WebMvcTest(UsersController.class)
class UsersControllerTest {
    @BeforeEach
    void setup() {
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

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

    @Test
    @DisplayName("회원가입 성공")
    void successSignup() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("1@1.com")
                        .password("test password")
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();
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
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("1@1.com")
                        .password("test password")
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();
        // when
        when(usersService.signUp(any(UsersRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.USER_ALREADY_EXISTS));
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isConflict()) // 예외 시 예상되는 상태 코드 설정
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "이미 가입한 회원입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 성공")
    void successLogin() throws Exception {
        //given
        LoginDto loginDto =
                new LoginDto("1@1.com", "testPassword");
        TokenDto tokenDto =
                new TokenDto(
                        "accessTokenValue",
                        "userTokenValue",
                        "refreshTokenValue");
        //when
        when(usersService.login(any(LoginDto.class))).thenReturn(tokenDto);
        //then
        String responseValue = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk()) // 예외 시 예상되는 상태 코드 설정
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        Map<String, String> responseMap =
                objectMapper.readValue(responseValue, new TypeReference<>() {});

        assertEquals("Bearer accessTokenValue", responseMap.get("accessToken"));
        assertEquals("userTokenValue", responseMap.get("userToken"));
        assertEquals("Bearer refreshTokenValue", responseMap.get("refreshToken"));
    }

    @Test
    @DisplayName("로그인 실패 -> 이메일과 비밀번호가 매칭 X")
    void failLogin_notMatchEmailAndPassword() throws Exception {
        //given
        LoginDto loginDto =
                new LoginDto("1@1.com", "testPassword");
        //when
        when(usersService.login(any(LoginDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_MATCH_PASSWORD_OR_EMAIL));
        //then
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 실패 -> 입력 이메일과 매칭되는 계정이 없음")
    void failLogin_invalidUserMatchEmail() throws Exception {
        //given
        LoginDto loginDto =
                new LoginDto("1@1.com", "testPassword");
        //when
        when(usersService.login(any(LoginDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        //then
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
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
        when(usersService.refreshAccessToken()).thenReturn(newAccessTokenExample);
        //then
        String responseValue = mockMvc.perform(post("/users/refresh-token")
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
        when(usersService.refreshAccessToken())
                .thenThrow(new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
        //then
        mockMvc.perform(post("/users/refresh-token")
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
    @DisplayName("회원정보 get 성공")
    @WithMockUser
    void successGetMyInfo() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        UsersResponseDto usersResponseDto = UsersResponseDto.builder()
                .nickname("test nick")
                .phoneNumber(null)
                .build();
        //when
        when(usersService.getMyInfo()).thenReturn(usersResponseDto);
        //then
        String responseValue = mockMvc.perform(get("/users/my-info")
                        .header("Authorization", accessTokenExample) // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        UsersResponseDto responseDtoMapper =
                objectMapper.readValue(responseValue, UsersResponseDto.class);

        assertEquals(usersResponseDto.getNickname(), responseDtoMapper.getNickname());
        assertEquals(usersResponseDto.getPhoneNumber(), responseDtoMapper.getPhoneNumber());
    }

    @Test
    @DisplayName("회원정보 get 실패 -> token 만료")
    @WithMockUser
    void failGetMyInfo_invalidAccessToken() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        //when
        when(usersService.getMyInfo())
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        //then
        mockMvc.perform(get("/users/my-info")
                        .header("Authorization", accessTokenExample)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "만료된 토큰입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원정보 get 실패 -> 해당 회원 정보 X")
    @WithMockUser
    void failGetMyInfo_invalidUser() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        //when
        when(usersService.getMyInfo())
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        //then
        mockMvc.perform(get("/users/my-info")
                        .header("Authorization", accessTokenExample)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "잘못된 인증정보입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
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