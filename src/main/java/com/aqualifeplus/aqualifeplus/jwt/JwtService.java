package com.aqualifeplus.aqualifeplus.jwt;

import com.aqualifeplus.aqualifeplus.exception.CustomException;
import com.aqualifeplus.aqualifeplus.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${jwt.secretKey}")
    private String secretKey;
    // 추가된 메서드: 토큰 만료 시간 반환
    @Getter
    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;
    @Getter
    @Value("${jwt.userTokenExpirationMs}")
    private long userTokenExpirationMs;
    @Getter
    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    public String makeAccessToken(String username) {
        return getJwtToken(username, accessTokenExpirationMs);
    }

    public String makeUserToken(String username) {
        return getJwtToken(username, userTokenExpirationMs);
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
