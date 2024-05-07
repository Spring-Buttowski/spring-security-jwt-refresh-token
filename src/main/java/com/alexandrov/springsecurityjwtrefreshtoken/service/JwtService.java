package com.alexandrov.springsecurityjwtrefreshtoken.service;

import com.alexandrov.springsecurityjwtrefreshtoken.constants.ProjectConstants;
import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class JwtService {
    @Value("${keys.jwt_access_secret_key}")
    private String jwtAccessSecretKey;

    @Value("${keys.jwt_refresh_secret_key}")
    private String jwtRefreshSecretKey;

    public static SecretKey ACCESS_TOKEN_SECRET_KEY;

    public static SecretKey REFRESH_TOKEN_SECRET_KEY;

    @PostConstruct
    public void init() {
        ACCESS_TOKEN_SECRET_KEY = Keys.hmacShaKeyFor(jwtAccessSecretKey.getBytes(StandardCharsets.UTF_8));
        REFRESH_TOKEN_SECRET_KEY = Keys.hmacShaKeyFor(jwtRefreshSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusMinutes(ProjectConstants.ACCESS_TOKEN_EXPIRATION_IN_MINUTES).atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);
        return Jwts.builder()
                .setIssuer(ProjectConstants.JWT_ISSUER)
                .setSubject(String.valueOf(user.getId()))
                .setExpiration(accessExpiration)
                .signWith(ACCESS_TOKEN_SECRET_KEY)
                .claim(ProjectConstants.ROLE, user.getRole())
                .compact();
    }

    public String generateRefreshToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(ProjectConstants.REFRESH_TOKEN_EXPIRATION_IN_DAYS).atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        return Jwts.builder()
                .setIssuer(ProjectConstants.JWT_ISSUER)
                .setSubject(String.valueOf(user.getId()))
                .setExpiration(refreshExpiration)
                .signWith(REFRESH_TOKEN_SECRET_KEY)
                .compact();
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, REFRESH_TOKEN_SECRET_KEY);
    }

    public Claims getAccessClaims(String token) {
        return getClaims(token, ACCESS_TOKEN_SECRET_KEY);
    }

    public Claims getRefreshClaims(String token) {
        return getClaims(token, REFRESH_TOKEN_SECRET_KEY);
    }

    private Claims getClaims(String token, Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean validateToken(String token, Key secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired", expEx);
            throw expEx;
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt", unsEx);
            throw unsEx;
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt", mjEx);
            throw mjEx;
        } catch (SignatureException sEx) {
            log.error("Invalid signature", sEx);
            throw sEx;
        } catch (Exception e) {
            log.error("invalid token", e);
            throw e;
        }
    }
}
