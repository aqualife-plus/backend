package com.aqualifeplus.aqualifeplus.auth.jwt;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final HttpServletRequest request;

    @Value("${jwt.secretKey}")
    private String secretKey;
    // 추가된 메서드: 토큰 만료 시간 반환
    @Getter
    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;
    @Getter
    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    public String makeAccessToken(String username) {
        return getJwtToken(username, accessTokenExpirationMs);
    }

    public String makeRefreshToken(String username) {
        return getJwtToken(username, refreshTokenExpirationMs);
    }

    private String getJwtToken(String username, long tokenExpirationMs) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpirationMs))
                .signWith(
                        new SecretKeySpec(Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                .compact();
    }

    public String getAuthorization() {
        return request.getHeader("Authorization").substring(7);
    }
    public String getFishbowlToken() {
        return request.getHeader("X-Fishbowl-Token");
    }

    public String getEmail() {return extractEmail(getAuthorization());}

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
