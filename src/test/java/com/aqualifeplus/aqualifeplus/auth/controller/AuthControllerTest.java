package com.aqualifeplus.aqualifeplus.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.dto.AndroidRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.auth.service.AuthService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorResponse;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.FishbowlNameDto;
import com.aqualifeplus.aqualifeplus.users.controller.UsersController;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
    @DisplayName("로그인 실패 -> 이메일 valid error")
    void failLogin_emailValidError() throws Exception {
        // given
        LoginRequestDto loginRequestDto =
                new LoginRequestDto(" ", "testPassword");
        // when
        // then
        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {
                            });
                    assertEquals(dto.get("errorKey"), "email");
                    assertEquals(dto.get("message"), "이메일을 입력해야합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 -> id에 이메일 형식이 아닐 때 에러")
    void failLogin_notMatchEmailFormat() throws Exception {
        // given
        LoginRequestDto loginRequestDto =
                new LoginRequestDto("123", "testPassword");
        // when
        // then
        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {
                            });
                    assertEquals(dto.get("errorKey"), "email");
                    assertEquals(dto.get("message"), "이메일 형식을 입력해야 합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 -> 비밀번호 valid error")
    void failLogin_passwordValidError() throws Exception {
        // given
        LoginRequestDto loginRequestDto =
                new LoginRequestDto("1@1.com", " ");
        // when
        // then
        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {
                            });
                    assertEquals(dto.get("errorKey"), "password");
                    assertEquals(dto.get("message"), "비밀번호를 입력해야 합니다.");
                })
                .andDo(print());
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
    @DisplayName("로그인 실패 -> redis에 refresh 저장 실패 (DISCONNECTED_REDIS만 하겠음)")
    void failLogin_notSaveRefreshTokenInRedis() throws Exception {
        // TODO :  Redis 에러 처리 후 구현
        //given
        LoginRequestDto loginRequestDto =
                new LoginRequestDto("1@1.com", "testPassword");
        //when
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DISCONNECTED_REDIS));
        //then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "Redis와 연결이 끊겼습니다.",
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
    @DisplayName("refresh 토큰으로 accessToken 생성 실패 -> redis에 refresh 토큰 X")
    void failCreateNewAccessToken_notFoundRefreshTokenInRedis() throws Exception {
        //given
        String refreshTokenExample = "Bearer refreshTokenExample";
        //when
        when(authService.refreshAccessToken())
                .thenThrow(new CustomException(ErrorCode.VAlUE_NOT_FOUND_IN_REDIS));
        //then
        mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", refreshTokenExample)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "해당 키에 데이터가 존재하지 않습니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("refresh 토큰으로 accessToken 생성 실패 -> redis의 refresh 토큰과 받은 refresh 토큰이 다름")
    void failCreateNewAccessToken_notMatchRefreshTokenInRedis() throws Exception{
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
    @DisplayName("휴대폰 fcm token 저장 성공")
    @WithMockUser
    void successAndroidToken() throws Exception{
        //given
        AndroidRequestDto androidRequestDto =
                new AndroidRequestDto("token test");
        SuccessDto successDto = new SuccessDto(true);

        //when
        when(authService.setAndroidToken(any(AndroidRequestDto.class)))
                .thenReturn(successDto);
        //then
        mockMvc.perform(post("/auth/android-token")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(androidRequestDto)))
                .andExpect(status().isOk())
                .andExpect(result ->  {
                    SuccessDto dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    SuccessDto.class);

                    assertTrue(dto.isSuccess());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("휴대폰 fcm token 저장 실패 -> valid error")
    @WithMockUser
    void failAndroidToken_validError() throws Exception{
        //given
        AndroidRequestDto androidRequestDto =
                new AndroidRequestDto(" ");

        //when
        //then
        mockMvc.perform(post("/auth/android-token")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(androidRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {
                            });
                    assertEquals(dto.get("errorKey"), "token");
                    assertEquals(dto.get("message"), "토큰을 넣어주세요.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("휴대폰 fcm token 저장 실패 -> accessToken과 일치하는 users가 없음")
    @WithMockUser
    void failAndroidToken_notFoundUsersInAccessToken() throws Exception{
        //given
        AndroidRequestDto androidRequestDto =
                new AndroidRequestDto("token test");

        //when
        when(authService.setAndroidToken(any(AndroidRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        //then
        mockMvc.perform(post("/auth/android-token")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(androidRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse errorResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(errorResponse.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(errorResponse.getMessage(), "존재하지 않는 회원입니다.");
                    assertEquals(errorResponse.getErrorCode(), ErrorCode.NOT_FOUND_MEMBER);
                })
                .andDo(print());
    }

    @Test
    @DisplayName("휴대폰 fcm token 저장 실패 -> redis에 저장 실패 (RedisConnectionFailureException만 Test)")
    @WithMockUser
    void failAndroidToken_errorSaveFCMTokenInRedis() throws Exception {
        //given
        AndroidRequestDto androidRequestDto =
                new AndroidRequestDto("token test");

        //when
        when(authService.setAndroidToken(any(AndroidRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DISCONNECTED_REDIS));

        //then
        mockMvc.perform(post("/auth/android-token")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(androidRequestDto)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse errorResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(errorResponse.getStatus(), HttpStatus.SERVICE_UNAVAILABLE);
                    assertEquals(errorResponse.getMessage(), "Redis와 연결이 끊겼습니다.");
                    assertEquals(errorResponse.getErrorCode(), ErrorCode.DISCONNECTED_REDIS);
                })
                .andDo(print());
    }

    @Test
    @DisplayName("휴대폰 fcm token 저장 실패 -> firebase에서 get 실패")
    void failAndroidToken_errorGetDataInFirebase() {
        //TODO : firebase
        //given
        //when
        //then
    }

    @Test
    @DisplayName("휴대폰 fcm token 저장 실패 -> firebase get한 값이 null일 때")
    void failAndroidToken_getFirebaseDataIsNull() {
        //TODO : firebase
        //given
        //when
        //then
    }

    @Test
    @DisplayName("휴대폰 fcm token 저장 실패 -> redis에 fcm token get data X")
    @WithMockUser
    void failAndroidToken_errorGetFCMDataInRedis() throws Exception{
        //given
        AndroidRequestDto androidRequestDto =
                new AndroidRequestDto("token test");

        //when
        when(authService.setAndroidToken(any(AndroidRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.VAlUE_NOT_FOUND_IN_REDIS));

        //then
        mockMvc.perform(post("/auth/android-token")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(androidRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse errorResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(errorResponse.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(errorResponse.getMessage(), "해당 키에 데이터가 존재하지 않습니다.");
                    assertEquals(errorResponse.getErrorCode(), ErrorCode.VAlUE_NOT_FOUND_IN_REDIS);
                })
                .andDo(print());
    }
}