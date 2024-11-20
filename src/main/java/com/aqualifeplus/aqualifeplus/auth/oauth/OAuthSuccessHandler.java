package com.aqualifeplus.aqualifeplus.auth.oauth;

import com.aqualifeplus.aqualifeplus.auth.service.AuthService;
import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.aqualifeplus.aqualifeplus.auth.dto.LoginRequestDto;
import com.aqualifeplus.aqualifeplus.auth.dto.TokenResponseDto;
import com.aqualifeplus.aqualifeplus.users.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import com.aqualifeplus.aqualifeplus.auth.entity.CustomOAuthUsers;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@NoLogging
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AuthService authService;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuthUsers customOAuthUsers = (CustomOAuthUsers) authentication.getPrincipal();
        LoginRequestDto loginRequestDto = new LoginRequestDto(
                customOAuthUsers.getEmail(),
                customOAuthUsers.getPassword());

        if (usersRepository.findByEmail(customOAuthUsers.getAttribute("email")).isEmpty()) {
            usersService.signUp(
                    UsersRequestDto.builder()
                            .email(customOAuthUsers.getEmail())
                            .password(customOAuthUsers.getPassword())
                            .nickname(customOAuthUsers.getNickname())
                            .phoneNumber(customOAuthUsers.getPhoneNumber())
                            .accountType(customOAuthUsers.getAccountType())
                            .accessDate(LocalDateTime.now())
                            .subscriptionDate(LocalDateTime.now())
                            .changeDate(LocalDateTime.now())
                            .build());
        }

        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);

        // 성공 후 json으로 return
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write("{\"accessToken\":\"" + tokenResponseDto.getAccessToken() + "\"}" +
                        "{\"userToken\":\"" + tokenResponseDto.getUserToken() + "\"}" +
                        "{\"refreshToken\":\"" + tokenResponseDto.getRefreshToken() + "\"}");

    }
}

