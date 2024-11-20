package com.aqualifeplus.aqualifeplus.auth.jwt;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final List<String> excludedPaths =
            Arrays.asList(
                    "/auth/login",
                    "/users/signup",
                    "/auth/google/login",
                    "/auth/naver/login",
                    "/users/check-email");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        // 로그인, 회원가입 API URL이 포함되는지 확인하는 함수
        boolean exclude = excludedPaths.stream().anyMatch(request.getRequestURI()::startsWith);
        log.info("Request URI: {}, Should Exclude: {}", request.getRequestURI(), exclude);
//        return excludedPaths.stream().anyMatch(request.getRequestURI()::startsWith);
        return exclude;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        try{
            String jwt = token.substring(7);

            jwtService.getClaims(jwt);

            String email = jwtService.extractEmail(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (CustomException ex) {
            response.setCharacterEncoding("UTF-8");
            response.setStatus(ex.getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\": \"" + ex.getStatus().getReasonPhrase().toUpperCase() + "\",\n");
            response.getWriter().write("\"errorCode\": \"" + ex.getErrorCode() + "\",\n");
            response.getWriter().write("\"message\": \"" + ex.getMessage() + "\"}");
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }
}

