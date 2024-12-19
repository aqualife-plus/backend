package com.aqualifeplus.aqualifeplus.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
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
import org.springframework.core.MethodParameter;
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
    @DisplayName("회원가입 실패 -> valid error")
    void failSignup_DtoValidError() throws Exception {
        // TODO : Valid 더 추가하기
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
    @DisplayName("회원가입 실패 -> save 메소드 error")
    void failSignup_errorJPASaveMethod() throws Exception {
        // TODO : save메소드 에러 처리 후 구현
        // given
        // when
        // then
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
    @DisplayName("이메일 중복체크 실패 -> valid error")
    void failCheckEmail_validError() {
        // TODO
        // given
        // when
        // then
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
    void failUpdateMyInfo_DtoValidError() throws Exception {
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
    @DisplayName("회원정보 update 실패 -> update 메소드 error")
    @WithMockUser
    void failUpdateMyInfo_errorJPAUpdateMethod() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    @WithMockUser
    void successChangePassword() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> valid errpr")
    @WithMockUser
    void failChangePassword_validError() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> token 만료")
    @WithMockUser
    void failChangePassword_invalidAccessToken() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> 일치하는 유저가 없음")
    @WithMockUser
    void failChangePassword_notMatchUsers() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("비밀번호 수정 실패 -> 이전 비밀번호와 유저의 비밀번호가 일치하지 않음")
    @WithMockUser
    void failChangePassword_notMatchPasswordInputPassword() {
        // given
        // when
        // then
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
    void failDeleteUsers_invalidAccessToken() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("회원 delete 실패 -> 일치하는 유저가 없음")
    void failDeleteUsers_notMatchUsers() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("회원 delete 실패 -> delete method error")
    void failDeleteUsers_errorJPADeleteMethod() {
        // given
        // when
        // then
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
    void failLogout_invalidAccessToken() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("회원 logout 실패 -> 일치하는 유저가 없음")
    void failLogout_notMatchUsers() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("회원 logout 실패 -> redis delete error")
    void failLogout_errorRedisDeleteError() {
        // given
        // when
        // then
    }
}