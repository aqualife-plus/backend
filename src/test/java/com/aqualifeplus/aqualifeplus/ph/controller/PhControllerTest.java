package com.aqualifeplus.aqualifeplus.ph.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.co2.service.Co2Service;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorResponse;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.filter.controller.FilterController;
import com.aqualifeplus.aqualifeplus.filter.service.FilterService;
import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitRequestDto;
import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitResponseDto;
import com.aqualifeplus.aqualifeplus.ph.service.PhService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Import(SecurityConfig.class)
@WebMvcTest(PhController.class)
class PhControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PhService phService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("ph 업데이트 성공")
    @WithMockUser
    void successUpdatePhLimit() throws Exception {
        //given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();
        UpdatePhLimitResponseDto updatePhLimitResponseDto =
                UpdatePhLimitResponseDto.builder()
                        .success(true)
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();
        //when
        when(phService.updatePhLimit(any(UpdatePhLimitRequestDto.class)))
                .thenReturn(updatePhLimitResponseDto);
        //then
        mockMvc.perform(put("/ph")
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    UpdatePhLimitResponseDto responseDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            UpdatePhLimitResponseDto.class);

                    assertEquals(responseDto.isSuccess(),
                            updatePhLimitResponseDto.isSuccess());
                    assertEquals(responseDto.getWarningMaxPh(),
                            updatePhLimitResponseDto.getWarningMaxPh());
                    assertEquals(responseDto.getWarningMinPh(),
                            updatePhLimitResponseDto.getWarningMinPh());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("ph 업데이트 실패 - firebase accessToken get error")
    @WithMockUser
    void failUpdatePhLimit_errorGetFirebaseAccessToken() throws Exception{
        //given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();
        // when
        when(phService.updatePhLimit(any(UpdatePhLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.FAIL_FIREBASE_CREDENTIALS));
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.FAIL_FIREBASE_CREDENTIALS);
                    assertEquals(response.getStatus(), HttpStatus.NO_CONTENT);
                    assertEquals(response.getMessage(), "Firebase 자격 증명에서 액세스 토큰을 가져오지 못했습니다");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - warningMax null error")
    @WithMockUser
    void failUpdatePhLimit_errorWarningMaxPhNullValid() throws Exception {
        // given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMinPh(0.0)
                        .build();

        // when
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> errorMap = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {
                            });

                    assertEquals(errorMap.get("errorKey"), "warningMaxPh");
                    assertEquals(errorMap.get("message"), "ph 최대치값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - warningMin null error")
    @WithMockUser
    void failUpdatePhLimit_errorWarningMinPhNullValid() throws Exception {
        // given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(0.0)
                        .build();

        // when
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    Map<String, String> errorMap = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {
                            });

                    assertEquals(errorMap.get("errorKey"), "warningMinPh");
                    assertEquals(errorMap.get("message"), "ph 최저치값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - accessToken 만료")
    @WithMockUser
    void failUpdatePhLimit_expiredAccessToken() throws Exception {
        // given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();

        // when
        when(phService.updatePhLimit(any(UpdatePhLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
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
    @DisplayName("환수 가져오기 fail - invalid Token")
    @WithMockUser
    void failUpdatePhLimit_invalidAccessToken() throws Exception {
        // given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();
        // when
        when(phService.updatePhLimit(any(UpdatePhLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.INVALID_CREDENTIALS);
                    assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED);
                    assertEquals(response.getMessage(), "잘못된 인증정보입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - 일치하는 유저 X")
    @WithMockUser
    void failUpdatePhLimit_notFoundUsers() throws Exception {
        // given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();
        // when
        when(phService.updatePhLimit(any(UpdatePhLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
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
    @DisplayName("환수 가져오기 fail - 어항 token null")
    @WithMockUser
    void failUpdatePhLimit_notFoundFishbowl() throws Exception {
        // given
        UpdatePhLimitRequestDto updatePhLimitRequestDto =
                UpdatePhLimitRequestDto.builder()
                        .warningMaxPh(10.0)
                        .warningMinPh(0.0)
                        .build();
        // when
        when(phService.updatePhLimit(any(UpdatePhLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(put("/ph")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhLimitRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 유저가 만든 어항이 존재하지 않습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - firebase update error")
    @WithMockUser
    void failUpdatePhLimit_errorUpdateInFirebase() throws Exception {
        // TODO : firebase
        // given
        // when
        // then
    }
}