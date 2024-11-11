package com.aqualifeplus.aqualifeplus.jwt;

import com.aqualifeplus.aqualifeplus.exception.CustomException;
import com.aqualifeplus.aqualifeplus.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        // Bearer 토큰이 있는지 확인하고 시작
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7); // "Bearer " 이후의 JWT 추출
            String email = jwtService.extractEmail(jwt);

            // 이메일이 유효하고, 현재 SecurityContext에 인증 객체가 설정되지 않은 경우에만 인증을 설정
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 객체 설정
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 예외 없이 정상적으로 요청을 계속 진행하도록 필터 체인 실행
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException ex) {
            // CustomException 발생 시 응답에 에러 메시지와 상태 코드 설정
            response.setStatus(ex.getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + ex.getMessage() + "\"}");
            response.getWriter().flush();
        }
    }
}

