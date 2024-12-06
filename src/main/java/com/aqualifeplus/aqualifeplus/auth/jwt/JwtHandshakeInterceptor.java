package com.aqualifeplus.aqualifeplus.auth.jwt;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.users.service.UsersService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
//    private static final String MyFishbowlAll = "MyFishbowlAll";
    private final JwtService jwtService;
    private final UsersService usersService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> authHeaders = request.getHeaders().get("Authorization");
//        List<String> fishbowlHeaders = request.getHeaders().get("X-Fishbowl-Token");
        try {
//            if (fishbowlHeaders != null && !fishbowlHeaders.isEmpty()
//                    && !fishbowlHeaders.getFirst().equals(MyFishbowlAll)) {
//                String fishbowlToken = fishbowlHeaders.getFirst();
//                UUID.fromString(fishbowlToken);
//                attributes.put("fishbowlToken", fishbowlToken);
//            }

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String token = authHeaders.getFirst().replace("Bearer ", "");
                if (!jwtService.isTokenExpired(token)) {
                    attributes.put("userId", usersService.getId(jwtService.extractEmail(token))); // 세션 속성에 이메일 저장
                    return true;
                }
            }
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.NOT_MATCH_UUID_FORMAT);
        }

        return false; // 토큰 검증 실패 시 연결 거부
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
