package dev.minsu.project.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    Logger log = LoggerFactory.getLogger(JwtProvider.class);

    // config
    private static String secretKey = "";

    private final long accessTokenValidMillisecond = 1000L * 10 * 60 * 60; // 10분
    private final long refreshTokenValidMillisecond = 1000L * 60 * 60 * 60; // 1시간

    private final UserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // access 토큰 생성
    public String createAccessToken(String nickname) {
        Claims claims = Jwts.claims().setSubject(nickname);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // refresh 토큰 생성
    public String createRefreshToken(String nickname) {
        Claims claims = Jwts.claims().setSubject(nickname);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰으로 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 회원 구별 정보 추출
    public String getEmail(String token) {
        return getClaims(token).getBody().getSubject();
    }

    // 토큰 추출
    public String getToken(HttpServletRequest request) {
        String token = resolveToken(request);
        if ( !validateToken(token) ) {
            throw new IllegalStateException("토큰이 유효하지 않습니다.");
        }
        return getEmail(token);
    }

    // request에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("X-AUTH-TOKEN");
    }

    public boolean validateToken(String token) {
        try {
            log.info("토큰 유효성 검사");
            Jws<Claims> claims = getClaims(token);
            log.info("토큰 유효성 검사 성공");
            return !claims.getBody().getExpiration().before(new Date());
        } catch ( NullPointerException e ) {
            log.error("토큰이 없습니다.");
            throw e;
        } catch ( Exception e ) {
            log.error("토큰 유효성 검사 실패");
            throw e;
        }
    }

    public Jws<Claims> getClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        } catch ( SecurityException | MalformedJwtException me ) {
            log.error("잘못된 JWT 서명입니다.");
            throw me;
        } catch ( ExpiredJwtException ee ) {
            log.error("만료된 JWT 토큰입니다.");
            throw ee;
        } catch ( UnsupportedJwtException ue ) {
            log.error("지원되지 않는 JWT 토큰입니다.");
            throw ue;
        } catch ( IllegalArgumentException ie ) {
            log.error("JWT 토큰이 잘못되었습니다.");
            throw ie;
        }
    }
}
