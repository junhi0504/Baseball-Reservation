package com.example.baseball_reservation.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 실제 서비스에서는 환경변수나 설정 파일(yml)에서 가져오는 것이 좋습니다.
    // 테스트용으로 임의의 키를 생성합니다.
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 토큰 유효 시간: 1시간
    private final long validityInMilliseconds = 3600000;

    // 1. JWT 토큰 생성
    public String createToken(String loginId) {
        Claims claims = Jwts.claims().setSubject(loginId);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    // 2. 토큰에서 사용자 아이디(loginId) 추출
    public String getLoginId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 3. 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 변조되었거나 만료된 경우
            return false;
        }
    }
}