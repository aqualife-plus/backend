package com.aqualifeplus.aqualifeplus.filter.controller;

import static org.junit.jupiter.api.Assertions.*;
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
import com.aqualifeplus.aqualifeplus.auth.service.AuthService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorResponse;
import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.filter.dto.FilterRequestDto;
import com.aqualifeplus.aqualifeplus.filter.dto.FilterResponseDto;
import com.aqualifeplus.aqualifeplus.filter.dto.UpdateFilterResponseDto;
import com.aqualifeplus.aqualifeplus.filter.service.FilterService;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
@WebMvcTest(FilterController.class)
class FilterControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilterService filterService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;
    @MockBean
    private CustomOAuthUserService customOAuthUserService;

    @Test
    @DisplayName("환수 가져오기 성공")
    @WithMockUser
    void successGetFilter() throws Exception {
        //given
        FilterResponseDto filterResponseDto =
                FilterResponseDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.now())
                        .build();
        //when
        when(filterService.getFilter())
                .thenReturn(filterResponseDto);

        //then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    FilterResponseDto responseDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            FilterResponseDto.class);

                    assertEquals(responseDto.getFilterDay(), filterResponseDto.getFilterDay());
                    assertEquals(responseDto.getFilterTime(), filterResponseDto.getFilterTime());
                    assertEquals(responseDto.getFilterRange(), filterResponseDto.getFilterRange());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - accessToken 만료")
    @WithMockUser
    void failGetFilter_expiredAccessToken() throws Exception {
        // given
        // when
        when(filterService.getFilter())
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
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
    void failGetFilter_invalidAccessToken() throws Exception {
        // given
        // when
        when(filterService.getFilter())
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
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
    void failGetFilter_notFoundUsers() throws Exception {
        // given
        // when
        when(filterService.getFilter())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
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
    @DisplayName("환수 가져오기 fail - 일치하는 어항 X")
    @WithMockUser
    void failGetFilter_notFoundFishbowl() throws Exception {
        // given
        // when
        when(filterService.getFilter())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
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
    @DisplayName("환수 가져오기 fail - 일치하는 환수 X")
    @WithMockUser
    void failGetFilter_notFoundFilter() throws Exception {
        // given
        // when
        when(filterService.getFilter())
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FILTER));
        // then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_FILTER);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 Filter가 없습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 가져오기 fail - jpa error (기타 예외만)")
    @WithMockUser
    void failGetFilter_UnExpectedErrorInJPA() throws Exception {
        // given
        // when
        when(filterService.getFilter())
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(get("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
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
    @DisplayName("환수 변경 성공")
    @WithMockUser
    void successUpdateFilter() throws Exception {
        //given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();

        UpdateFilterResponseDto updateFilterResponseDto =
                UpdateFilterResponseDto.builder()
                        .success(true)
                        .build();
        //when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenReturn(updateFilterResponseDto);

        //then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    UpdateFilterResponseDto responseDto = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            UpdateFilterResponseDto.class);

                    assertTrue(responseDto.isSuccess());
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 변경 fail - filterDay blank valid")
    @WithMockUser
    void failUpdateFilter_filterDayNullValidError() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();

        // when
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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

                    assertEquals(errorMap.get("errorKey"), "filterDay");
                    assertEquals(errorMap.get("message"), "설정할 요일데이터가 필요합니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 변경 fail - filterRange null valid")
    @WithMockUser
    void failUpdateFilter_filterRangeNullValidError() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0001000")
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();

        // when
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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

                    assertEquals(errorMap.get("errorKey"), "filterRange");
                    assertEquals(errorMap.get("message"), "설정할 환수량이 필요합니다.");
                })
                .andDo(print());
    }


    @Test
    @DisplayName("환수 변경 fail - filterTime null valid")
    @WithMockUser
    void failUpdateFilter_filterTimeNullValidError() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0001000")
                        .filterRange(3)
                        .build();

        // when
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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

                    assertEquals(errorMap.get("errorKey"), "filterTime");
                    assertEquals(errorMap.get("message"), "설정할 시간이 필요합니다.");
                })
                .andDo(print());
    }


    @Test
    @DisplayName("환수 변경 fail - accessToken 만료")
    @WithMockUser
    void failUpdateFilter_expiredAccessToken() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();

        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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
    @DisplayName("환수 변경 fail - invalid Token")
    @WithMockUser
    void failUpdateFilter_invalidAccessToken() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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
    @DisplayName("환수 변경 fail - 일치하는 유저 X")
    @WithMockUser
    void failUpdateFilter_notFoundUsers() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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
    @DisplayName("환수 변경 fail - 일치하는 어항 X")
    @WithMockUser
    void failUpdateFilter_notFoundFishbowl() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
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
    @DisplayName("환수 변경 fail - 일치하는 환수 X")
    @WithMockUser
    void failUpdateFilter_notFoundFilter() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_FILTER));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
                .andExpect(result -> {
                    ErrorResponse response = objectMapper.readValue(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            ErrorResponse.class);

                    assertEquals(response.getErrorCode(), ErrorCode.NOT_FOUND_FILTER);
                    assertEquals(response.getStatus(), HttpStatus.NOT_FOUND);
                    assertEquals(response.getMessage(), "해당 Filter가 없습니다.");
                })
                .andDo(print());
    }

    @Test
    @DisplayName("환수 변경 fail - jpa error (기타 예외만)")
    @WithMockUser
    void failUpdateFilter_UnExpectedErrorInJPA() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
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
    @DisplayName("환수 변경 fail - firebase update error")
    @WithMockUser
    void failUpdateFilter_errorUpdateFirebase() throws Exception {
        // TODO : firebase
        // given
        // when
        // then
    }

    @Test
    @DisplayName("환수 변경 fail - redis key not found error")
    @WithMockUser
    void failUpdateFilter_errorNotFoundKeyInRedis() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_KEY_IN_REDIS));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
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
    @DisplayName("환수 변경 fail - redis save error (DataAccessException만)")
    @WithMockUser
    void failUpdateFilter_errorSaveInRedis() throws Exception {
        // given
        FilterRequestDto filterRequestDto =
                FilterRequestDto.builder()
                        .filterDay("0000000")
                        .filterRange(3)
                        .filterTime(LocalTime.parse("11:22", DateTimeFormatter.ofPattern("HH:mm")))
                        .build();
        // when
        when(filterService.updateFilter(any(FilterRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_REDIS));
        // then
        mockMvc.perform(put("/filter")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(
                        CustomException.class,
                        result.getResolvedException()
                ))
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

}