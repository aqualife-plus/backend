package com.aqualifeplus.aqualifeplus.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    @Value("${jwt.secretKey}")
    private String secretKey;
    // 추가된 메서드: 토큰 만료 시간 반환
    @Getter
    @Value("${jwt.expirationMs}")
    private long expirationMs ; // 1시간

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        System.out.println(token);
        System.out.println(Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject());
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean extractClaims(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
