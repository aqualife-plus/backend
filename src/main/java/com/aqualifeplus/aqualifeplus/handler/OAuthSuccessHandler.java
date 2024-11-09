package com.aqualifeplus.aqualifeplus.handler;

import com.aqualifeplus.aqualifeplus.dto.LoginDto;
import com.aqualifeplus.aqualifeplus.dto.TokenDto;
import com.aqualifeplus.aqualifeplus.dto.UsersRequestDto;
import com.aqualifeplus.aqualifeplus.repository.UsersRepository;
import com.aqualifeplus.aqualifeplus.entity.CustomOAuthUsers;
import com.aqualifeplus.aqualifeplus.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuthUsers customOAuthUsers = (CustomOAuthUsers) authentication.getPrincipal();
        LoginDto loginDto = new LoginDto(
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

        TokenDto tokenDto = usersService.login(loginDto);

        // 성공 후 json으로 return
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write("{\"accessToken\":\"" + tokenDto.getAccessToken() + "\"}" +
                        "{\"userToken\":\"" + tokenDto.getUserToken() + "\"}" +
                        "{\"refreshToken\":\"" + tokenDto.getRefreshToken() + "\"}");

    }
}

