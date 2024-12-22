package com.aqualifeplus.aqualifeplus.co2.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.auth.oauth.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.auth.oauth.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2ResponseDto;
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
    void failGetCo2Reserve_expiredAccessToken() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - invalid Token")
    void failGetCo2Reserve_invalidAccessToken() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - 일치하는 유저 X")
    void failGetCo2Reserve_notFoundUsers() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - invalid 어항토큰")
    void failGetCo2Reserve_invalidFishbowlToken() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - 일치하는 어항 X")
    void failGetCo2Reserve_notFoundFishbowl() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - 해당하는 co2를 찾는 중 error")
    void failGetCo2Reserve_errorFoundCo2Reserves() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("co2 예약 가져오기 fail - co2 reserve list get 작업 중 error (db 기타 예외만)")
    void failGetCo2Reserve_errorGetCo2ReservesInRDBMS() {
        // given
        // when
        // then
    }
    
    @Test
    @DisplayName("어항 생성 성공")
    void successPostCo2Reserve() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 수정 성공")
    void successPutCo2Reserve() {
        // given
        // when
        // then
    }

    @Test
    @DisplayName("어항 삭제 성공")
    void successDeleteCo2Reserve() {
        // given
        // when
        // then
    }
}