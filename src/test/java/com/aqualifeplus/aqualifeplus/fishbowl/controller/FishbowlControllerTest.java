package com.aqualifeplus.aqualifeplus.fishbowl.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.controller.AuthController;
import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.auth.service.AuthService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.ConnectDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.FishbowlNameDto;
import com.aqualifeplus.aqualifeplus.fishbowl.service.FishbowlService;
import com.aqualifeplus.aqualifeplus.users.dto.SuccessDto;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Import(SecurityConfig.class)
@WebMvcTest(FishbowlController.class)
class FishbowlControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FishbowlService fishbowlService;
    @MockBean
    private UsersService usersService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("어항 연결 성공")
    @WithMockUser
    void successConnect() throws Exception {
        // given
        ConnectDto connectDto = ConnectDto.builder()
                .success(true)
                .fishbowlId("test fishbowl")
                .build();

        // when
        when(fishbowlService.connect()).thenReturn(connectDto);
        // then
        String responseValue = mockMvc.perform(get("/fishbowl/connect")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(connectDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        ConnectDto returnConnectDto =
                objectMapper.readValue(responseValue, ConnectDto.class);

        assertTrue(returnConnectDto.isSuccess());
    }

    @Test
    @DisplayName("어항 연결 실패 - 존재하지 않는 유저")
    @WithMockUser
    void failConnect_notFoundUsers() throws Exception {
        // given
        // when
        when(fishbowlService.connect()).
                thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(get("/fishbowl/connect")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(any(ConnectDto.class))))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "존재하지 않는 회원입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 연결 실패 - 올바르지 않은 JWT")
    @WithMockUser
    void failConnect_useInvalidAccessToken() throws Exception {
        // given
        // when
        when(fishbowlService.connect()).
                thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(get("/fishbowl/connect")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(any(ConnectDto.class))))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "잘못된 인증정보입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 연결 실패 - firebase 어항 조회 실패")
    @WithMockUser
    void failConnect_errorGetFishbowlData() throws Exception {
        // TODO : firebase 에러 처리 후 구현
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 연결 실패 - firebase 어항 삭제 실패")
    @WithMockUser
    void failConnect_errorDeleteFishbowlData() throws Exception {
        // TODO : firebase 에러 처리 후 구현
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 연결 실패 - JPA 삭제 메소드 error")
    @WithMockUser
    void failConnect_errorJPADeleteMethod() throws Exception {
        // TODO
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 연결 실패 - redis에 데이터 저장 실패")
    @WithMockUser
    void failConnect_errorSaveFishbowlId() throws Exception {
        // TODO : Redis 에러 처리 후 구현
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 연결 실패 - redis에 데이터 ,삭제 실패")
    @WithMockUser
    void failConnect_errorDeleteFishbowlId() throws Exception {
        // TODO : Redis 에러 처리 후 구현
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 이름 초기설정 성공")
    @WithMockUser
    void successCreateFishbowlName() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name("test name")
                        .build();

        SuccessDto successDto =
                SuccessDto.builder()
                        .success(true)
                        .build();

        // when
        when(fishbowlService
                .createFishbowlName(any(String.class)))
                .thenReturn(successDto);
        // then
        String responseValue = mockMvc.perform(post("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnSuccessDto =
                objectMapper.readValue(responseValue, SuccessDto.class);

        assertTrue(returnSuccessDto.isSuccess());
    }

    @Test
    @DisplayName("어항 이름 초기설정 실패 - valid error")
    @WithMockUser
    void failCreateFishbowlName_blankName() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name(" ")
                        .build();
        // when
        // then
        mockMvc.perform(post("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
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
                    assertEquals(dto.get("errorKey"), "name");
                    assertEquals(dto.get("message"), "이름이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("어항 이름 초기설정 실패 - 존재하지 않는 유저")
    @WithMockUser
    void failCreateFishbowlName_notFoundUsers() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name("test name")
                        .build();
        // when
        when(fishbowlService.createFishbowlName(any(String.class))).
                thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(post("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "존재하지 않는 회원입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 이름 초기설정 실패 - 올바르지 않은 JWT")
    @WithMockUser
    void failCreateFishbowlName_useInvalidAccessToken() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name("test name")
                        .build();
        // when
        when(fishbowlService.createFishbowlName(any(String.class))).
                thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(post("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "잘못된 인증정보입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 이름 초기설정 실패 - redis 내부에 존재하지 않는 fishbowlId")
    @WithMockUser
    void failCreateFishbowlName_notFoundFishbowlIdInRedis() throws Exception {
        // TODO : Redis 에러처리 후 작업하기
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 이름 초기설정 실패 - firebase 데이터 수정 작업 실패")
    @WithMockUser
    void failCreateFishbowlName_errorFirebaseUpdateName() throws Exception {
        // TODO : Firebase 에러처리 후 작업하기
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 이름 변경 성공")
    @WithMockUser
    void successUpdateFishbowlName() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name("test update name")
                        .build();

        SuccessDto successDto =
                SuccessDto.builder()
                        .success(true)
                        .build();

        // when
        when(fishbowlService
                .updateFishbowlName(any(String.class)))
                .thenReturn(successDto);
        // then
        String responseValue = mockMvc.perform(patch("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        SuccessDto returnSuccessDto =
                objectMapper.readValue(responseValue, SuccessDto.class);

        assertTrue(returnSuccessDto.isSuccess());
    }

    @Test
    @DisplayName("어항 이름 변경 실패 - valid error")
    @WithMockUser
    void failUpdateFishbowlName_blankName() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name(" ")
                        .build();
        // when
        // then
        mockMvc.perform(patch("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 이름 변경 실패 - 존재하지 않는 유저")
    @WithMockUser
    void failUpdateFishbowlName_notFoundUsers() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name("test name")
                        .build();
        // when
        when(fishbowlService.updateFishbowlName(any(String.class))).
                thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(patch("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "존재하지 않는 회원입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 이름 변경 실패 - 올바르지 않은 JWT")
    @WithMockUser
    void failUpdateFishbowlName_useInvalidAccessToken() throws Exception {
        // given
        FishbowlNameDto fishbowlNameDto =
                FishbowlNameDto.builder()
                        .name("test name")
                        .build();
        // when
        when(fishbowlService.updateFishbowlName(any(String.class))).
                thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(patch("/fishbowl/name")
                        .header("Authorization", "Bearer accessToken") // 헤더에 refreshToken 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fishbowlNameDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(
                        "잘못된 인증정보입니다.",
                        result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("어항 이름 변경 실패 - redis 내부에 존재하지 않는 fishbowlId")
    @WithMockUser
    void failUpdateFishbowlName_notFoundFishbowlIdInRedis() throws Exception {
        // TODO : Redis 에러처리 후 작업하기
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 이름 변경 실패 - firebase 데이터 수정 작업 실패")
    @WithMockUser
    void failUpdateFishbowlName_errorFirebaseUpdateName() throws Exception {
        // TODO : Firebase 에러처리 후 작업하기
        // given
        // when
        // then
    }


    @Test
    void deleteFishbowl() {
        // TODO : 여기부터 시작
    }
}