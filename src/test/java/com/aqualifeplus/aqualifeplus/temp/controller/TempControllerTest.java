package com.aqualifeplus.aqualifeplus.temp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorResponse;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitRequestDto;
import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitResponseDto;
import com.aqualifeplus.aqualifeplus.temp.service.TempService;
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
@WebMvcTest(TempController.class)
class TempControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TempService tempService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("temp 업데이트 성공")
    @WithMockUser
    void successUpdateTempLimit() throws Exception {
        //given
        UpdateTempLimitRequestDto updateTempLimitRequestDto =
                UpdateTempLimitRequestDto.builder()
                        .tempStay(0.0)
                        .build();
        UpdateTempLimitResponseDto updateTempLimitResponseDto =
                UpdateTempLimitResponseDto.builder()
                        .success(true)
                        .tempStay(0.0)
                        .build();
        //when
        when(tempService.updateTempLimit(any(UpdateTempLimitRequestDto.class)))
                .thenReturn(updateTempLimitResponseDto);
        //then
        mockMvc.perform(put("/temp")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTempLimitRequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    UpdateTempLimitResponseDto responseDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            UpdateTempLimitResponseDto.class);

                    assertEquals(responseDto.isSuccess(),
                            updateTempLimitResponseDto.isSuccess());
                    assertEquals(responseDto.getTempStay(),
                            updateTempLimitResponseDto.getTempStay());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("temp 업데이트 실패 - firebase accessToken get error")
    @WithMockUser
    void failUpdateTempLimit_errorGetFirebaseAccessToken() throws Exception{
        //given
        UpdateTempLimitRequestDto updateTempLimitRequestDto =
                UpdateTempLimitRequestDto.builder()
                        .tempStay(0.0)
                        .build();
        // when
        when(tempService.updateTempLimit(any(UpdateTempLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.FAIL_FIREBASE_CREDENTIALS));
        // then
        mockMvc.perform(put("/temp")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTempLimitRequestDto)))
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
    @DisplayName("temp update fail - accessToken 만료")
    @WithMockUser
    void failUpdateTempLimit_expiredAccessToken() throws Exception {
        // given
        UpdateTempLimitRequestDto updateTempLimitRequestDto =
                UpdateTempLimitRequestDto.builder()
                        .tempStay(0.0)
                        .build();

        // when
        when(tempService.updateTempLimit(any(UpdateTempLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(put("/temp")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTempLimitRequestDto)))
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
    @DisplayName("temp update fail - invalid Token")
    @WithMockUser
    void failUpdateTempLimit_invalidAccessToken() throws Exception {
        // given
        UpdateTempLimitRequestDto updateTempLimitRequestDto =
                UpdateTempLimitRequestDto.builder()
                        .tempStay(0.0)
                        .build();
        // when
        when(tempService.updateTempLimit(any(UpdateTempLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(put("/temp")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTempLimitRequestDto)))
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
    @DisplayName("temp update fail - 일치하는 유저 X")
    @WithMockUser
    void failUpdateTempLimit_notFoundUsers() throws Exception {
        // given
        UpdateTempLimitRequestDto updateTempLimitRequestDto =
                UpdateTempLimitRequestDto.builder()
                        .tempStay(0.0)
                        .build();
        // when
        when(tempService.updateTempLimit(any(UpdateTempLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(put("/temp")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTempLimitRequestDto)))
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
    @DisplayName("temp update fail - 어항 token null")
    @WithMockUser
    void failUpdateTempLimit_notFoundFishbowl() throws Exception {
        // given
        UpdateTempLimitRequestDto updateTempLimitRequestDto =
                UpdateTempLimitRequestDto.builder()
                        .tempStay(0.0)
                        .build();
        // when
        when(tempService.updateTempLimit(any(UpdateTempLimitRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(put("/temp")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTempLimitRequestDto)))
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
    @DisplayName("temp update fail - firebase update error")
    @WithMockUser
    void failUpdateTempLimit_errorUpdateInFirebase() throws Exception {
        // TODO : firebase
        // given
        // when
        // then
    }
}