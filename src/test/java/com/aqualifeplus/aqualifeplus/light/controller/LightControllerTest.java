package com.aqualifeplus.aqualifeplus.light.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.aqualifeplus.aqualifeplus.light.dto.DeleteLightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightRequestDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightResponseDto;
import com.aqualifeplus.aqualifeplus.light.dto.LightSuccessDto;
import com.aqualifeplus.aqualifeplus.light.entity.Light;
import com.aqualifeplus.aqualifeplus.light.service.LightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;
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
@WebMvcTest(LightController.class)
class LightControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LightService lightService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("Light 예약 리스트 가져오기 성공")
    @WithMockUser
    void successGetLightReserveList() throws Exception {
        //given
        LightResponseDto dto1 =
                LightResponseDto.builder()
                        .id(1L)
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        LightResponseDto dto2 =
                LightResponseDto.builder()
                        .id(2L)
                        .lightStartTime(LocalTime.now().minusMinutes(1))
                        .lightEndTime(LocalTime.now())
                        .lightReserveState(false)
                        .build();
        List<LightResponseDto> dtoList = List.of(dto1, dto2);
        //when
        when(lightService.lightReserveList())
                .thenReturn(dtoList);
        //then
        mockMvc.perform(get("/light/reserve-list")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<LightResponseDto> responseDtoList = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<List<LightResponseDto>>() {
                            });

                    assertEquals(responseDtoList.getFirst().getId(), dto1.getId());
                    assertEquals(responseDtoList.getFirst().getLightStartTime(), dto1.getLightStartTime());
                    assertEquals(responseDtoList.getFirst().getLightEndTime(), dto1.getLightEndTime());
                    assertEquals(responseDtoList.getFirst().isLightReserveState(), dto1.isLightReserveState());
                    assertEquals(responseDtoList.getLast().getId(), dto2.getId());
                    assertEquals(responseDtoList.getLast().getLightStartTime(), dto2.getLightStartTime());
                    assertEquals(responseDtoList.getLast().getLightEndTime(), dto2.getLightEndTime());
                    assertEquals(responseDtoList.getLast().isLightReserveState(), dto2.isLightReserveState());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 리스트 가져오기 fail - accessToken 만료")
    @WithMockUser
    void failGetLightReserveList_expiredAccessToken() throws Exception{
        // given
        // when
        when(lightService.lightReserveList())
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(get("/light/reserve-list")
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
    @DisplayName("Light 예약 리스트 가져오기 fail - invalid Token")
    @WithMockUser
    void failGetLightReserveList_invalidAccessToken() throws Exception{
        // given
        // when
        when(lightService.lightReserveList())
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(get("/light/reserve-list")
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

                    assertEquals(response.getErrorCode(), ErrorCode.INVALID_CREDENTIALS);
                    assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED);
                    assertEquals(response.getMessage(), "잘못된 인증정보입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 리스트 가져오기 fail - 일치하는 유저 X")
    @WithMockUser
    void failGetLightReserveList_notFoundUsers() throws Exception{
        // given
        // when
        when(lightService.lightReserveList())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(get("/light/reserve-list")
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
    @DisplayName("Light 예약 리스트 가져오기 fail - 일치하는 어항 X")
    @WithMockUser
    void failGetLightReserveList_notFoundFishbowl() throws Exception{
        // given
        // when
        when(lightService.lightReserveList())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(get("/light/reserve-list")
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

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 유저가 만든 어항이 존재하지 않습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 리스트 가져오기 fail - Light reserve list get 작업 중 error (db 기타 예외만)")
    @WithMockUser
    void failGetLightReserveList_errorGetLightReservesInRDBMS() throws Exception{
        // given
        // when
        when(lightService.lightReserveList())
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(get("/light/reserve-list")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 가져오기 성공")
    @WithMockUser
    void successGetLightReserve() throws Exception {
        //given
        LightResponseDto dto1 =
                LightResponseDto.builder()
                        .id(1L)
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        //when
        when(lightService.lightReserve(1L))
                .thenReturn(dto1);
        //then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    LightResponseDto responseDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<LightResponseDto>() {
                            });

                    assertEquals(responseDto.getId(), dto1.getId());
                    assertEquals(responseDto.getLightStartTime(), dto1.getLightStartTime());
                    assertEquals(responseDto.getLightEndTime(), dto1.getLightEndTime());
                    assertEquals(responseDto.isLightReserveState(), dto1.isLightReserveState());
                })
                .andDo(print());
    }


    @Test
    @DisplayName("Light 예약 가져오기 fail - accessToken 만료")
    @WithMockUser
    void failGetLightReserve_expiredAccessToken() throws Exception {
        // given
        // when
        when(lightService.lightReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
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
    @DisplayName("Light 예약 가져오기 fail - invalid Token")
    @WithMockUser
    void failGetLightReserve_invalidAccessToken() throws Exception {
        // given
        // when
        when(lightService.lightReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 가져오기 fail - 일치하는 유저 X")
    @WithMockUser
    void failGetLightReserve_notFoundUsers() throws Exception {
        // given
        // when
        when(lightService.lightReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
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
    @DisplayName("Light 예약 가져오기 fail - 일치하는 어항 X")
    @WithMockUser
    void failGetLightReserve_notFoundFishbowl() throws Exception {
        // given
        // when
        when(lightService.lightReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 가져오기 fail - 해당하는 Light를 찾는 중 error")
    @WithMockUser
    void failGetLightReserve_errorFoundlightReserves() throws Exception {
        // given
        // when
        when(lightService.lightReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_LIGHT_RESERVE));
        // then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_LIGHT_RESERVE);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 데이터로 Light 예약이 없습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 가져오기 fail - Light reserve get 작업 중 error (db 기타 예외만)")
    @WithMockUser
    void failGetLightReserve_errorGetLightReservesInRDBMS() throws Exception {
        // given
        // when
        when(lightService.lightReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(get("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 생성 성공")
    @WithMockUser
    void successPostlightReserve() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();

        Light light = Light.builder()
                .id(1L)
                .lightStartTime(lightRequestDto.getLightStartTime())
                .lightEndTime(lightRequestDto.getLightEndTime())
                .lightReserveState(true)
                .build();

        LightSuccessDto lightSuccessDto =
                LightSuccessDto.builder()
                        .success(true)
                        .lightResponseDto(
                                LightResponseDto.toResponseDto(light))
                        .build();

        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenReturn(lightSuccessDto);
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    LightSuccessDto successDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            LightSuccessDto.class);

                    assertTrue(successDto.isSuccess());
                    assertEquals(successDto.getLightResponseDto().isLightReserveState(),
                            lightSuccessDto.getLightResponseDto().isLightReserveState());
                    assertEquals(successDto.getLightResponseDto().getLightEndTime(),
                            lightSuccessDto.getLightResponseDto().getLightEndTime());
                    assertEquals(successDto.getLightResponseDto().getLightStartTime(),
                            lightSuccessDto.getLightResponseDto().getLightStartTime());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 생성 실패 - 예약 시작 시간 Null Valid Error")
    @WithMockUser
    void failPostlightReserve_startTimeNullValidError() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(null)
                        .lightEndTime(LocalTime.now())
                        .lightReserveState(true)
                        .build();
        // when
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Map<String, String> errorMap = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {});

                    assertEquals(errorMap.get("errorKey"), "lightStartTime");
                    assertEquals(errorMap.get("message"), "예약 시작값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 생성 fail - accessToken 만료")
    @WithMockUser
    void failPostlightReserve_expiredAccessToken() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
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
    @DisplayName("Light 예약 생성 fail - invalid Token")
    @WithMockUser
    void failPostlightReserve_invalidAccessToken() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 생성 fail - 일치하는 유저 X")
    @WithMockUser
    void failPostlightReserve_notFoundUsers() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
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
    @DisplayName("Light 예약 생성 fail - 일치하는 어항 X")
    @WithMockUser
    void failPostlightReserve_notFoundFishbowl() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 생성 fail - jpa save 저장 과정 중 error (기타 예외만)")
    @WithMockUser
    void failPostlightReserve_errorlightReservesSaveInJPA() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 생성 fail - redis save 저장 과정 중 error (DataAccessException만)")
    @WithMockUser
    void failPostlightReserve_errorlightReservesSaveInRedis() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightCreateReserve(any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS));
        // then
        mockMvc.perform(post("/light/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.DATA_ACCESS_ERROR_IN_REDIS);
                    assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
                    assertEquals(response.getMessage(), "Redis 데이터 접근 중 문제가 발생했습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 변경 성공")
    @WithMockUser
    void successPutLightReserve() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();

        Light light = Light.builder()
                .id(1L)
                .lightStartTime(lightRequestDto.getLightStartTime())
                .lightEndTime(lightRequestDto.getLightEndTime())
                .lightReserveState(true)
                .build();

        LightSuccessDto lightSuccessDto =
                LightSuccessDto.builder()
                        .lightResponseDto(LightResponseDto.toResponseDto(light))
                        .success(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenReturn(lightSuccessDto);
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    LightSuccessDto successDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            LightSuccessDto.class);

                    assertTrue(successDto.isSuccess());
                    assertEquals(successDto.getLightResponseDto().isLightReserveState(),
                            lightSuccessDto.getLightResponseDto().isLightReserveState());
                    assertEquals(successDto.getLightResponseDto().getLightEndTime(),
                            lightSuccessDto.getLightResponseDto().getLightEndTime());
                    assertEquals(successDto.getLightResponseDto().getLightStartTime(),
                            lightSuccessDto.getLightResponseDto().getLightStartTime());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 변경 실패 - 예약 변경 시간 Null Valid Error")
    @WithMockUser
    void failPutLightReserve_startTimeNullValidError() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(null)
                        .lightEndTime(LocalTime.now())
                        .lightReserveState(true)
                        .build();
        // when
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Map<String, String> errorMap = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {});

                    assertEquals(errorMap.get("errorKey"), "lightStartTime");
                    assertEquals(errorMap.get("message"), "예약 시작값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 변경 fail - accessToken 만료")
    @WithMockUser
    void failPutLightReserve_expiredAccessToken() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
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
    @DisplayName("Light 예약 변경 fail - invalid Token")
    @WithMockUser
    void failPutLightReserve_invalidAccessToken() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 변경 fail - 일치하는 유저 X")
    @WithMockUser
    void failPutLightReserve_notFoundUsers() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
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
    @DisplayName("Light 예약 변경 fail - 일치하는 어항 X")
    @WithMockUser
    void failPutLightReserve_notFoundFishbowl() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 변경 fail - 해당하는 Light를 찾는 중 error")
    @WithMockUser
    void failPutLightReserve_errorFoundlightReserves() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_LIGHT_RESERVE));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_LIGHT_RESERVE);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 데이터로 Light 예약이 없습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 변경 fail - redis에 data가 없음")
    @WithMockUser
    void failPutLightReserve_notFoundKeyInRedis() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.VAlUE_NOT_FOUND_IN_REDIS));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.VAlUE_NOT_FOUND_IN_REDIS);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 키에 데이터가 존재하지 않습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 변경 fail - redis에 key가 없음")
    @WithMockUser
    void failPutLightReserve_errorUpdateInRedis() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
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
                    assertEquals(response.getMessage(), "해당 키가 존재하지 않습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 변경 fail - redis 작업 중 error (DataAccessException만)")
    @WithMockUser
    void failPutLightReserve_errorSaveInRedis() throws Exception {
        // given
        LightRequestDto lightRequestDto =
                LightRequestDto.builder()
                        .lightStartTime(LocalTime.now().minusMinutes(3))
                        .lightEndTime(LocalTime.now().minusMinutes(2))
                        .lightReserveState(true)
                        .build();
        // when
        when(lightService.lightUpdateReserve(any(Long.class), any(LightRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS));
        // then
        mockMvc.perform(put("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lightRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.DATA_ACCESS_ERROR_IN_REDIS);
                    assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
                    assertEquals(response.getMessage(), "Redis 데이터 접근 중 문제가 발생했습니다.");
                })
                .andDo(print());
    }


    @Test
    @DisplayName("Light 예약 삭제 성공")
    @WithMockUser
    void successDeleteLightReserve() throws Exception {
        // given
        DeleteLightSuccessDto deleteLightSuccessDto =
                DeleteLightSuccessDto.builder()
                        .success(true)
                        .build();
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenReturn(deleteLightSuccessDto);
        // then
        mockMvc.perform(delete("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    DeleteLightSuccessDto successDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<DeleteLightSuccessDto>() {});

                    assertTrue(successDto.isSuccess());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 삭제 fail - accessToken 만료")
    @WithMockUser
    void failDeleteLightReserve_expiredAccessToken() throws Exception {
        // given
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(delete("/light/reserve/1")
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
    @DisplayName("Light 예약 삭제 fail - invalid Token")
    @WithMockUser
    void failDeleteLightReserve_invalidAccessToken() throws Exception {
        // given
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(delete("/light/reserve/1")
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

                    assertEquals(response.getErrorCode(), ErrorCode.INVALID_CREDENTIALS);
                    assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED);
                    assertEquals(response.getMessage(), "잘못된 인증정보입니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 삭제 fail - 일치하는 유저 X")
    @WithMockUser
    void failDeleteLightReserve_notFoundUsers() throws Exception {
        // given
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(delete("/light/reserve/1")
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
    @DisplayName("Light 예약 삭제 fail - 일치하는 어항 X")
    @WithMockUser
    void failDeleteLightReserve_notFoundFishbowl() throws Exception {
        // given
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(delete("/light/reserve/1")
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

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 유저가 만든 어항이 존재하지 않습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("Light 예약 삭제 fail - jpa delete error (기타 예외처리만)")
    @WithMockUser
    void failDeleteLightReserve_errorDeleteInJPA() throws Exception {
        // given
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(delete("/light/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
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
    @DisplayName("Light 예약 삭제 fail - not found key in redis")
    @WithMockUser
    void failDeleteLightReserve_notFoundKeyInRedis() throws Exception {
        // given
        // when
        when(lightService.lightDeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS));
        // then
        mockMvc.perform(delete("/light/reserve/1")
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
                    assertEquals(response.getMessage(), "해당 키가 존재하지 않습니다.");
                })
                .andDo(print());
    }
}