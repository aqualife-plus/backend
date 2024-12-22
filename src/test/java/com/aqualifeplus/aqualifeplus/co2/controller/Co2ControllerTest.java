package com.aqualifeplus.aqualifeplus.co2.controller;

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
import com.aqualifeplus.aqualifeplus.co2.dto.Co2RequestDto;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2ResponseDto;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2SuccessDto;
import com.aqualifeplus.aqualifeplus.co2.dto.DeleteCo2SuccessDto;
import com.aqualifeplus.aqualifeplus.co2.entity.Co2;
import com.aqualifeplus.aqualifeplus.co2.service.Co2Service;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorResponse;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
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
@WebMvcTest(Co2Controller.class)
class Co2ControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Co2Service co2Service;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("co2 예약 리스트 가져오기 성공")
    @WithMockUser
    void successGetCo2ReserveList() throws Exception {
        //given
        Co2ResponseDto dto1 =
                Co2ResponseDto.builder()
                        .id(1L)
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        Co2ResponseDto dto2 =
                Co2ResponseDto.builder()
                        .id(2L)
                        .co2StartTime(LocalTime.now().minusMinutes(1))
                        .co2EndTime(LocalTime.now())
                        .co2ReserveState(false)
                        .build();
        List<Co2ResponseDto> dtoList = List.of(dto1, dto2);
        //when
        when(co2Service.co2ReserveList())
                .thenReturn(dtoList);
        //then
        mockMvc.perform(get("/co2/reserve-list")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<Co2ResponseDto> responseDtoList = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<List<Co2ResponseDto>>() {
                            });

                    assertEquals(responseDtoList.getFirst().getId(), dto1.getId());
                    assertEquals(responseDtoList.getFirst().getCo2StartTime(), dto1.getCo2StartTime());
                    assertEquals(responseDtoList.getFirst().getCo2EndTime(), dto1.getCo2EndTime());
                    assertEquals(responseDtoList.getFirst().isCo2ReserveState(), dto1.isCo2ReserveState());
                    assertEquals(responseDtoList.getLast().getId(), dto2.getId());
                    assertEquals(responseDtoList.getLast().getCo2StartTime(), dto2.getCo2StartTime());
                    assertEquals(responseDtoList.getLast().getCo2EndTime(), dto2.getCo2EndTime());
                    assertEquals(responseDtoList.getLast().isCo2ReserveState(), dto2.isCo2ReserveState());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 리스트 가져오기 fail - accessToken 만료")
    @WithMockUser
    void failGetCo2ReserveList_expiredAccessToken() throws Exception{
        // given
        // when
        when(co2Service.co2ReserveList())
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(get("/co2/reserve-list")
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
    @DisplayName("co2 예약 리스트 가져오기 fail - invalid Token")
    @WithMockUser
    void failGetCo2ReserveList_invalidAccessToken() throws Exception{
        // given
        // when
        when(co2Service.co2ReserveList())
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(get("/co2/reserve-list")
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
    @DisplayName("co2 예약 리스트 가져오기 fail - 일치하는 유저 X")
    @WithMockUser
    void failGetCo2ReserveList_notFoundUsers() throws Exception{
        // given
        // when
        when(co2Service.co2ReserveList())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(get("/co2/reserve-list")
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
    @DisplayName("co2 예약 리스트 가져오기 fail - 일치하는 어항 X")
    @WithMockUser
    void failGetCo2ReserveList_notFoundFishbowl() throws Exception{
        // given
        // when
        when(co2Service.co2ReserveList())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(get("/co2/reserve-list")
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
    @DisplayName("co2 예약 리스트 가져오기 fail - co2 reserve list get 작업 중 error (db 기타 예외만)")
    @WithMockUser
    void failGetCo2ReserveList_errorGetCo2ReservesInRDBMS() throws Exception{
        // given
        // when
        when(co2Service.co2ReserveList())
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(get("/co2/reserve-list")
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
    @DisplayName("co2 예약 가져오기 성공")
    @WithMockUser
    void successGetCo2Reserve() throws Exception {
        //given
        Co2ResponseDto dto1 =
                Co2ResponseDto.builder()
                        .id(1L)
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        //when
        when(co2Service.co2Reserve(1L))
                .thenReturn(dto1);
        //then
        mockMvc.perform(get("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    Co2ResponseDto responseDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Co2ResponseDto>() {
                            });

                    assertEquals(responseDto.getId(), dto1.getId());
                    assertEquals(responseDto.getCo2StartTime(), dto1.getCo2StartTime());
                    assertEquals(responseDto.getCo2EndTime(), dto1.getCo2EndTime());
                    assertEquals(responseDto.isCo2ReserveState(), dto1.isCo2ReserveState());
                })
                .andDo(print());
    }


    @Test
    @DisplayName("co2 예약 가져오기 fail - accessToken 만료")
    @WithMockUser
    void failGetCo2Reserve_expiredAccessToken() throws Exception {
        // given
        // when
        when(co2Service.co2Reserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(get("/co2/reserve/1")
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
    @DisplayName("co2 예약 가져오기 fail - invalid Token")
    @WithMockUser
    void failGetCo2Reserve_invalidAccessToken() throws Exception {
        // given
        // when
        when(co2Service.co2Reserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(get("/co2/reserve/1")
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
    @DisplayName("co2 예약 가져오기 fail - 일치하는 유저 X")
    @WithMockUser
    void failGetCo2Reserve_notFoundUsers() throws Exception {
        // given
        // when
        when(co2Service.co2Reserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(get("/co2/reserve/1")
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
    @DisplayName("co2 예약 가져오기 fail - 일치하는 어항 X")
    @WithMockUser
    void failGetCo2Reserve_notFoundFishbowl() throws Exception {
        // given
        // when
        when(co2Service.co2Reserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(get("/co2/reserve/1")
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
    @DisplayName("co2 예약 가져오기 fail - 해당하는 co2를 찾는 중 error")
    @WithMockUser
    void failGetCo2Reserve_errorFoundCo2Reserves() throws Exception {
        // given
        // when
        when(co2Service.co2Reserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_CO2_RESERVE));
        // then
        mockMvc.perform(get("/co2/reserve/1")
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

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_CO2_RESERVE);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 데이터로 Co2 예약이 없습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - co2 reserve get 작업 중 error (db 기타 예외만)")
    @WithMockUser
    void failGetCo2Reserve_errorGetCo2ReservesInRDBMS() throws Exception {
        // given
        // when
        when(co2Service.co2Reserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(get("/co2/reserve/1")
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
    @DisplayName("co2 예약 생성 성공")
    @WithMockUser
    void successPostCo2Reserve() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();

        Co2 co2 = Co2.builder()
                .id(1L)
                .co2StartTime(co2RequestDto.getCo2StartTime())
                .co2EndTime(co2RequestDto.getCo2EndTime())
                .co2ReserveState(true)
                .build();

        Co2SuccessDto co2SuccessDto =
                Co2SuccessDto.builder()
                        .success(true)
                        .co2ResponseDto(
                                Co2ResponseDto.toResponseDto(co2))
                        .build();

        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenReturn(co2SuccessDto);
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    Co2SuccessDto successDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            Co2SuccessDto.class);

                    assertTrue(successDto.isSuccess());
                    assertEquals(successDto.getCo2ResponseDto().isCo2ReserveState(),
                            co2SuccessDto.getCo2ResponseDto().isCo2ReserveState());
                    assertEquals(successDto.getCo2ResponseDto().getCo2EndTime(),
                            co2SuccessDto.getCo2ResponseDto().getCo2EndTime());
                    assertEquals(successDto.getCo2ResponseDto().getCo2StartTime(),
                            co2SuccessDto.getCo2ResponseDto().getCo2StartTime());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 생성 실패 - 예약 시작 시간 Null Valid Error")
    @WithMockUser
    void failPostCo2Reserve_startTimeNullValidError() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(null)
                        .co2EndTime(LocalTime.now())
                        .co2ReserveState(true)
                        .build();
        // when
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Map<String, String> errorMap = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {});

                    assertEquals(errorMap.get("errorKey"), "co2StartTime");
                    assertEquals(errorMap.get("message"), "예약 시작값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 생성 fail - accessToken 만료")
    @WithMockUser
    void failPostCo2Reserve_expiredAccessToken() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 생성 fail - invalid Token")
    @WithMockUser
    void failPostCo2Reserve_invalidAccessToken() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 생성 fail - 일치하는 유저 X")
    @WithMockUser
    void failPostCo2Reserve_notFoundUsers() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 생성 fail - 일치하는 어항 X")
    @WithMockUser
    void failPostCo2Reserve_notFoundFishbowl() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 생성 fail - jpa save 저장 과정 중 error (기타 예외만)")
    @WithMockUser
    void failPostCo2Reserve_errorCo2ReservesSaveInJPA() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 생성 fail - redis save 저장 과정 중 error (DataAccessException만)")
    @WithMockUser
    void failPostCo2Reserve_errorCo2ReservesSaveInRedis() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2CreateReserve(any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS));
        // then
        mockMvc.perform(post("/co2/reserve")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 성공")
    @WithMockUser
    void successPutCo2Reserve() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();

        Co2 co2 = Co2.builder()
                .id(1L)
                .co2StartTime(co2RequestDto.getCo2StartTime())
                .co2EndTime(co2RequestDto.getCo2EndTime())
                .co2ReserveState(true)
                .build();

        Co2SuccessDto co2SuccessDto =
                Co2SuccessDto.builder()
                        .co2ResponseDto(Co2ResponseDto.toResponseDto(co2))
                        .success(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenReturn(co2SuccessDto);
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    Co2SuccessDto successDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            Co2SuccessDto.class);

                    assertTrue(successDto.isSuccess());
                    assertEquals(successDto.getCo2ResponseDto().isCo2ReserveState(),
                            co2SuccessDto.getCo2ResponseDto().isCo2ReserveState());
                    assertEquals(successDto.getCo2ResponseDto().getCo2EndTime(),
                            co2SuccessDto.getCo2ResponseDto().getCo2EndTime());
                    assertEquals(successDto.getCo2ResponseDto().getCo2StartTime(),
                            co2SuccessDto.getCo2ResponseDto().getCo2StartTime());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 변경 실패 - 예약 변경 시간 Null Valid Error")
    @WithMockUser
    void failPutCo2Reserve_startTimeNullValidError() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(null)
                        .co2EndTime(LocalTime.now())
                        .co2ReserveState(true)
                        .build();
        // when
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Map<String, String> errorMap = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<Map<String, String>>() {});

                    assertEquals(errorMap.get("errorKey"), "co2StartTime");
                    assertEquals(errorMap.get("message"), "예약 시작값이 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 변경 fail - accessToken 만료")
    @WithMockUser
    void failPutCo2Reserve_expiredAccessToken() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 fail - invalid Token")
    @WithMockUser
    void failPutCo2Reserve_invalidAccessToken() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 fail - 일치하는 유저 X")
    @WithMockUser
    void failPutCo2Reserve_notFoundUsers() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 fail - 일치하는 어항 X")
    @WithMockUser
    void failPutCo2Reserve_notFoundFishbowl() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 fail - 해당하는 co2를 찾는 중 error")
    @WithMockUser
    void failPutCo2Reserve_errorFoundCo2Reserves() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_CO2_RESERVE));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_CO2_RESERVE);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 데이터로 Co2 예약이 없습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 변경 fail - redis에 data가 없음")
    @WithMockUser
    void failPutCo2Reserve_notFoundKeyInRedis() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.VAlUE_NOT_FOUND_IN_REDIS));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 fail - redis에 key가 없음")
    @WithMockUser
    void failPutCo2Reserve_errorUpdateInRedis() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 변경 fail - redis 작업 중 error (DataAccessException만)")
    @WithMockUser
    void failPutCo2Reserve_errorSaveInRedis() throws Exception {
        // given
        Co2RequestDto co2RequestDto =
                Co2RequestDto.builder()
                        .co2StartTime(LocalTime.now().minusMinutes(3))
                        .co2EndTime(LocalTime.now().minusMinutes(2))
                        .co2ReserveState(true)
                        .build();
        // when
        when(co2Service.co2UpdateReserve(any(Long.class), any(Co2RequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS));
        // then
        mockMvc.perform(put("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(co2RequestDto)))
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
    @DisplayName("co2 예약 삭제 성공")
    @WithMockUser
    void successDeleteCo2Reserve() throws Exception {
        // given
        DeleteCo2SuccessDto deleteCo2SuccessDto =
                DeleteCo2SuccessDto.builder()
                        .success(true)
                        .build();
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenReturn(deleteCo2SuccessDto);
        // then
        mockMvc.perform(delete("/co2/reserve/1")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    DeleteCo2SuccessDto successDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            new TypeReference<DeleteCo2SuccessDto>() {});

                    assertTrue(successDto.isSuccess());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("co2 예약 삭제 fail - accessToken 만료")
    @WithMockUser
    void failDeleteCo2Reserve_expiredAccessToken() throws Exception {
        // given
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(delete("/co2/reserve/1")
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
    @DisplayName("co2 예약 삭제 fail - invalid Token")
    @WithMockUser
    void failDeleteCo2Reserve_invalidAccessToken() throws Exception {
        // given
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(delete("/co2/reserve/1")
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
    @DisplayName("co2 예약 삭제 fail - 일치하는 유저 X")
    @WithMockUser
    void failDeleteCo2Reserve_notFoundUsers() throws Exception {
        // given
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(delete("/co2/reserve/1")
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
    @DisplayName("co2 예약 삭제 fail - 일치하는 어항 X")
    @WithMockUser
    void failDeleteCo2Reserve_notFoundFishbowl() throws Exception {
        // given
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(delete("/co2/reserve/1")
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
    @DisplayName("co2 예약 삭제 fail - jpa delete error (기타 예외처리만)")
    @WithMockUser
    void failDeleteCo2Reserve_errorDeleteInJPA() throws Exception {
        // given
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(delete("/co2/reserve/1")
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
    @DisplayName("co2 예약 삭제 fail - not found key in redis")
    @WithMockUser
    void failDeleteCo2Reserve_notFoundKeyInRedis() throws Exception {
        // given
        // when
        when(co2Service.co2DeleteReserve(any(Long.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS));
        // then
        mockMvc.perform(delete("/co2/reserve/1")
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