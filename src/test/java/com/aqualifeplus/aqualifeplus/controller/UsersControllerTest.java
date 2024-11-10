package com.aqualifeplus.aqualifeplus.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aqualifeplus.aqualifeplus.config.SecurityConfig;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.entity.Users;
import com.aqualifeplus.aqualifeplus.handler.OAuthSuccessHandler;
import com.aqualifeplus.aqualifeplus.service.CustomOAuthUserService;
import com.aqualifeplus.aqualifeplus.service.JwtService;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest(UsersController.class)
class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

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
        // when
        when(usersService.signUp(any(UsersRequestDto.class))).thenReturn(true);
        // then
        String responseValue = mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usersRequestDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals("true", responseValue);
    }
}