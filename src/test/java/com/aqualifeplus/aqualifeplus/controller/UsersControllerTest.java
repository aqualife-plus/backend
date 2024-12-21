package com.aqualifeplus.aqualifeplus.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.controller.AuthController;
import com.aqualifeplus.aqualifeplus.auth.service.AuthService;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorResponse;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.PasswordChangeDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupCheckDto;
import com.aqualifeplus.aqualifeplus.users.dto.SignupResponseDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.users.dto.SuccessDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersResponseDto;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import com.aqualifeplus.aqualifeplus.users.controller.UsersController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
        SignupResponseDto signUpResponseDto =
                new SignupResponseDto(true, usersRequestDto.getEmail());
        // when
        when(usersService.signUp(any(UsersRequestDto.class))).thenReturn(signUpResponseDto);
        // then
        String responseValue = mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals(
                objectMapper.writeValueAsString(signUpResponseDto),
                responseValue);
    }

    @Test
    @DisplayName("회원가입 실패 -> email blank valid error")
    void failSignup_DtoEmailBlankValidError() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email(" ")
                        .password("test password")
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "email");
                    assertEquals(dto.get("message"), "이메일을 입력해야 합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 -> email format valid error")
    void failSignup_DtoEmailFormatValidError() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("123")
                        .password("test password")
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "email");
                    assertEquals(dto.get("message"), "이메일 형식으로 입력해야 합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 -> email format valid error")
    void failSignup_DtoPasswordBlankValidError() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("1@1.com")
                        .password(" ")
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "password");
                    assertEquals(dto.get("message"), "비밀번호를 입력해야 합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 -> email format valid error")
    void failSignup_DtoNicknameBlankValidError() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("1@1.com")
                        .password("test password")
                        .nickname(" ")
                        .phoneNumber(null)
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "nickname");
                    assertEquals(dto.get("message"), "닉네임을 입력해야 합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 -> phoneNumber length valid error")
    void failSignup_DtoPhoneNumberLengthValidError() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("1@1.com")
                        .password("test password")
                        .nickname("test nickname")
                        .phoneNumber("010111")
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "phoneNumber");
                    assertEquals(dto.get("message"), "11자리의 숫자가 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 -> phoneNumber pattern valid error")
    void failSignup_DtoPhoneNumberPatternValidError() throws Exception {
        // given
        UsersRequestDto usersRequestDto =
                UsersRequestDto.builder()
                        .email("1@1.com")
                        .password("test password")
                        .nickname("test nickname")
                        .phoneNumber("0101111222k")
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "phoneNumber");
                    assertEquals(dto.get("message"), "11자리의 숫자가 필요합니다.");
                })
                .andDo(print());
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
    @DisplayName("회원가입 실패 -> save 메소드 error (기타 예외처리만 구현)")
    void failSignup_errorJPASaveMethod() throws Exception {
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
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(response.getErrorCode(), ErrorCode.UNEXPECTED_ERROR_IN_JPA);
                    assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
                    assertEquals(response.getMessage(), "JPA에서 예상치 못한 오류 발생");
                })
                .andDo(print());
    }


    @Test
    @DisplayName("이메일 중복체크 성공")
    void successCheckEmail() throws Exception {
        // given
        SignupCheckDto signupCheckDto = SignupCheckDto.builder()
                .email("t@t.com")
                .build();
        SuccessDto successDto = SuccessDto.builder()
                .success(false)
                .build();
        // when
        when(usersService.checkEmail(any(SignupCheckDto.class))).thenReturn(successDto);
        // then
        String responseValue = mockMvc.perform(post("/users/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupCheckDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnDto = objectMapper.readValue(responseValue, SuccessDto.class);

        assertFalse(returnDto.isSuccess());
    }

    @Test
    @DisplayName("이메일 중복체크 실패 -> 빈칸 valid error")
    void failCheckEmail_blankValidError() throws Exception {
        // given
        SignupCheckDto signupCheckDto =
                SignupCheckDto.builder()
                        .email(" ")
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupCheckDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "email");
                    assertEquals(dto.get("message"), "값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("이메일 중복체크 실패 -> 이메일 format valid error")
    void failCheckEmail_emailFormatValidError() throws Exception {
        // given
        SignupCheckDto signupCheckDto =
                SignupCheckDto.builder()
                        .email("123")
                        .build();
        // when
        // then
        mockMvc.perform(post("/users/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupCheckDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "email");
                    assertEquals(dto.get("message"), "email 형태여야 합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("이메일 중복체크 실패 -> 이메일 이미 존재")
    void failCheckEmail_alreadyExistEmail() throws Exception {
        // given
        SignupCheckDto signupCheckDto = SignupCheckDto.builder()
                .email("t@t.com")
                .build();
        SuccessDto successDto = SuccessDto.builder()
                .success(true)
                .build();
        // when
        when(usersService.checkEmail(any(SignupCheckDto.class))).thenReturn(successDto);
        // then
        String responseValue = mockMvc.perform(post("/users/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupCheckDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnDto = objectMapper.readValue(responseValue, SuccessDto.class);

        assertTrue(returnDto.isSuccess());
    }

    @Test
    @DisplayName("이메일 중복체크 실패 -> 이메일이 아니거나 값이 X")
    void failCheckEmail_NullOrNotMatchEmailFormat() throws Exception {
        // given
        SignupCheckDto signupCheckDto = SignupCheckDto.builder()
                .email(null)
                .build();

        // when
        // then
        mockMvc.perform(post("/users/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupCheckDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersResponseDto)))
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
    @DisplayName("회원정보 update 성공")
    @WithMockUser
    void successUpdateMyInfo() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        UsersResponseDto afterResponseDto = UsersResponseDto.builder()
                .nickname("update nick")
                .phoneNumber("01011112222")
                .build();
        SuccessDto successDto = SuccessDto.builder()
                .success(true)
                .build();
        //when
        when(usersService.updateMyInfo(any(UsersResponseDto.class))).thenReturn(successDto);
        //then
        String responseValue = mockMvc.perform(patch("/users/my-info")
                        .header("Authorization", accessTokenExample)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(afterResponseDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnDto = objectMapper.readValue(responseValue, SuccessDto.class);

        assertTrue(returnDto.isSuccess());
    }

    @Test
    @DisplayName("회원정보 update 실패 -> valid error")
    @WithMockUser
    void failUpdateMyInfo_dtoValidError() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        UsersResponseDto afterResponseDto = UsersResponseDto.builder()
                .nickname(" ")
                .phoneNumber("0101112222")
                .build();
        //when
        //then
        mockMvc.perform(patch("/users/my-info")
                        .header("Authorization", accessTokenExample)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(afterResponseDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andDo(print());
    }

    @Test
    @DisplayName("회원정보 update 실패  -> 해당 회원이 없다.")
    @WithMockUser
    void failCheckEmail_notMatchUsers() throws Exception {
        // given
        UsersResponseDto afterResponseDto =
                UsersResponseDto.builder()
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();

        // when
        when(usersService.updateMyInfo(any(UsersResponseDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // then
        mockMvc.perform(patch("/users/my-info")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(afterResponseDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_MEMBER);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "존재하지 않는 회원입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원정보 update 실패 -> update 메소드 error (기타 모든 예외)")
    @WithMockUser
    void failUpdateMyInfo_errorJPAUpdateMethod() throws Exception {
        // given
        UsersResponseDto usersResponseDto =
                UsersResponseDto.builder()
                        .nickname("test nickname")
                        .phoneNumber(null)
                        .build();
        // when
        when(usersService.updateMyInfo(any(UsersResponseDto.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(patch("/users/my-info")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersResponseDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(response.getErrorCode(), ErrorCode.UNEXPECTED_ERROR_IN_JPA);
                    assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
                    assertEquals(response.getMessage(), "JPA에서 예상치 못한 오류 발생");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    @WithMockUser
    void successChangePassword() throws Exception {
        // given
        PasswordChangeDto passwordChangeDto =
                PasswordChangeDto.builder()
                        .oldPassword("before pw")
                        .changePassword("after pw")
                        .build();
        SuccessDto successDto =
                SuccessDto.builder()
                        .success(true)
                        .build();
        // when
        when(usersService.changePassword(any(PasswordChangeDto.class)))
                .thenReturn(successDto);

        // then
        mockMvc.perform(patch("/users/change-password")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    SuccessDto dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            SuccessDto.class);

                    assertTrue(dto.isSuccess());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> 현재 비번 blank valid error")
    @WithMockUser
    void failChangePassword_beforePasswordValidError() throws Exception {
        // given
        PasswordChangeDto passwordChangeDto =
                PasswordChangeDto.builder()
                        .oldPassword(" ")
                        .changePassword("after pw")
                        .build();
        // when
        // then
        mockMvc.perform(patch("/users/change-password")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "oldPassword");
                    assertEquals(dto.get("message"), "지금 비밀번호를 입력해주세요.");
                });
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> 수정할 비번 blank valid error")
    @WithMockUser
    void failChangePassword_changePasswordValidError() throws Exception {
        // given
        PasswordChangeDto passwordChangeDto =
                PasswordChangeDto.builder()
                        .oldPassword("before pw")
                        .changePassword(" ")
                        .build();
        // when
        // then
        mockMvc.perform(patch("/users/change-password")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> dto =
                            objectMapper.readValue(
                                    result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                    new TypeReference<Map<String, String>>() {
                                    });

                    assertEquals(dto.get("errorKey"), "changePassword");
                    assertEquals(dto.get("message"), "바꿀 비밀번호를 입력해주세요.");
                });
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> token 만료")
    @WithMockUser
    void failChangePassword_invalidAccessToken() throws Exception {
        // given
        PasswordChangeDto passwordChangeDto =
                PasswordChangeDto.builder()
                        .oldPassword("before pw")
                        .changePassword("after pw")
                        .build();
        // when
        when(usersService.changePassword(any(PasswordChangeDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));

        // then
        mockMvc.perform(patch("/users/change-password")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.EXPIRED_TOKEN);
                    assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED);
                    assertEquals(response.getMessage(), "만료된 토큰입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> 일치하는 유저가 없음")
    @WithMockUser
    void failChangePassword_notMatchUsers() throws Exception {
        // given
        PasswordChangeDto passwordChangeDto =
                PasswordChangeDto.builder()
                        .oldPassword("before pw")
                        .changePassword("after pw")
                        .build();
        // when
        when(usersService.changePassword(any(PasswordChangeDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // then
        mockMvc.perform(patch("/users/change-password")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_MEMBER);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "존재하지 않는 회원입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> 이전 비밀번호와 유저의 비밀번호가 일치하지 않음")
    @WithMockUser
    void failChangePassword_notMatchPasswordInputPassword() throws Exception {
        // given
        PasswordChangeDto passwordChangeDto =
                PasswordChangeDto.builder()
                        .oldPassword("before pw")
                        .changePassword("after pw")
                        .build();
        SuccessDto successDto =
                SuccessDto.builder()
                        .success(false)
                        .build();
        // when
        when(usersService.changePassword(any(PasswordChangeDto.class)))
                .thenReturn(successDto);

        // then
        mockMvc.perform(patch("/users/change-password")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    SuccessDto dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            SuccessDto.class);

                    assertFalse(dto.isSuccess());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원 delete 성공")
    @WithMockUser
    void successDeleteUsers() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        SuccessDto successDto = SuccessDto.builder()
                .success(true)
                .build();
        //when
        when(usersService.deleteUser()).thenReturn(successDto);
        //then
        String responseValue = mockMvc.perform(delete("/users/withdrawal")
                        .header("Authorization", accessTokenExample)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnDto = objectMapper.readValue(responseValue, SuccessDto.class);

        assertTrue(returnDto.isSuccess());
    }

    @Test
    @DisplayName("회원 delete 실패 -> token 만료")
    @WithMockUser
    void failDeleteUsers_invalidAccessToken() throws Exception {
        // given
        // when
        when(usersService.deleteUser())
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));

        // then
        mockMvc.perform(delete("/users/withdrawal")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.EXPIRED_TOKEN);
                    assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED);
                    assertEquals(response.getMessage(), "만료된 토큰입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원 delete 실패 -> 일치하는 유저가 없음")
    @WithMockUser
    void failDeleteUsers_notMatchUsers() throws Exception {
        // given
        // when
        when(usersService.deleteUser())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // then
        mockMvc.perform(delete("/users/withdrawal")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_MEMBER);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "존재하지 않는 회원입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원 delete 실패 -> delete method error")
    @WithMockUser
    void failDeleteUsers_errorJPADeleteMethod() throws Exception {
        // given
        // when
        when(usersService.deleteUser())
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(delete("/users/withdrawal")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);
                    assertEquals(response.getErrorCode(), ErrorCode.UNEXPECTED_ERROR_IN_JPA);
                    assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
                    assertEquals(response.getMessage(), "JPA에서 예상치 못한 오류 발생");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원 logout 성공")
    @WithMockUser
    void successLogout() throws Exception {
        //given
        String accessTokenExample = "Bearer accessTokenExample";
        SuccessDto successDto = SuccessDto.builder()
                .success(true)
                .build();
        //when
        when(usersService.logout()).thenReturn(successDto);
        //then
        String responseValue = mockMvc.perform(post("/users/logout")
                        .header("Authorization", accessTokenExample)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnDto = objectMapper.readValue(responseValue, SuccessDto.class);

        assertTrue(returnDto.isSuccess());
    }

    @Test
    @DisplayName("회원 logout 실패 -> token 만료")
    @WithMockUser
    void failLogout_invalidAccessToken() throws Exception {
        // given
        // when
        when(usersService.logout())
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));

        // then
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.EXPIRED_TOKEN);
                    assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED);
                    assertEquals(response.getMessage(), "만료된 토큰입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원 logout 실패 -> 일치하는 유저가 없음")
    @WithMockUser
    void failLogout_notMatchUsers() throws Exception {
        // given
        // when
        when(usersService.logout())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // then
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_MEMBER);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "존재하지 않는 회원입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("회원 logout 실패 -> redis delete error")
    @WithMockUser
    void failLogout_errorRedisDeleteError() throws Exception {
        // given
        // when
        when(usersService.logout())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS));
        // then
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_KEY_IN_REDIS);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 키가 존재하지 않습니다.");})
                .andDo(print());
    }
}